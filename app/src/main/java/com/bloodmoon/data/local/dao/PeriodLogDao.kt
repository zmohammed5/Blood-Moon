package com.bloodmoon.data.local.dao

import androidx.room.*
import com.bloodmoon.data.local.entities.PeriodLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PeriodLogDao {
    @Query("SELECT * FROM period_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<PeriodLog>>

    @Query("SELECT * FROM period_logs WHERE date = :date LIMIT 1")
    suspend fun getLogForDate(date: LocalDate): PeriodLog?

    @Query("SELECT * FROM period_logs WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    suspend fun getLogsInRange(startDate: LocalDate, endDate: LocalDate): List<PeriodLog>

    @Query("SELECT * FROM period_logs WHERE isPeriodStart = 1 ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentPeriodStarts(limit: Int = 12): List<PeriodLog>

    @Query("SELECT * FROM period_logs WHERE date < :beforeDate ORDER BY date DESC LIMIT 1")
    suspend fun getLastPeriodBefore(beforeDate: LocalDate): PeriodLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: PeriodLog): Long

    @Update
    suspend fun update(log: PeriodLog)

    @Delete
    suspend fun delete(log: PeriodLog)

    @Query("DELETE FROM period_logs")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM period_logs WHERE isPeriodStart = 1")
    suspend fun getPeriodStartCount(): Int
}
