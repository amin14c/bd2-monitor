package com.bd2monitor

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: DailyRecord)

    @Query("SELECT * FROM daily_records ORDER BY timestamp DESC")
    fun getAllRecords(): LiveData<List<DailyRecord>>

    @Query("SELECT * FROM daily_records WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): DailyRecord?

    @Query("SELECT * FROM daily_records ORDER BY timestamp DESC LIMIT 30")
    suspend fun getLast30(): List<DailyRecord>

    @Query("DELETE FROM daily_records")
    suspend fun deleteAll()
}
