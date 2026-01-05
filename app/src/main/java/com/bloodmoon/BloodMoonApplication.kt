package com.bloodmoon

import android.app.Application
import com.bloodmoon.data.local.BloodMoonDatabase
import com.bloodmoon.data.repository.MedicationRepository
import com.bloodmoon.data.repository.PartnerNoteRepository
import com.bloodmoon.data.repository.PeriodRepository
import com.bloodmoon.data.repository.SettingsRepository

class BloodMoonApplication : Application() {

    lateinit var database: BloodMoonDatabase
        private set

    lateinit var periodRepository: PeriodRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var partnerNoteRepository: PartnerNoteRepository
        private set

    lateinit var medicationRepository: MedicationRepository
        private set

    override fun onCreate() {
        super.onCreate()

        // Initialize encrypted database
        database = BloodMoonDatabase.getInstance(this)

        // Initialize repositories
        periodRepository = PeriodRepository(database.periodLogDao())
        settingsRepository = SettingsRepository(database.appSettingsDao())
        partnerNoteRepository = PartnerNoteRepository(database.partnerNoteDao())
        medicationRepository = MedicationRepository(database.medicationDao())
    }

    override fun onTerminate() {
        super.onTerminate()
        BloodMoonDatabase.closeDatabase()
    }
}
