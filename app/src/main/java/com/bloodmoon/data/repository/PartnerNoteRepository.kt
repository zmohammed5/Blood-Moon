package com.bloodmoon.data.repository

import com.bloodmoon.data.local.dao.PartnerNoteDao
import com.bloodmoon.data.local.entities.PartnerNote
import kotlinx.coroutines.flow.Flow

class PartnerNoteRepository(private val partnerNoteDao: PartnerNoteDao) {

    fun getAllNotes(): Flow<List<PartnerNote>> = partnerNoteDao.getAllNotes()

    fun getAllEnabledNotes(): Flow<List<PartnerNote>> = partnerNoteDao.getAllEnabledNotes()

    suspend fun getRandomNote(): PartnerNote? = partnerNoteDao.getRandomNote()

    suspend fun insertNote(message: String): Long {
        val note = PartnerNote(message = message)
        return partnerNoteDao.insert(note)
    }

    suspend fun updateNote(note: PartnerNote) = partnerNoteDao.update(note)

    suspend fun deleteNote(note: PartnerNote) = partnerNoteDao.delete(note)

    suspend fun getEnabledNoteCount(): Int = partnerNoteDao.getEnabledNoteCount()
}
