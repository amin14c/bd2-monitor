package com.bd2monitor

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patient_profile")
data class PatientProfile(
    @PrimaryKey
    val id: Int = 1,
    val fullNameAr: String = "",
    val fullNameFr: String = "",
    val birthDate: String = "",
    val diagnosis: String = "",
    val doctorName: String = "",
    val emergencyContact: String = "",
    val notes: String = ""
