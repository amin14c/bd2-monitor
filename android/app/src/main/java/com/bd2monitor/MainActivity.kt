package com.bd2monitor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bd2monitor.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var sensorManager: SensorManager
    private var stepCount = 0

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) registerStepSensor()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        setupSliders()
        setupSaveButton()
        loadTodayRecord()
        checkAndRequestSensorPermission()
        showTodayDate()
    }
private fun showTodayDate() {
        val fmt = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar"))
        binding.txtDate.text = fmt.format(Date())
    }

    private fun setupSliders() {
        binding.sliderMood.addOnChangeListener { _, value, _ ->
            binding.txtMoodVal.text = value.toInt().toString()
        }
        binding.sliderEnergy.addOnChangeListener { _, value, _ ->
            binding.txtEnergyVal.text = value.toInt().toString()
        }
        binding.sliderSleep.addOnChangeListener { _, value, _ ->
            binding.txtSleepVal.text = String.format("%.1f", value)
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener
      }

    private fun saveRecord() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val record = DailyRecord(
            date = today,
            mood = binding.sliderMood.value.toInt(),
            energy = binding.sliderEnergy.value.toInt(),
            sleepHours = binding.sliderSleep.value,
            medicationTaken = binding.checkMedication.isChecked,
            steps = stepCount,
            note = binding.editNote.text.toString().trim()
        )
        lifecycleScope.launch {
            db.recordDao().insert(record)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "✅ تم حفظ البيانات", Toast.LENGTH_SHORT).show()
                binding.btnSave.text = "✅ تم الحفظ"
            }
        }
    }

}

    private fun checkAndRequestSensorPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED -> registerStepSensor()
                else -> requestPermission.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        } else {
            registerStepSensor()
        }
    }

    private fun registerStepSensor() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            binding.txtSteps.text = "👟 حساس الخطوات غير متوفر"
        }
    }
override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            stepCount = event.values[0].toInt()
            binding.txtSteps.text = "👟 $stepCount خطوة اليوم"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        if (::sensorManager.isInitialized) {
            val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            stepSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (::sensorManager.isInitialized) {
            sensorManager.unregisterListener(this)
        }
}
}
    
