package com.bd2monitor

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bd2monitor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        // الزر لفتح الملف الشخصي (يجب أن يكون موجوداً في التخطيط)
        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // عرض اسم المريض إذا كان TextView موجوداً، وإلا يتم تجاهله
        try {
            db.patientProfileDao().getProfile().observe(this) { profile ->
                val nameText = if (profile != null) "${profile.firstName} ${profile.lastName}" else "غير مسجل"
                binding.textProfileName?.text = nameText
            }
        } catch (e: Exception) {
            // TextView غير موجود – لا مشكلة
        }

        // مثال على زر إضافة سجل (إذا كان موجوداً)
        try {
            binding.btnAddRecord.setOnClickListener {
                Toast.makeText(this, "سيتم إضافة هذه الميزة قريباً", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // الزر غير موجود – لا مشكلة
        }
    }
}
