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
        val profile = PatientProfile(
            fullNameAr = binding.editNameAr.text.toString().trim(),
            fullNameFr = binding.editNameFr.text.toString().trim(),
            birthDate = binding.editBirthDate.text.toString().trim(),
            diagnosis = binding.editDiagnosis.text.toString().trim(),
            doctorName = binding.editDoctorName.text.toString().trim(),
            emergencyContact = binding.editEmergency.text.toString().trim(),
            notes = binding.editProfileNotes.text.toString().trim()
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
                binding.editNameAr.setText(it.fullNameAr)
                binding.editNameFr.setText(it.fullNameFr)
                binding.editBirthDate.setText(it.birthDate)
                binding.editDiagnosis.setText(it.diagnosis)
                binding.editDoctorName.setText(it.doctorName)
                binding.editEmergency.setText(it.emergencyContact)
                binding.editProfileNotes.setText(it.notes)
            }
        }
    }
}
