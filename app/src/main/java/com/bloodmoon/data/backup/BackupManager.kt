package com.bloodmoon.data.backup

import android.content.Context
import android.net.Uri
import com.bloodmoon.data.local.BloodMoonDatabase
import com.bloodmoon.data.local.entities.AppSettings
import com.bloodmoon.data.local.entities.PartnerNote
import com.bloodmoon.data.local.entities.PeriodLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.time.Instant
import java.time.LocalDate
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class BackupManager(private val context: Context) {

    companion object {
        private const val BACKUP_VERSION = 1
        private const val ALGORITHM = "AES/CBC/PKCS5Padding"
        private const val KEY_FACTORY = "PBKDF2WithHmacSHA256"
        private const val ITERATION_COUNT = 10000
        private const val KEY_LENGTH = 256
    }

    /**
     * Export encrypted backup to URI
     */
    suspend fun exportBackup(
        uri: Uri,
        password: String,
        periodLogs: List<PeriodLog>,
        partnerNotes: List<PartnerNote>,
        settings: AppSettings
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val encryptedStream = createEncryptionStream(outputStream, password)
                val zipStream = ZipOutputStream(encryptedStream)

                // Create backup JSON
                val backupData = createBackupJson(periodLogs, partnerNotes, settings)

                // Add to zip
                zipStream.putNextEntry(ZipEntry("backup.json"))
                zipStream.write(backupData.toString().toByteArray())
                zipStream.closeEntry()

                zipStream.close()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Import encrypted backup from URI
     */
    suspend fun importBackup(
        uri: Uri,
        password: String
    ): Result<BackupData> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val decryptedStream = createDecryptionStream(inputStream, password)
                val zipStream = ZipInputStream(decryptedStream)

                var entry = zipStream.nextEntry
                val backupJson = StringBuilder()

                while (entry != null) {
                    if (entry.name == "backup.json") {
                        val buffer = ByteArray(1024)
                        var len: Int
                        while (zipStream.read(buffer).also { len = it } > 0) {
                            backupJson.append(String(buffer, 0, len))
                        }
                    }
                    entry = zipStream.nextEntry
                }

                zipStream.close()

                val data = parseBackupJson(backupJson.toString())
                Result.success(data)
            } ?: Result.failure(Exception("Could not open file"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createEncryptionStream(outputStream: OutputStream, password: String): OutputStream {
        val salt = ByteArray(16).apply { java.security.SecureRandom().nextBytes(this) }
        val iv = ByteArray(16).apply { java.security.SecureRandom().nextBytes(this) }

        // Write salt and IV first
        outputStream.write(salt)
        outputStream.write(iv)

        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))

        return CipherOutputStream(outputStream, cipher)
    }

    private fun createDecryptionStream(inputStream: InputStream, password: String): InputStream {
        val salt = ByteArray(16)
        val iv = ByteArray(16)

        inputStream.read(salt)
        inputStream.read(iv)

        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))

        return CipherInputStream(inputStream, cipher)
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance(KEY_FACTORY)
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val key = factory.generateSecret(spec)
        return SecretKeySpec(key.encoded, "AES")
    }

    private fun createBackupJson(
        periodLogs: List<PeriodLog>,
        partnerNotes: List<PartnerNote>,
        settings: AppSettings
    ): JSONObject {
        return JSONObject().apply {
            put("version", BACKUP_VERSION)
            put("timestamp", Instant.now().toString())
            put("appVersion", "1.0.0") // TODO: Get from BuildConfig

            // Period logs
            put("periodLogs", JSONArray().apply {
                periodLogs.forEach { log ->
                    put(JSONObject().apply {
                        put("date", log.date.toString())
                        put("isPeriodStart", log.isPeriodStart)
                        put("isPeriodEnd", log.isPeriodEnd)
                        put("isSafeDay", log.isSafeDay)
                        put("flowIntensity", log.flowIntensity?.name)
                        put("symptoms", JSONArray(log.symptoms))
                        put("mood", log.mood)
                        put("notes", log.notes)
                        put("customFields", JSONObject(log.customFields))
                    })
                }
            })

            // Partner notes
            put("partnerNotes", JSONArray().apply {
                partnerNotes.forEach { note ->
                    put(JSONObject().apply {
                        put("message", note.message)
                        put("isEnabled", note.isEnabled)
                    })
                }
            })

            // Settings (exclude PIN hash for security)
            put("settings", JSONObject().apply {
                put("averageCycleLength", settings.averageCycleLength)
                put("averagePeriodLength", settings.averagePeriodLength)
                put("lutealPhaseLength", settings.lutealPhaseLength)
                put("partnerNotesEnabled", settings.partnerNotesEnabled)
                put("isMetalModeUnlocked", settings.isMetalModeUnlocked)
                put("isMetalModeEnabled", settings.isMetalModeEnabled)
            })
        }
    }

    private fun parseBackupJson(json: String): BackupData {
        val obj = JSONObject(json)

        val periodLogs = mutableListOf<PeriodLog>()
        val logsArray = obj.getJSONArray("periodLogs")
        for (i in 0 until logsArray.length()) {
            val logObj = logsArray.getJSONObject(i)
            periodLogs.add(PeriodLog(
                date = LocalDate.parse(logObj.getString("date")),
                isPeriodStart = logObj.getBoolean("isPeriodStart"),
                isPeriodEnd = logObj.getBoolean("isPeriodEnd"),
                isSafeDay = logObj.optBoolean("isSafeDay", false),
                flowIntensity = logObj.optString("flowIntensity").takeIf { it.isNotBlank() }?.let {
                    com.bloodmoon.data.local.entities.FlowIntensity.valueOf(it)
                },
                symptoms = parseJsonArray(logObj.optJSONArray("symptoms")),
                mood = logObj.optString("mood"),
                notes = logObj.optString("notes"),
                customFields = parseJsonObject(logObj.optJSONObject("customFields"))
            ))
        }

        val partnerNotes = mutableListOf<PartnerNote>()
        val notesArray = obj.getJSONArray("partnerNotes")
        for (i in 0 until notesArray.length()) {
            val noteObj = notesArray.getJSONObject(i)
            partnerNotes.add(PartnerNote(
                message = noteObj.getString("message"),
                isEnabled = noteObj.getBoolean("isEnabled")
            ))
        }

        val settingsObj = obj.getJSONObject("settings")
        val settings = AppSettings(
            averageCycleLength = settingsObj.getInt("averageCycleLength"),
            averagePeriodLength = settingsObj.getInt("averagePeriodLength"),
            lutealPhaseLength = settingsObj.getInt("lutealPhaseLength"),
            partnerNotesEnabled = settingsObj.getBoolean("partnerNotesEnabled"),
            isMetalModeUnlocked = settingsObj.optBoolean("isMetalModeUnlocked", false),
            isMetalModeEnabled = settingsObj.optBoolean("isMetalModeEnabled", false)
        )

        return BackupData(periodLogs, partnerNotes, settings)
    }

    private fun parseJsonArray(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        return (0 until array.length()).map { array.getString(it) }
    }

    private fun parseJsonObject(obj: JSONObject?): Map<String, String> {
        if (obj == null) return emptyMap()
        return obj.keys().asSequence().associateWith { obj.getString(it) }
    }
}

data class BackupData(
    val periodLogs: List<PeriodLog>,
    val partnerNotes: List<PartnerNote>,
    val settings: AppSettings
)
