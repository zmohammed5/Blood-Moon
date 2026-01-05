package com.bloodmoon

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bloodmoon.data.repository.PartnerNoteRepository
import com.bloodmoon.data.repository.PeriodRepository
import com.bloodmoon.data.repository.SettingsRepository
import com.bloodmoon.ui.MainViewModel
import com.bloodmoon.ui.screens.CalendarScreen
import com.bloodmoon.ui.screens.DashboardScreen
import com.bloodmoon.ui.screens.LockScreen
import com.bloodmoon.ui.screens.LogPeriodScreen
import com.bloodmoon.ui.screens.MedicationsScreen
import com.bloodmoon.ui.screens.PersonalNotesScreen
import com.bloodmoon.ui.screens.SettingsScreen
import com.bloodmoon.ui.theme.BloodMoonTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home)
    object Calendar : Screen("calendar", "Calendar", Icons.Default.DateRange)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as BloodMoonApplication

        setContent {
            val viewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(
                    app.periodRepository,
                    app.settingsRepository,
                    app.partnerNoteRepository
                )
            )

            val uiState by viewModel.uiState.collectAsState()

            BloodMoonTheme(metalMode = uiState.isMetalMode) {
                // Show lock screen if PIN is enabled and not unlocked
                if (uiState.isPinEnabled && !uiState.isUnlocked) {
                    LockScreen(
                        isBiometricEnabled = uiState.isBiometricEnabled,
                        onPinEntered = { pin ->
                            app.settingsRepository.verifyPin(pin)
                        },
                        onUnlocked = {
                            viewModel.unlockApp()
                        }
                    )
                } else {
                    MainScreen(viewModel = viewModel, uiState = uiState)
                }

                // Alternate theme unlock message
                if (uiState.showMetalModeUnlockMessage) {
                    AlertDialog(
                        onDismissRequest = { viewModel.dismissMetalModeMessage() },
                        title = { Text("Alternate Theme Unlocked") },
                        text = {
                            Text("You've unlocked the alternate color theme with enhanced crimson tones.")
                        },
                        confirmButton = {
                            TextButton(onClick = { viewModel.dismissMetalModeMessage() }) {
                                Text("Got it!")
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    uiState: com.bloodmoon.ui.MainUiState
) {
    val navController = rememberNavController()
    val items = listOf(Screen.Dashboard, Screen.Calendar, Screen.Settings)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                val app = LocalContext.current.applicationContext as BloodMoonApplication
                val settings by app.settingsRepository.getSettings().collectAsState(initial = com.bloodmoon.data.local.entities.AppSettings())
                val logs by app.periodRepository.getAllLogs().collectAsState(initial = emptyList())

                // Calculate probabilities for today
                val today = LocalDate.now()
                val periodProbabilityToday = uiState.prediction?.let { prediction ->
                    val periodStarts = logs.filter { it.isPeriodStart }.map { it.date }
                    val cycleLengths = periodStarts.zipWithNext { a, b ->
                        ChronoUnit.DAYS.between(a, b).toInt()
                    }.filter { it in 14..45 }

                    viewModel.predictionEngine.calculatePeriodProbability(
                        date = today,
                        prediction = prediction,
                        cycleLengths = cycleLengths
                    )
                }

                val conceptionProbabilityToday = uiState.prediction?.let { prediction ->
                    viewModel.predictionEngine.calculateConceptionProbability(
                        date = today,
                        prediction = prediction
                    )
                }

                DashboardScreen(
                    prediction = uiState.prediction,
                    cycleStatus = uiState.cycleStatus,
                    cycleStatistics = uiState.cycleStatistics,
                    cycleInsights = uiState.cycleInsights,
                    dailyAffirmation = uiState.dailyAffirmation,
                    partnerNote = uiState.partnerNote,
                    isMetalMode = uiState.isMetalMode,
                    showCycleInsights = settings.showCycleInsights,
                    showCycleStatistics = settings.showCycleStatistics,
                    showPeriodProbability = settings.showPeriodProbability,
                    showDetailedPredictions = settings.showDetailedPredictions,
                    periodProbabilityToday = periodProbabilityToday,
                    conceptionProbabilityToday = conceptionProbabilityToday,
                    onMoonyTap = { viewModel.handleMoonyTap() }
                )
            }

            composable(Screen.Calendar.route) {
                val app = LocalContext.current.applicationContext as BloodMoonApplication
                val logs by app.periodRepository.getAllLogs().collectAsState(initial = emptyList())
                val settings by app.settingsRepository.getSettings().collectAsState(initial = com.bloodmoon.data.local.entities.AppSettings())
                val scope = rememberCoroutineScope()

                // Calculate ovulation days from prediction (only if setting is enabled)
                val fertileDates = if (settings.showOvulationDays) {
                    uiState.prediction?.let { prediction ->
                        val fertileStart = prediction.fertileWindowStart
                        val fertileEnd = prediction.fertileWindowEnd
                        generateSequence(fertileStart) { it.plusDays(1) }
                            .takeWhile { it <= fertileEnd }
                            .toSet()
                    } ?: emptySet()
                } else {
                    emptySet()
                }

                // Calculate safe days from prediction (only if setting is enabled)
                val safeDates = if (settings.showSafeDays && uiState.prediction != null) {
                    viewModel.predictionEngine.calculateSafeDays(uiState.prediction!!, logs)
                } else {
                    emptySet()
                }

                CalendarScreen(
                    onDateSelected = { date ->
                        navController.navigate("log_period/${date}")
                    },
                    onDeleteLog = { log ->
                        scope.launch(Dispatchers.IO) {
                            app.periodRepository.deleteLog(log)
                        }
                    },
                    periodDates = logs
                        .filter { it.flowIntensity != null || it.isPeriodStart }
                        .map { it.date }
                        .toSet(),
                    fertileDates = fertileDates,
                    safeDates = safeDates,
                    periodLogs = logs
                )
            }

            composable("log_period/{date}") { backStackEntry ->
                val app = LocalContext.current.applicationContext as BloodMoonApplication
                val dateString = backStackEntry.arguments?.getString("date")
                val selectedDate = dateString?.let { LocalDate.parse(it) } ?: LocalDate.now()
                val logs by app.periodRepository.getAllLogs().collectAsState(initial = emptyList())
                val existingLog = logs.find { it.date == selectedDate }
                val scope = rememberCoroutineScope()

                LogPeriodScreen(
                    selectedDate = selectedDate,
                    existingLog = existingLog,
                    onSave = { log ->
                        val context = app.applicationContext
                        scope.launch(Dispatchers.IO) {
                            try {
                                Log.d("BloodMoon", "=== SAVE ATTEMPT ===")
                                Log.d("BloodMoon", "Date: $selectedDate")
                                Log.d("BloodMoon", "Data: start=${log.isPeriodStart}, end=${log.isPeriodEnd}, flow=${log.flowIntensity}, mood='${log.mood}', notes='${log.notes}'")

                                // Query database directly to check for existing log
                                val dbLog = app.periodRepository.getLogForDate(selectedDate)
                                Log.d("BloodMoon", "Existing log: ${dbLog != null}, id=${dbLog?.id}")

                                if (dbLog != null) {
                                    // Update existing log
                                    Log.d("BloodMoon", "Updating log id=${dbLog.id}")
                                    app.periodRepository.updateLog(
                                        log.copy(
                                            id = dbLog.id,
                                            symptoms = dbLog.symptoms,
                                            customFields = dbLog.customFields,
                                            createdAt = dbLog.createdAt
                                        )
                                    )
                                } else {
                                    // Insert new log
                                    Log.d("BloodMoon", "Inserting new log")
                                    val newId = app.periodRepository.insertLog(log)
                                    Log.d("BloodMoon", "Inserted with ID: $newId")
                                }

                                // Verify the save
                                delay(200)
                                val verify = app.periodRepository.getLogForDate(selectedDate)
                                val success = verify != null
                                Log.d("BloodMoon", "Verify: ${if (success) "SUCCESS" else "FAILED"}")
                                if (verify != null) {
                                    Log.d("BloodMoon", "Saved data: id=${verify.id}, flow=${verify.flowIntensity}, mood='${verify.mood}'")
                                }

                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        if (success) "✓ Saved!" else "⚠ Save failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                delay(300)
                                withContext(Dispatchers.Main) {
                                    navController.popBackStack()
                                }
                            } catch (e: Exception) {
                                Log.e("BloodMoon", "=== SAVE ERROR ===", e)
                                e.printStackTrace()

                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    navController.popBackStack()
                                }
                            }
                        }
                    },
                    onDelete = if (existingLog != null) {
                        {
                            scope.launch(Dispatchers.IO) {
                                app.periodRepository.deleteLog(existingLog)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        app.applicationContext,
                                        "Entry deleted",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.popBackStack()
                                }
                            }
                        }
                    } else null,
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }

            composable("personal_notes") {
                val app = LocalContext.current.applicationContext as BloodMoonApplication
                val notes by app.partnerNoteRepository.getAllNotes().collectAsState(initial = emptyList())
                val scope = rememberCoroutineScope()

                PersonalNotesScreen(
                    notes = notes,
                    onAddNote = { message ->
                        scope.launch {
                            app.partnerNoteRepository.insertNote(message)
                        }
                    },
                    onDeleteNote = { note ->
                        scope.launch {
                            app.partnerNoteRepository.deleteNote(note)
                        }
                    },
                    onToggleNote = { note ->
                        scope.launch {
                            app.partnerNoteRepository.updateNote(
                                note.copy(isEnabled = !note.isEnabled)
                            )
                        }
                    }
                )
            }

            composable(Screen.Settings.route) {
                val app = LocalContext.current.applicationContext as BloodMoonApplication
                val settings by app.settingsRepository.getSettings().collectAsState(initial = com.bloodmoon.data.local.entities.AppSettings())
                val scope = rememberCoroutineScope()

                SettingsScreen(
                    settings = settings,
                    onUpdateSettings = { newSettings ->
                        scope.launch {
                            app.settingsRepository.updateSettings(newSettings)
                        }
                    },
                    onNavigateToPersonalNotes = {
                        navController.navigate("personal_notes")
                    },
                    onNavigateToMedications = {
                        navController.navigate("medications")
                    }
                )
            }

            // Medications Screen
            composable("medications") {
                val app = LocalContext.current.applicationContext as BloodMoonApplication
                val medications by app.medicationRepository.getAllActiveMedications().collectAsState(initial = emptyList())
                val scope = rememberCoroutineScope()

                MedicationsScreen(
                    medications = medications,
                    onAddMedication = { medication ->
                        scope.launch(Dispatchers.IO) {
                            app.medicationRepository.insertMedication(medication)
                        }
                    },
                    onUpdateMedication = { medication ->
                        scope.launch(Dispatchers.IO) {
                            app.medicationRepository.updateMedication(medication)
                        }
                    },
                    onDeleteMedication = { medication ->
                        scope.launch(Dispatchers.IO) {
                            app.medicationRepository.deleteMedication(medication)
                        }
                    },
                    onMedicationTaken = { medicationId ->
                        scope.launch(Dispatchers.IO) {
                            app.medicationRepository.logMedicationTaken(medicationId)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(app, "Medication logged ✓", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CalendarPlaceholder() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        androidx.compose.foundation.layout.Box(
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = "Calendar Coming Soon",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

// ViewModelFactory
class MainViewModelFactory(
    private val periodRepository: PeriodRepository,
    private val settingsRepository: SettingsRepository,
    private val partnerNoteRepository: PartnerNoteRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(com.bloodmoon.ui.MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return com.bloodmoon.ui.MainViewModel(
                periodRepository,
                settingsRepository,
                partnerNoteRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
