package com.bd2monitor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecordAdapter(private val records: List<DailyRecord>) :
    RecyclerView.Adapter<RecordAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtDate: TextView = view.findViewById(R.id.txtDate)
        val txtMood: TextView = view.findViewById(R.id.txtMood)
        val txtEnergy: TextView = view.findViewById(R.id.txtEnergy)
        val txtSleep: TextView = view.findViewById(R.id.txtSleep)
        val txtMedication: TextView = view.findViewById(R.id.txtMedication)
        val txtNote: TextView = view.findViewById(R.id.txtNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = records[position]

        holder.txtDate.text = "📅 ${record.date}"
        holder.txtMood.text = "😊 ${record.mood}/10"
        holder.txtEnergy.text = "⚡ ${record.energy}/10"
        holder.txtSleep.text = "🌙 ${record.sleepHours}س"
        holder.txtMedication.text = if (record.medicationTaken) "💊 ✅" else "💊 ❌"

        if (record.note.isNotEmpty()) {
            holder.txtNote.visibility = View.VISIBLE
            holder.txtNote.text = "📝 ${record.note}"
        } else {
            holder.txtNote.visibility = View.GONE
        }
    }

    override fun getItemCount() = records.size
    }
