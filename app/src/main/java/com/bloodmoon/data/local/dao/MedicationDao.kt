package com.bloodmoon.data.local.dao

import androidx.room.*
import com.bloodmoon.data.local.entities.Medication
import com.bloodmoon.data.local.entities.MedicationLog
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveMedications(): Flow<List<Medication>>

    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun getAllMedications(): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: Long): Medication?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication): Long

    @Update
    suspend fun updateMedication(medication: Medication)

    @Delete
    suspend fun deleteMedication(medication: Medication)

    @Query("UPDATE medications SET isActive = :isActive WHERE id = :id")
    suspend fun toggleMedicationActive(id: Long, isActive: Boolean)

    // Medication logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationLog(log: MedicationLog): Long

    @Query("SELECT * FROM medication_logs WHERE medicationId = :medicationId ORDER BY takenAt DESC")
    fun getMedicationLogs(medicationId: Long): Flow<List<MedicationLog>>

    @Query("SELECT * FROM medication_logs WHERE medicationId = :medicationId AND takenAt >= :startTime AND takenAt <= :endTime")
    suspend fun getMedicationLogsInRange(medicationId: Long, startTime: Long, endTime: Long): List<MedicationLog>

    @Query("DELETE FROM medication_logs WHERE id = :id")
    suspend fun deleteMedicationLog(id: Long)
}
