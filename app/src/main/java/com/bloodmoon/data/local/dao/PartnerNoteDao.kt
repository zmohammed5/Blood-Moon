package com.bloodmoon.data.local.dao

import androidx.room.*
import com.bloodmoon.data.local.entities.PartnerNote
import kotlinx.coroutines.flow.Flow

@Dao
interface PartnerNoteDao {
    @Query("SELECT * FROM partner_notes WHERE isEnabled = 1 ORDER BY createdAt DESC")
    fun getAllEnabledNotes(): Flow<List<PartnerNote>>

    @Query("SELECT * FROM partner_notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<PartnerNote>>

    @Query("SELECT * FROM partner_notes WHERE isEnabled = 1 ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomNote(): PartnerNote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: PartnerNote): Long

    @Update
    suspend fun update(note: PartnerNote)

    @Delete
    suspend fun delete(note: PartnerNote)

    @Query("DELETE FROM partner_notes")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM partner_notes WHERE isEnabled = 1")
    suspend fun getEnabledNoteCount(): Int
}
