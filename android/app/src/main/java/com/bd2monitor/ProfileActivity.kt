package com.bd2monitor

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bd2monitor.databinding.ActivityProfileBinding
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        binding.btnSaveProfile.setOnClickListener { saveProfile() }
        binding.btnBack.setOnClickListener { finish() }

        loadProfile()
    }

    private fun saveProfile() {
        val firstName = binding.editFirstName.text.toString().trim()
        val lastName = binding.editLastName.text.toString().trim()
        val job = binding.editJob.text.toString().trim()
        val ageText = binding.editAge.text.toString().trim()
        val diagnosisDate = binding.editDiagnosisDate.text.toString().trim()
        val phone = binding.editPhone.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty() || ageText.isEmpty()) {
            Toast.makeText(this, "الرجاء إدخال الاسم واللقب والسن", Toast.LENGTH_SHORT).show()
            return
        }

        val age = ageText.toIntOrNull() ?: run {
            Toast.makeText(this, "السن يجب أن يكون رقمًا", Toast.LENGTH_SHORT).show()
            return
        }

        val profile = PatientProfile(
            firstName = firstName,
            lastName = lastName,
            job = job,
            age = age,
            diagnosisDate = diagnosisDate,
            phone = phone
        )

        lifecycleScope.launch {
            db.patientProfileDao().insert(profile)
            runOnUiThread {
                Toast.makeText(this@ProfileActivity,
                    "✅ تم الحفظ / Enregistré", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadProfile() {
        db.patientProfileDao().getProfile().observe(this) { profile ->
            profile?.let {
                binding.editFirstName.setText(it.firstName)
                binding.editLastName.setText(it.lastName)
                binding.editJob.setText(it.job)
                binding.editAge.setText(it.age.toString())
                binding.editDiagnosisDate.setText(it.diagnosisDate)
                binding.editPhone.setText(it.phone)
            }
        }
    }
}
