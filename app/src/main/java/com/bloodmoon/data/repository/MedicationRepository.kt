package com.bloodmoon.data.repository

import com.bloodmoon.data.local.dao.MedicationDao
import com.bloodmoon.data.local.entities.Medication
import com.bloodmoon.data.local.entities.MedicationLog
import kotlinx.coroutines.flow.Flow

class MedicationRepository(private val medicationDao: MedicationDao) {

    fun getAllActiveMedications(): Flow<List<Medication>> {
        return medicationDao.getAllActiveMedications()
    }

    fun getAllMedications(): Flow<List<Medication>> {
        return medicationDao.getAllMedications()
    }

    suspend fun getMedicationById(id: Long): Medication? {
        return medicationDao.getMedicationById(id)
    }

    suspend fun insertMedication(medication: Medication): Long {
        return medicationDao.insertMedication(medication)
    }

    suspend fun updateMedication(medication: Medication) {
        medicationDao.updateMedication(medication)
    }

    suspend fun deleteMedication(medication: Medication) {
        medicationDao.deleteMedication(medication)
    }

    suspend fun toggleMedicationActive(id: Long, isActive: Boolean) {
        medicationDao.toggleMedicationActive(id, isActive)
    }

    // Medication logs
    suspend fun logMedicationTaken(medicationId: Long, notes: String? = null): Long {
        return medicationDao.insertMedicationLog(
            MedicationLog(
                medicationId = medicationId,
                takenAt = System.currentTimeMillis(),
                skipped = false,
                notes = notes
            )
        )
    }

    suspend fun logMedicationSkipped(medicationId: Long, notes: String? = null): Long {
        return medicationDao.insertMedicationLog(
            MedicationLog(
                medicationId = medicationId,
                takenAt = System.currentTimeMillis(),
                skipped = true,
                notes = notes
            )
        )
    }

    fun getMedicationLogs(medicationId: Long): Flow<List<MedicationLog>> {
        return medicationDao.getMedicationLogs(medicationId)
    }

    suspend fun getMedicationLogsInRange(medicationId: Long, startTime: Long, endTime: Long): List<MedicationLog> {
        return medicationDao.getMedicationLogsInRange(medicationId, startTime, endTime)
    }

    suspend fun deleteMedicationLog(id: Long) {
        medicationDao.deleteMedicationLog(id)
    }
}
