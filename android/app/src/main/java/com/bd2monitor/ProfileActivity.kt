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
        // استخدام الحقول الموجودة في التخطيط (من activity_profile.xml)
        val firstName = binding.editNameAr.text.toString().trim()
        val lastName = binding.editNameFr.text.toString().trim()
        val job = binding.editDoctorName.text.toString().trim()   // الوظيفة
        val ageText = binding.editBirthDate.text.toString().trim()  // العمر (سلسلة)
        val diagnosisDate = binding.editDiagnosis.text.toString().trim()  // تاريخ التشخيص
        val phone = binding.editEmergency.text.toString().trim()  // رقم الهاتف
        val notes = binding.editProfileNotes.text.toString().trim() // ملاحظات إضافية (غير مستخدمة حالياً)

        if (firstName.isEmpty() || lastName.isEmpty() || ageText.isEmpty()) {
            Toast.makeText(this, "الرجاء إدخال الاسم واللقب والعمر", Toast.LENGTH_SHORT).show()
            return
        }

        val age = ageText.toIntOrNull()
        if (age == null) {
            Toast.makeText(this, "العمر يجب أن يكون رقماً", Toast.LENGTH_SHORT).show()
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
                binding.editNameAr.setText(it.firstName)
                binding.editNameFr.setText(it.lastName)
                binding.editDoctorName.setText(it.job)
                binding.editBirthDate.setText(it.age.toString())
                binding.editDiagnosis.setText(it.diagnosisDate)
                binding.editEmergency.setText(it.phone)
                // binding.editProfileNotes.setText(it.notes) // إذا أضفنا notes لاحقاً
            }
        }
    }
}
