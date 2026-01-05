package com.bloodmoon.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "partner_notes")
data class PartnerNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val message: String,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
