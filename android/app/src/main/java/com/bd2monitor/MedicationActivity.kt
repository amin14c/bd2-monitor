package com.bd2monitor

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bd2monitor.databinding.ActivityMedicationBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class MedicationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicationBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        binding.btnSaveMed.setOnClickListener { saveMedication() }
        binding.btnBack.setOnClickListener { finish() }

        observeMedications()
    }

    private fun saveMedication() {
        val nameFr = binding.editMedNameFr.text.toString().trim()
        val nameAr = binding.editMedNameAr.text.toString().trim()
        val dosage = binding.editDosage.text.toString().trim()
        val hour = binding.timePicker.hour
        val minute = binding.timePicker.minute

        if (nameFr.isEmpty() && nameAr.isEmpty()) {
            Toast.makeText(this, "أدخل اسم الدواء / Entrez le nom", Toast.LENGTH_SHORT).show()
            return
        }

        val medication = Medication(
            nameFr = nameFr,
            nameAr = nameAr,
            dosage = dosage,
            timeHour = hour,
            timeMinute = minute
        )

        lifecycleScope.launch {
            db.medicationDao().insert(medication)
            scheduleAlarm(medication)
            runOnUiThread {
                Toast.makeText(this@MedicationActivity,
                    "✅ تم الحفظ / Enregistré", Toast.LENGTH_SHORT).show()
                clearForm()
            }
        }
    }

    private fun scheduleAlarm(medication: Medication) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, MedicationReceiver::class.java).apply {
            putExtra("medNameAr", medication.nameAr)
            putExtra("medNameFr", medication.nameFr)
            putExtra("dosage", medication.dosage)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, medication.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, medication.timeHour)
            set(Calendar.MINUTE, medication.timeMinute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun observeMedications() {
        db.medicationDao().getAllActive().observe(this) { list ->
            val text = list.joinToString("\n") { med ->
                "💊 ${med.nameAr} / ${med.nameFr} — ${med.dosage} — " +
                String.format("%02d:%02d", med.timeHour, med.timeMinute)
            }
            binding.txtMedList.text = text.ifEmpty {
                "لا توجد أدوية مضافة\nAucun médicament ajouté"
            }
        }
    }

    private fun clearForm() {
        binding.editMedNameFr.text?.clear()
        binding.editMedNameAr.text?.clear()
        binding.editDosage.text?.clear()
    }
}
