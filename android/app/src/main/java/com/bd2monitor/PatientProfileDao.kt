package com.bd2monitor

import androidx.room.*

@Dao
interface PatientProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: PatientProfile)

    @Update
    suspend fun update(profile: PatientProfile)

    @Delete
    suspend fun delete(profile: PatientProfile)

    @Query("SELECT * FROM patient_profiles")
    suspend fun getAll(): List<PatientProfile>

    @Query("SELECT * FROM patient_profiles WHERE id = :id")
    suspend fun getById(id: Int): PatientProfile?
}
