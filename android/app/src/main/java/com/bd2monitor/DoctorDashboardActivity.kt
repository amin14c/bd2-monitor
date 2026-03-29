package com.bd2monitor

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bd2monitor.databinding.ActivityDoctorDashboardBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class DoctorDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorDashboardBinding
    private lateinit var db: AppDatabase

    // ← ضع مفتاح Gemini هنا
    private val GEMINI_API_KEY = "YOUR_GEMINI_API_KEY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        binding.btnLogout.setOnClickListener { finish() }
        binding.btnGenerateReport.setOnClickListener { generateSoapReport() }
        binding.btnExportCsv.setOnClickListener { exportCsv() }
        binding.btnBack.setOnClickListener { finish() }

        loadDashboard()
    }

    private fun loadDashboard() {
        lifecycleScope.launch {
            val records = withContext(Dispatchers.IO) {
                db.recordDao().getAll()
            }
            val profile = withContext(Dispatchers.IO) {
                db.patientProfileDao().getProfileDirect()
            }

            showPatientInfo(profile)
            showAlerts(records)
            showStats(records)
            setupMoodEnergyChart(records.take(30).reversed())
            setupSleepChart(records.take(30).reversed())
        }
    }

    private fun showPatientInfo(profile: PatientProfile?) {
        if (profile == null) {
            binding.txtPatientInfo.text = "لم يُسجَّل ملف مريض بعد"
            return
        }
        binding.txtPatientInfo.text = """
            👤 ${profile.firstName} ${profile.lastName}
            🎂 العمر: ${profile.age} سنة
            📅 تاريخ التشخيص: ${profile.diagnosisDate}
            📞 الهاتف: ${profile.phone}
            💼 ${profile.job}
        """.trimIndent()
    }

    private fun showAlerts(records: List<DailyRecord>) {
        val alerts = StringBuilder()
        val last7 = records.take(7)

        if (last7.isEmpty()) {
            binding.txtAlerts.text = "لا توجد بيانات كافية للتحليل"
            return
        }

        // تحذير هوس: مزاج > 7 لأكثر من 3 أيام
        val highMoodDays = last7.count { it.mood >= 8 }
        if (highMoodDays >= 3) {
            alerts.appendLine("🔴 تحذير: مزاج مرتفع (≥8) لـ $highMoodDays أيام متتالية — خطر هوس")
        }

        // تحذير اكتئاب: مزاج < 3 لأكثر من 3 أيام
        val lowMoodDays = last7.count { it.mood <= 3 }
        if (lowMoodDays >= 3) {
            alerts.appendLine("🔴 تحذير: مزاج منخفض (≤3) لـ $lowMoodDays أيام — خطر اكتئاب")
        }

        // تحذير نوم: أقل من 4 ساعات
        val poorSleepDays = last7.count { it.sleepHours < 4 }
        if (poorSleepDays >= 2) {
            alerts.appendLine("🟠 تحذير: نوم < 4 ساعات لـ $poorSleepDays أيام")
        }

        // تحذير عدم الانتظام في الدواء
        val missedMeds = last7.count { !it.medicationTaken }
        if (missedMeds >= 2) {
            alerts.appendLine("🟡 تحذير: فوّت الدواء $missedMeds أيام من آخر 7")
        }

        // طاقة مرتفعة جداً
        val highEnergyDays = last7.count { it.energy >= 8 }
        if (highEnergyDays >= 3) {
            alerts.appendLine("🟠 تحذير: طاقة مفرطة (≥8) لـ $highEnergyDays أيام")
        }

        if (alerts.isEmpty()) {
            binding.txtAlerts.text = "✅ لا توجد مؤشرات تحذيرية — الحالة مستقرة"
            binding.txtAlerts.setTextColor(Color.parseColor("#00f5a0"))
        } else {
            binding.txtAlerts.text = alerts.toString().trim()
            binding.txtAlerts.setTextColor(Color.parseColor("#ffd60a"))
        }
    }

    private fun showStats(records: List<DailyRecord>) {
        if (records.isEmpty()) return
        val last30 = records.take(30)

        val avgMood = last30.map { it.mood }.average()
        val avgSleep = last30.map { it.sleepHours }.average()
        val medCompliance = last30.count { it.medicationTaken } * 100 / last30.size

        binding.txtAvgMood.text = String.format("%.1f", avgMood)
        binding.txtAvgSleep.text = String.format("%.1f", avgSleep)
        binding.txtMedCompliance.text = "$medCompliance%"
    }

    private fun setupMoodEnergyChart(records: List<DailyRecord>) {
        if (records.isEmpty()) return

        val moodEntries = records.mapIndexed { i, r -> Entry(i.toFloat(), r.mood.toFloat()) }
        val energyEntries = records.mapIndexed { i, r -> Entry(i.toFloat(), r.energy.toFloat()) }

        val moodDataSet = LineDataSet(moodEntries, "المزاج").apply {
            color = Color.parseColor("#00b4d8")
            setCircleColor(Color.parseColor("#00b4d8"))
            lineWidth = 2f
            circleRadius = 3f
            setDrawValues(false)
        }

        val energyDataSet = LineDataSet(energyEntries, "الطاقة").apply {
            color = Color.parseColor("#ffd60a")
            setCircleColor(Color.parseColor("#ffd60a"))
            lineWidth = 2f
            circleRadius = 3f
            setDrawValues(false)
        }

        val labels = records.map { it.date.takeLast(5) }

        binding.chartMoodEnergy.apply {
            data = LineData(moodDataSet, energyDataSet)
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.parseColor("#5b8db8")
                granularity = 1f
                setDrawGridLines(false)
            }
            axisLeft.apply {
                textColor = Color.parseColor("#5b8db8")
                axisMinimum = 0f
                axisMaximum = 10f
                setDrawGridLines(true)
                gridColor = Color.parseColor("#1a3a5c")
            }
            axisRight.isEnabled = false
            legend.textColor = Color.WHITE
            description.isEnabled = false
            setBackgroundColor(Color.TRANSPARENT)
            animateX(800)
            invalidate()
        }
    }

    private fun setupSleepChart(records: List<DailyRecord>) {
        if (records.isEmpty()) return

        val sleepEntries = records.mapIndexed { i, r ->
            BarEntry(i.toFloat(), r.sleepHours)
        }

        val sleepDataSet = BarDataSet(sleepEntries, "النوم (ساعات)").apply {
            color = Color.parseColor("#00f5a0")
            setDrawValues(false)
        }

        val labels = records.map { it.date.takeLast(5) }

        binding.chartSleep.apply {
            data = BarData(sleepDataSet)
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.parseColor("#5b8db8")
                granularity = 1f
                setDrawGridLines(false)
            }
            axisLeft.apply {
                textColor = Color.parseColor("#5b8db8")
                axisMinimum = 0f
                axisMaximum = 24f
                setDrawGridLines(true)
                gridColor = Color.parseColor("#1a3a5c")
            }
            axisRight.isEnabled = false
            legend.textColor = Color.WHITE
            description.isEnabled = false
            setBackgroundColor(Color.TRANSPARENT)
            animateY(800)
            invalidate()
        }
    }

    private fun generateSoapReport() {
        binding.layoutReport.visibility = View.VISIBLE
        binding.progressReport.visibility = View.VISIBLE
        binding.txtReport.text = ""
        binding.btnGenerateReport.isEnabled = false

        lifecycleScope.launch {
            val records = withContext(Dispatchers.IO) {
                db.recordDao().getAll().take(7)
            }
            val profile = withContext(Dispatchers.IO) {
                db.patientProfileDao().getProfileDirect()
            }

            if (records.isEmpty()) {
                binding.progressReport.visibility = View.GONE
                binding.btnGenerateReport.isEnabled = true
                Toast.makeText(this@DoctorDashboardActivity,
                    "لا توجد بيانات كافية", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val patientInfo = if (profile != null)
                "المريض: ${profile.firstName} ${profile.lastName}, العمر: ${profile.age} سنة"
            else "مريض غير معرّف"

            val recordsSummary = records.joinToString("\n") { r ->
                "- ${r.date}: مزاج ${r.mood}/10, طاقة ${r.energy}/10, " +
                "نوم ${r.sleepHours}س, دواء: ${if (r.medicationTaken) "نعم" else "لا"}, " +
                "ملاحظة: ${r.note.ifEmpty { "—" }}"
            }

            val prompt = """
                أنت طبيب نفسي متخصص في اضطراب ثنائي القطب (Bipolar Disorder).
                اكتب تقريراً طبياً بصيغة SOAP باللغتين العربية والفرنسية بناءً على هذه البيانات:
                
                $patientInfo
                
                البيانات (آخر 7 أيام):
                $recordsSummary
                
                الهيكل المطلوب:
                ═══ RAPPORT SOAP / تقرير SOAP ═══
                
                S — Subjective / ذاتي:
                [ما يُبلّغ عنه المريض]
                
                O — Objective / موضوعي:
                [القيم الرقمية والأنماط]
                
                A — Assessment / التقييم:
                [التحليل السريري وعلاقته بثنائي القطب]
                
                P — Plan / الخطة:
                [التوصيات العلاجية والمتابعة]
                
                ══════════════════════════════════
                التقرير للاستخدام البحثي الطبي المتخصص.
            """.trimIndent()

            val report = withContext(Dispatchers.IO) { callGemini(prompt) }

            binding.progressReport.visibility = View.GONE
            binding.btnGenerateReport.isEnabled = true

            if (report != null) {
                val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                binding.txtReport.text = "📋 $date\n\n$report"
            } else {
                Toast.makeText(this@DoctorDashboardActivity,
                    "فشل توليد التقرير — تحقق من مفتاح Gemini",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun exportCsv() {
        lifecycleScope.launch {
            val records = withContext(Dispatchers.IO) {
                db.recordDao().getAll()
            }

            if (records.isEmpty()) {
                Toast.makeText(this@DoctorDashboardActivity,
                    "لا توجد بيانات للتصدير", Toast.LENGTH_SHORT).show()
                return@launch
            }

            withContext(Dispatchers.IO) {
                val fileName = "BD2_Export_${
                    SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                }.csv"
                val file = File(getExternalFilesDir(null), fileName)

                file.writeText(buildString {
                    appendLine("date,mood,energy,sleep_hours,medication_taken,steps,note")
                    records.forEach { r ->
                        appendLine("${r.date},${r.mood},${r.energy},${r.sleepHours}," +
                            "${r.medicationTaken},${r.steps},\"${r.note}\"")
                    }
                })
            }

            Toast.makeText(this@DoctorDashboardActivity,
                "✅ تم التصدير في مجلد التطبيق / Exporté avec succès",
                Toast.LENGTH_LONG).show()
        }
    }

    private fun callGemini(prompt: String): String? {
        return try {
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$GEMINI_API_KEY")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 30000
            conn.readTimeout = 30000

            val body = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
            }.toString()

            OutputStreamWriter(conn.outputStream).use { it.write(body) }

            val response = conn.inputStream.bufferedReader().readText()
            JSONObject(response)
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        } catch (e: Exception) {
            null
        }
    }
}
