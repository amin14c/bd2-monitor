package com.bd2monitor

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RecordDao {
    @Insert
    suspend fun insert(record: DailyRecord)

    @Query("SELECT * FROM daily_records ORDER BY date DESC")
    suspend fun getAll(): List<DailyRecord>
}
