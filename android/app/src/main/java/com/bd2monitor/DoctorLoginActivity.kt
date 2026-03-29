package com.bd2monitor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bd2monitor.databinding.ActivityDoctorLoginBinding

class DoctorLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorLoginBinding
    private val enteredPin = StringBuilder()
    private val DEFAULT_PIN = "1234"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
    }

    private fun setupButtons() {
        val buttons = mapOf(
            binding.btn0 to "0", binding.btn1 to "1", binding.btn2 to "2",
            binding.btn3 to "3", binding.btn4 to "4", binding.btn5 to "5",
            binding.btn6 to "6", binding.btn7 to "7", binding.btn8 to "8",
            binding.btn9 to "9"
        )

        buttons.forEach { (button, digit) ->
            button.setOnClickListener {
                if (enteredPin.length < 4) {
                    enteredPin.append(digit)
                    updateDots()
                }
            }
        }

        binding.btnClear.setOnClickListener {
            if (enteredPin.isNotEmpty()) {
                enteredPin.deleteCharAt(enteredPin.length - 1)
                updateDots()
            }
        }

        binding.btnOk.setOnClickListener {
            checkPin()
        }
    }

    private fun updateDots() {
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3, binding.dot4)
        dots.forEachIndexed { index, dot ->
            dot.setBackgroundResource(
                if (index < enteredPin.length) R.drawable.pin_dot_filled
                else R.drawable.pin_dot_empty
            )
        }
    }

    private fun checkPin() {
        val savedPin = getSharedPreferences("doctor_prefs", Context.MODE_PRIVATE)
            .getString("pin", DEFAULT_PIN)

        if (enteredPin.toString() == savedPin) {
            startActivity(Intent(this, DoctorDashboardActivity::class.java))
            finish()
        } else {
            binding.txtMessage.visibility = View.VISIBLE
            binding.txtMessage.text = "❌ PIN خاطئ / PIN incorrect"
            enteredPin.clear()
            updateDots()
        }
    }
}
