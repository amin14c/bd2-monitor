package com.bd2monitor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bd2monitor.databinding.ActivityHistoryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        binding.recyclerHistory.layoutManager = LinearLayoutManager(this)

        binding.btnBack.setOnClickListener { finish() }

        loadRecords()
    }

    private fun loadRecords() {
        lifecycleScope.launch {
            val records = withContext(Dispatchers.IO) {
                db.recordDao().getAll()
            }

            if (records.isEmpty()) {
                binding.txtEmpty.visibility = android.view.View.VISIBLE
                binding.recyclerHistory.visibility = android.view.View.GONE
            } else {
                binding.txtEmpty.visibility = android.view.View.GONE
                binding.recyclerHistory.visibility = android.view.View.VISIBLE
                binding.recyclerHistory.adapter = RecordAdapter(records)
            }
        }
    }
}
