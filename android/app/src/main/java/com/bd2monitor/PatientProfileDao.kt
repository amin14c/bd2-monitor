package com.bd2monitor

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PatientProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: PatientProfile)

    @Update
    suspend fun update(profile: PatientProfile)

    @Query("SELECT * FROM patient_profile WHERE id = 1")
    fun getProfile(): LiveData<PatientProfile?>

    @Query("SELECT * FROM patient_profile WHERE id = 1")
    suspend fun getProfileOnce(): PatientProfile?
}
