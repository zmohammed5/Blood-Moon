package com.bloodmoon.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bloodmoon.data.local.dao.AppSettingsDao
import com.bloodmoon.data.local.dao.MedicationDao
import com.bloodmoon.data.local.dao.PartnerNoteDao
import com.bloodmoon.data.local.dao.PeriodLogDao
import com.bloodmoon.data.local.entities.AppSettings
import com.bloodmoon.data.local.entities.Medication
import com.bloodmoon.data.local.entities.MedicationLog
import com.bloodmoon.data.local.entities.PartnerNote
import com.bloodmoon.data.local.entities.PeriodLog
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        PeriodLog::class,
        PartnerNote::class,
        AppSettings::class,
        Medication::class,
        MedicationLog::class
    ],
    version = 10,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class BloodMoonDatabase : RoomDatabase() {
    abstract fun periodLogDao(): PeriodLogDao
    abstract fun partnerNoteDao(): PartnerNoteDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun medicationDao(): MedicationDao

    companion object {
        private const val DATABASE_NAME = "blood_moon.db"

        @Volatile
        private var INSTANCE: BloodMoonDatabase? = null

        // Migration from version 6 to 7: SPOTTING enum added (no schema change needed)
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // No SQL changes needed - FlowIntensity enum is handled by TypeConverter
                // The SPOTTING value will be stored as a string, so existing data is compatible
            }
        }

        // Migration from version 7 to 8: Add isSafeDay column
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add isSafeDay column with default value of 0 (false)
                database.execSQL("ALTER TABLE period_logs ADD COLUMN isSafeDay INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Migration from version 8 to 9: Add new prediction/stats settings
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new settings columns for enhanced predictions
                database.execSQL("ALTER TABLE app_settings ADD COLUMN showSafeDays INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE app_settings ADD COLUMN showPeriodProbability INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE app_settings ADD COLUMN showDetailedPredictions INTEGER NOT NULL DEFAULT 1")
            }
        }

        // Migration from version 9 to 10: Add medication tracking tables
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create medications table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS medications (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        dosage TEXT,
                        frequency TEXT NOT NULL,
                        reminderTimes TEXT NOT NULL,
                        notes TEXT,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        startDate INTEGER NOT NULL,
                        endDate INTEGER,
                        color TEXT,
                        createdAt INTEGER NOT NULL
                    )
                """)

                // Create medication_logs table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS medication_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        medicationId INTEGER NOT NULL,
                        takenAt INTEGER NOT NULL,
                        skipped INTEGER NOT NULL DEFAULT 0,
                        notes TEXT,
                        FOREIGN KEY(medicationId) REFERENCES medications(id) ON DELETE CASCADE
                    )
                """)

                // Create index for faster queries
                database.execSQL("CREATE INDEX IF NOT EXISTS index_medication_logs_medicationId ON medication_logs(medicationId)")
            }
        }

        fun getInstance(context: Context): BloodMoonDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): BloodMoonDatabase {
            // Generate encryption key from device-specific data
            val passphrase = getEncryptionKey(context)
            val factory = SupportFactory(SQLiteDatabase.getBytes(passphrase.toCharArray()))

            return Room.databaseBuilder(
                context.applicationContext,
                BloodMoonDatabase::class.java,
                DATABASE_NAME
            )
                .openHelperFactory(factory)
                .addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
                .build()
        }

        private fun getEncryptionKey(context: Context): String {
            // Use device-specific identifiers to generate a unique encryption key
            // In production, consider using Android Keystore for more secure key management
            val prefs = context.getSharedPreferences("blood_moon_secure", Context.MODE_PRIVATE)

            var key = prefs.getString("db_key", null)
            if (key == null) {
                // Generate new key
                key = generateSecureKey()
                prefs.edit().putString("db_key", key).apply()
            }
            return key
        }

        private fun generateSecureKey(): String {
            val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
            return (1..64)
                .map { charset.random() }
                .joinToString("")
        }

        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
