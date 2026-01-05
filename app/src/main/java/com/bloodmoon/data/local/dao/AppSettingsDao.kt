package com.bloodmoon.data.local.dao

import androidx.room.*
import com.bloodmoon.data.local.entities.AppSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsOnce(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: AppSettings)

    @Update
    suspend fun update(settings: AppSettings)

    @Transaction
    suspend fun upsert(settings: AppSettings) {
        val existing = getSettingsOnce()
        if (existing == null) {
            insert(settings.copy(id = 1))
        } else {
            update(settings.copy(id = 1))
        }
    }
}
