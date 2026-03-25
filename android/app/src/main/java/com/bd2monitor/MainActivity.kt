package com.bd2monitor

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bd2monitor.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        // زر الانتقال إلى صفحة الملف الشخصي
        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // زر لإضافة سجل (سيتم تنفيذه لاحقاً)
        binding.btnAddRecord.setOnClickListener {
            Toast.makeText(this, "سيتم إضافة هذه الميزة قريباً", Toast.LENGTH_SHORT).show()
        }

        // تحميل بيانات الملف الشخصي
        loadProfile()
    }

    private fun loadProfile() {
        db.patientProfileDao().getProfile().observe(this) { profile ->
            if (profile != null) {
                // إذا كان لديك TextView باسم textProfileName في التخطيط، استخدمه
                // وإلا استخدم أي TextView آخر أو أزل هذا السطر
                binding.textProfileName?.text = "${profile.firstName} ${profile.lastName}"
            } else {
                binding.textProfileName?.text = "غير مسجل"
            }
        }
    }
}
