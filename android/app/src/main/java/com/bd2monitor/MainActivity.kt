package com.bd2monitor

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bd2monitor.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: DailyRecordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        setupRecyclerView()
        setupClickListeners()
        loadProfile()
        loadRecords()
    }

    private fun setupRecyclerView() {
        adapter = DailyRecordAdapter { record ->
            // عند النقر على سجل يمكنك إضافة تفاصيل
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnAddRecord.setOnClickListener {
            // فتح نشاط إضافة تسجيل جديد
        }
        binding.btnProfile.setOnClickListener {
            // فتح نشاط الملف الشخصي
            startActivity(android.content.Intent(this, ProfileActivity::class.java))
        }
    }

    private fun loadProfile() {
        db.patientProfileDao().getProfile().observe(this) { profile ->
            profile?.let {
                // عرض الاسم واللقب من PatientProfile
                binding.textProfileName.text = "${it.firstName} ${it.lastName}"
                // إذا كان لديك عناصر أخرى مثل الوظيفة أو العمر يمكن تعيينها
                // binding.textJob.text = it.job
                // binding.textAge.text = it.age.toString()
            } ?: run {
                binding.textProfileName.text = "غير مسجل"
            }
        }
    }

    private fun loadRecords() {
        lifecycleScope.launch {
            val records = db.recordDao().getAll()
            adapter.submitList(records)
        }
    }
}
