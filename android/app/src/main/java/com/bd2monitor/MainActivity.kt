package com.bd2monitor

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bd2monitor.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // تهيئة قاعدة البيانات على خيط خلفي
        lifecycleScope.launch(Dispatchers.IO) {
            db = AppDatabase.getDatabase(this@MainActivity)
        }

        // تحديث التاريخ
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        binding.txtDate.text = today

        // زر حفظ بيانات اليوم
        binding.btnSave.setOnClickListener { saveRecord() }

        // زر الأدوية
        binding.btnMedications.setOnClickListener {
            startActivity(Intent(this, MedicationActivity::class.java))
        }

        // زر الملف الشخصي
        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // تحديث قيم السلايدرات
        binding.sliderMood.setOnSeekBarChangeListener(object :
            android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                binding.txtMoodVal.text = progress.toString()
            }
            override fun onStartTrackingTouch(sb: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(sb: android.widget.SeekBar?) {}
        })

        binding.sliderEnergy.setOnSeekBarChangeListener(object :
            android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                binding.txtEnergyVal.text = progress.toString()
            }
            override fun onStartTrackingTouch(sb: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(sb: android.widget.SeekBar?) {}
        })

        binding.sliderSleep.setOnSeekBarChangeListener(object :
            android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                binding.txtSleepVal.text = progress.toString()
            }
            override fun onStartTrackingTouch(sb: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(sb: android.widget.SeekBar?) {}
        })
    }

    private fun saveRecord() {
        if (!::db.isInitialized) {
            Toast.makeText(this, "جارٍ التحضير، أعد المحاولة / Patientez...", Toast.LENGTH_SHORT).show()
            return
        }

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val record = DailyRecord(
            date = today,
            mood = binding.sliderMood.progress,
            energy = binding.sliderEnergy.progress,
            sleepHours = binding.sliderSleep.progress.toFloat(),
            medicationTaken = binding.checkMedication.isChecked,
            note = binding.editNote.text.toString().trim()
        )

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                db.recordDao().insert(record)
            }
            Toast.makeText(
                this@MainActivity,
                "✅ تم الحفظ / Enregistré",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
