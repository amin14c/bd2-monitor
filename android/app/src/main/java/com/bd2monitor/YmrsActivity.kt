package com.bd2monitor

import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bd2monitor.databinding.ActivityYmrsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class YmrsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityYmrsBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYmrsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        setupSliders()
        binding.btnSaveYmrs.setOnClickListener { saveAssessment() }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupSliders() {
        val sliders = listOf(
            binding.seekYmrs1 to binding.txtYmrs1Val,
            binding.seekYmrs2 to binding.txtYmrs2Val,
            binding.seekYmrs3 to binding.txtYmrs3Val,
            binding.seekYmrs4 to binding.txtYmrs4Val,
            binding.seekYmrs5 to binding.txtYmrs5Val,
            binding.seekYmrs6 to binding.txtYmrs6Val,
            binding.seekYmrs7 to binding.txtYmrs7Val
        )

        sliders.forEach { (seekBar, textView) ->
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    textView.text = progress.toString()
                    updateTotal()
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }
    }

    private fun updateTotal() {
        val total = binding.seekYmrs1.progress +
                binding.seekYmrs2.progress +
                binding.seekYmrs3.progress +
                binding.seekYmrs4.progress +
                binding.seekYmrs5.progress +
                binding.seekYmrs6.progress +
                binding.seekYmrs7.progress

        binding.txtYmrsTotal.text = "$total / 28"

        val (level, color) = when {
            total <= 7  -> "🟢 طبيعي / Normal" to "#00f5a0"
            total <= 14 -> "🟡 هوس خفيف / Manie légère" to "#ffd60a"
            total <= 21 -> "🟠 هوس متوسط / Manie modérée" to "#ff9f43"
            else        -> "🔴 هوس حاد / Manie sévère" to "#ff6b6b"
        }

        binding.txtYmrsLevel.text = level
        binding.txtYmrsLevel.setTextColor(android.graphics.Color.parseColor(color))
        binding.txtYmrsTotal.setTextColor(android.graphics.Color.parseColor(color))
    }

    private fun saveAssessment() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val total = binding.seekYmrs1.progress +
                binding.seekYmrs2.progress +
                binding.seekYmrs3.progress +
                binding.seekYmrs4.progress +
                binding.seekYmrs5.progress +
                binding.seekYmrs6.progress +
                binding.seekYmrs7.progress

        val assessment = AssessmentRecord(
            date = today,
            type = "YMRS",
            ymrs1 = binding.seekYmrs1.progress,
            ymrs2 = binding.seekYmrs2.progress,
            ymrs3 = binding.seekYmrs3.progress,
            ymrs4 = binding.seekYmrs4.progress,
            ymrs5 = binding.seekYmrs5.progress,
            ymrs6 = binding.seekYmrs6.progress,
            ymrs7 = binding.seekYmrs7.progress,
            ymrsTotal = total
        )

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                db.assessmentDao().insert(assessment)
            }
            Toast.makeText(
                this@YmrsActivity,
                "✅ تم حفظ تقييم YMRS / Enregistré",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }
}
