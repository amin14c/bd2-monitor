
package com.bd2monitor

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patient_profiles")
data class PatientProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val firstName: String,      // الاسم
    val lastName: String,       // اللقب
    val job: String,            // الوظيفة
    val age: Int,               // السن
    val diagnosisDate: String,  // تاريخ التشخيص (مثلاً "2025-03-25")
    val phone: String           // رقم الهاتف
)
