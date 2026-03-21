package com.bd2monitor

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_records")
data class DailyRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val date: String,
    val mood: Int,
    val energy: Int,
    val sleepHours: Float,
    val medicationTaken: Boolean,
    val steps: Int = 0,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
