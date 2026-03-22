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
import android.widget.SeekBar
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
        binding.sliderMood.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.txtMoodVal.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.sliderEnergy.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.txtEnergyVal.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.sliderSleep.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val hours = progress / 2.0f
                binding.txtSleepVal.text = String.format("%.1f", hours)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener { saveRecord() }
    }

    private fun saveRecord() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val record = DailyRecord(
            date = today,
            mood = binding.sliderMood.progress,
            energy = binding.sliderEnergy.progress,
            sleepHours = binding.sliderSleep.progress / 2.0f,
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

    private fun loadTodayRecord() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        lifecycleScope.launch {
            val existing = db.recordDao().getByDate(today)
            existing?.let { rec ->
                runOnUiThread {
                    binding.sliderMood.progress = rec.mood
                    binding.sliderEnergy.progress = rec.energy
                    binding.sliderSleep.progress = (rec.sleepHours * 2).toInt()
                    binding.checkMedication.isChecked = rec.medicationTaken
                    binding.editNote.setText(rec.note)
                    binding.btnSave.text = "🔄 تحديث بيانات اليوم"
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
            binding.txtSteps.text = "حساس الخطوات غير متوفر"
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
