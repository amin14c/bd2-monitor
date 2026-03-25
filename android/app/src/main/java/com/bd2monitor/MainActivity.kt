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

        // زر الانتقال إلى صفحة الملف الشخصي (يجب أن يكون موجوداً في التخطيط)
        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // إذا كان لديك زر إضافة سجل في التخطيط، قم بتفعيله
        // لكن لتجنب الأخطاء، نتحقق من وجوده أولاً
        try {
            binding.btnAddRecord.setOnClickListener {
                Toast.makeText(this, "سيتم إضافة هذه الميزة قريباً", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // الزر غير موجود، لا مشكلة
        }

        // عرض اسم المريض إذا كان TextView موجوداً
        try {
            db.patientProfileDao().getProfile().observe(this) { profile ->
                if (profile != null) {
                    binding.textProfileName?.text = "${profile.firstName} ${profile.lastName}"
                } else {
                    binding.textProfileName?.text = "غير مسجل"
                }
            }
        } catch (e: Exception) {
            // TextView غير موجود، نتجاهل عرض الاسم
        }
    }
}
