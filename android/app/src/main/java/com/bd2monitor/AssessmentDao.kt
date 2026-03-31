package com.bd2monitor

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AssessmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assessment: AssessmentRecord)

    @Query("SELECT * FROM assessments ORDER BY date DESC")
    suspend fun getAll(): List<AssessmentRecord>

    @Query("SELECT * FROM assessments WHERE type = 'YMRS' ORDER BY date DESC")
    suspend fun getAllYmrs(): List<AssessmentRecord>

    @Query("SELECT * FROM assessments WHERE type = 'MADRS' ORDER BY date DESC")
    suspend fun getAllMadrs(): List<AssessmentRecord>

    @Query("SELECT * FROM assessments ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(): AssessmentRecord?

    @Query("SELECT * FROM assessments WHERE type = 'YMRS' ORDER BY date DESC LIMIT 1")
    suspend fun getLatestYmrs(): AssessmentRecord?

    @Query("SELECT * FROM assessments WHERE type = 'MADRS' ORDER BY date DESC LIMIT 1")
    suspend fun getLatestMadrs(): AssessmentRecord?

    @Query("SELECT * FROM assessments ORDER BY date DESC")
    fun getAllLive(): LiveData<List<AssessmentRecord>>
}
