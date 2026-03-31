package com.bd2monitor

import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bd2monitor.databinding.ActivityMadrsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MadrsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMadrsBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMadrsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        setupSliders()
        binding.btnSaveMadrs.setOnClickListener { saveAssessment() }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupSliders() {
        val sliders = listOf(
            binding.seekMadrs1 to binding.txtMadrs1Val,
            binding.seekMadrs2 to binding.txtMadrs2Val,
            binding.seekMadrs3 to binding.txtMadrs3Val,
            binding.seekMadrs4 to binding.txtMadrs4Val,
            binding.seekMadrs5 to binding.txtMadrs5Val,
            binding.seekMadrs6 to binding.txtMadrs6Val,
            binding.seekMadrs7 to binding.txtMadrs7Val
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
        val total = binding.seekMadrs1.progress +
                binding.seekMadrs2.progress +
                binding.seekMadrs3.progress +
                binding.seekMadrs4.progress +
                binding.seekMadrs5.progress +
                binding.seekMadrs6.progress +
                binding.seekMadrs7.progress

        binding.txtMadrsTotal.text = "$total / 28"

        val (level, color) = when {
            total <= 7  -> "🟢 طبيعي / Normal" to "#00f5a0"
            total <= 14 -> "🟡 اكتئاب خفيف / Dépression légère" to "#ffd60a"
            total <= 21 -> "🟠 اكتئاب متوسط / Dépression modérée" to "#ff9f43"
            else        -> "🔴 اكتئاب حاد / Dépression sévère" to "#ff6b6b"
        }

        binding.txtMadrsLevel.text = level
        binding.txtMadrsLevel.setTextColor(android.graphics.Color.parseColor(color))
        binding.txtMadrsTotal.setTextColor(android.graphics.Color.parseColor(color))
    }

    private fun saveAssessment() {
        val total = binding.seekMadrs1.progress +
                binding.seekMadrs2.progress +
                binding.seekMadrs3.progress +
                binding.seekMadrs4.progress +
                binding.seekMadrs5.progress +
                binding.seekMadrs6.progress +
                binding.seekMadrs7.progress

        // تنبيه إذا كان السؤال 7 > 0
        if (binding.seekMadrs7.progress > 0) {
            android.app.AlertDialog.Builder(this)
                .setTitle("⚠️ تنبيه مهم")
                .setMessage("لاحظنا وجود أفكار صعبة. يُنصح بالتواصل مع طبيبك المعالج فوراً.")
                .setPositiveButton("حسناً") { _, _ -> proceedSave(total) }
                .show()
        } else {
            proceedSave(total)
        }
    }

    private fun proceedSave(total: Int) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val assessment = AssessmentRecord(
            date = today,
            type = "MADRS",
            madrs1 = binding.seekMadrs1.progress,
            madrs2 = binding.seekMadrs2.progress,
            madrs3 = binding.seekMadrs3.progress,
            madrs4 = binding.seekMadrs4.progress,
            madrs5 = binding.seekMadrs5.progress,
            madrs6 = binding.seekMadrs6.progress,
            madrs7 = binding.seekMadrs7.progress,
            madrsTotal = total
        )

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                db.assessmentDao().insert(assessment)
            }
            Toast.makeText(
                this@MadrsActivity,
                "✅ تم حفظ تقييم MADRS / Enregistré",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }
}
