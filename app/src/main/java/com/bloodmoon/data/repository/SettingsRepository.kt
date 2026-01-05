package com.bloodmoon.data.repository

import com.bloodmoon.data.local.dao.AppSettingsDao
import com.bloodmoon.data.local.entities.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

class SettingsRepository(private val settingsDao: AppSettingsDao) {

    fun getSettings(): Flow<AppSettings> = settingsDao.getSettings().map { it ?: AppSettings() }

    suspend fun getSettingsOnce(): AppSettings = settingsDao.getSettingsOnce() ?: AppSettings()

    suspend fun updateSettings(settings: AppSettings) {
        settingsDao.upsert(settings)
    }

    suspend fun setPinCode(pin: String) {
        val current = getSettingsOnce()
        val hashedPin = hashPin(pin)
        settingsDao.upsert(current.copy(isPinEnabled = true, pinHash = hashedPin))
    }

    suspend fun verifyPin(pin: String): Boolean {
        val settings = getSettingsOnce()
        return settings.pinHash == hashPin(pin)
    }

    suspend fun disablePin() {
        val current = getSettingsOnce()
        settingsDao.upsert(current.copy(isPinEnabled = false, pinHash = null))
    }

    suspend fun enableBiometric() {
        val current = getSettingsOnce()
        settingsDao.upsert(current.copy(isBiometricEnabled = true))
    }

    suspend fun disableBiometric() {
        val current = getSettingsOnce()
        settingsDao.upsert(current.copy(isBiometricEnabled = false))
    }

    suspend fun unlockMetalMode() {
        val current = getSettingsOnce()
        settingsDao.upsert(current.copy(isMetalModeUnlocked = true, isMetalModeEnabled = true))
    }

    suspend fun toggleMetalMode(enabled: Boolean) {
        val current = getSettingsOnce()
        if (current.isMetalModeUnlocked) {
            settingsDao.upsert(current.copy(isMetalModeEnabled = enabled))
        }
    }

    suspend fun incrementMoonyTapCount(): Int {
        val current = getSettingsOnce()
        val newCount = current.moonyTapCount + 1
        settingsDao.upsert(current.copy(moonyTapCount = newCount))

        // Unlock metal mode at 6 taps
        if (newCount >= 6 && !current.isMetalModeUnlocked) {
            unlockMetalMode()
        }

        return newCount
    }

    suspend fun resetMoonyTapCount() {
        val current = getSettingsOnce()
        settingsDao.upsert(current.copy(moonyTapCount = 0))
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}
