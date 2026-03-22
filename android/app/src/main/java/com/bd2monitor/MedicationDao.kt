package com.bd2monitor

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MedicationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: Medication)

    @Update
    suspend fun update(medication: Medication)

    @Delete
    suspend fun delete(medication: Medication)

    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY timeHour, timeMinute")
    fun getAllActive(): LiveData<List<Medication>>

    @Query("SELECT * FROM medications ORDER BY timeHour, timeMinute")
    suspend fun getAll(): List<Medication>
}
