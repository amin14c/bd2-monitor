package com.bd2monitor

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nameFr: String,
    val nameAr: String,
    val dosage: String,
    val timeHour: Int,
    val timeMinute: Int,
    val isActive: Boolean = true
)
