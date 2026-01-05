package com.bloodmoon.data.repository

import com.bloodmoon.data.local.dao.PeriodLogDao
import com.bloodmoon.data.local.entities.PeriodLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class PeriodRepository(private val periodLogDao: PeriodLogDao) {

    fun getAllLogs(): Flow<List<PeriodLog>> = periodLogDao.getAllLogs()

    suspend fun getLogForDate(date: LocalDate): PeriodLog? =
        periodLogDao.getLogForDate(date)

    suspend fun getLogsInRange(startDate: LocalDate, endDate: LocalDate): List<PeriodLog> =
        periodLogDao.getLogsInRange(startDate, endDate)

    suspend fun getRecentPeriodStarts(limit: Int = 12): List<PeriodLog> =
        periodLogDao.getRecentPeriodStarts(limit)

    suspend fun insertLog(log: PeriodLog): Long =
        periodLogDao.insert(log)

    suspend fun updateLog(log: PeriodLog) =
        periodLogDao.update(log)

    suspend fun deleteLog(log: PeriodLog) =
        periodLogDao.delete(log)

    suspend fun getPeriodStartCount(): Int =
        periodLogDao.getPeriodStartCount()
}
