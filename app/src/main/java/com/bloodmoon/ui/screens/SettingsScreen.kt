package com.bloodmoon.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bloodmoon.BloodMoonApplication
import com.bloodmoon.data.backup.BackupManager
import com.bloodmoon.data.local.entities.AppSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onUpdateSettings: (AppSettings) -> Unit,
    onNavigateToPersonalNotes: () -> Unit = {},
    onNavigateToMedications: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showPinDialog by remember { mutableStateOf(false) }
    var showBackupPasswordDialog by remember { mutableStateOf(false) }
    var showRestorePasswordDialog by remember { mutableStateOf(false) }
    var backupMessage by remember { mutableStateOf<String?>(null) }
    var exportUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var importUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val context = LocalContext.current
    val app = context.applicationContext as BloodMoonApplication
    val scope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                exportUri = uri
                showBackupPasswordDialog = true
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                importUri = uri
                showRestorePasswordDialog = true
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )

        // Security Section
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Security",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // PIN Lock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable PIN Lock")
                    Switch(
                        checked = settings.isPinEnabled,
                        onCheckedChange = {
                            if (it) {
                                showPinDialog = true
                            } else {
                                onUpdateSettings(settings.copy(isPinEnabled = false, pinHash = null))
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Biometric
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Biometric")
                    Switch(
                        checked = settings.isBiometricEnabled,
                        onCheckedChange = {
                            onUpdateSettings(settings.copy(isBiometricEnabled = it))
                        }
                    )
                }
            }
        }

        // Appearance Section
        if (settings.isMetalModeUnlocked) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Alternate Theme")
                            Text(
                                text = "Enhanced crimson color palette",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Switch(
                            checked = settings.isMetalModeEnabled,
                            onCheckedChange = {
                                onUpdateSettings(settings.copy(isMetalModeEnabled = it))
                            }
                        )
                    }
                }
            }
        }

        // Medications Section (PCOS Support)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Medications",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Track medications like Metformin, Progesterone, etc.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onNavigateToMedications,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Manage Medications")
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }

        // Personal Notes Section
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Personal Notes",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Personal Notes")
                    Switch(
                        checked = settings.partnerNotesEnabled,
                        onCheckedChange = {
                            onUpdateSettings(settings.copy(partnerNotesEnabled = it))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onNavigateToPersonalNotes,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Manage Personal Notes")
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }

        // Fertility & Insights Section
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Fertility & Insights",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Show Ovulation Days
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show Ovulation Days")
                        Text(
                            text = "Highlight ovulation period on calendar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = settings.showOvulationDays,
                        onCheckedChange = {
                            onUpdateSettings(settings.copy(showOvulationDays = it))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Show Cycle Insights
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show Cycle Insights")
                        Text(
                            text = "Display cycle analysis on dashboard",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = settings.showCycleInsights,
                        onCheckedChange = {
                            onUpdateSettings(settings.copy(showCycleInsights = it))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Show Cycle Statistics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show Cycle Statistics")
                        Text(
                            text = "Display detailed stats (avg, min, max cycles)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = settings.showCycleStatistics,
                        onCheckedChange = {
                            onUpdateSettings(settings.copy(showCycleStatistics = it))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Show Safe Days
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show Safe Days")
                        Text(
                            text = "Highlight low-risk days on calendar (<5% conception probability)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = settings.showSafeDays,
                        onCheckedChange = {
                            onUpdateSettings(settings.copy(showSafeDays = it))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Show Period Probability
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show Period Probability")
                        Text(
                            text = "Display daily percentage likelihood of period starting",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = settings.showPeriodProbability,
                        onCheckedChange = {
                            onUpdateSettings(settings.copy(showPeriodProbability = it))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Show Detailed Predictions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show Detailed Predictions")
                        Text(
                            text = "Display scientific probability metrics and confidence levels",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = settings.showDetailedPredictions,
                        onCheckedChange = {
                            onUpdateSettings(settings.copy(showDetailedPredictions = it))
                        }
                    )
                }
            }
        }

        // Backup & Restore Section
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Backup & Restore",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Export and import your data with encryption",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "application/zip"
                                putExtra(Intent.EXTRA_TITLE, "bloodmoon_backup.zip")
                            }
                            exportLauncher.launch(intent)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Export")
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "application/zip"
                            }
                            importLauncher.launch(intent)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Import")
                    }
                }

                if (backupMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = backupMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Cycle Settings Section
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Cycle Settings",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Average Cycle Length: ${settings.averageCycleLength} days",
                    style = MaterialTheme.typography.bodyMedium
                )

                Slider(
                    value = settings.averageCycleLength.toFloat(),
                    onValueChange = {
                        onUpdateSettings(settings.copy(averageCycleLength = it.toInt()))
                    },
                    valueRange = 21f..35f,
                    steps = 13
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Average Period Length: ${settings.averagePeriodLength} days",
                    style = MaterialTheme.typography.bodyMedium
                )

                Slider(
                    value = settings.averagePeriodLength.toFloat(),
                    onValueChange = {
                        onUpdateSettings(settings.copy(averagePeriodLength = it.toInt()))
                    },
                    valueRange = 3f..8f,
                    steps = 4
                )
            }
        }
    }

    // PIN Setup Dialog
    if (showPinDialog) {
        PinSetupDialog(
            onDismiss = { showPinDialog = false },
            onPinSet = { pin ->
                // Hash the PIN and save
                val hashedPin = hashPin(pin)
                onUpdateSettings(settings.copy(isPinEnabled = true, pinHash = hashedPin))
                showPinDialog = false
            }
        )
    }

    // Backup Password Dialog
    if (showBackupPasswordDialog && exportUri != null) {
        BackupPasswordDialog(
            onDismiss = {
                showBackupPasswordDialog = false
                exportUri = null
            },
            onConfirm = { password ->
                scope.launch {
                    val backupManager = BackupManager(context)

                    // Collect data (use first() to get a single snapshot)
                    val logs = app.periodRepository.getAllLogs().first()
                    val notes = app.partnerNoteRepository.getAllNotes().first()

                    val result = backupManager.exportBackup(
                        uri = exportUri!!,
                        password = password,
                        periodLogs = logs,
                        partnerNotes = notes,
                        settings = settings
                    )

                    backupMessage = if (result.isSuccess) {
                        "Backup created successfully"
                    } else {
                        "Backup failed: ${result.exceptionOrNull()?.message}"
                    }
                }
                showBackupPasswordDialog = false
                exportUri = null
            }
        )
    }

    // Restore Password Dialog
    if (showRestorePasswordDialog && importUri != null) {
        BackupPasswordDialog(
            isRestore = true,
            onDismiss = {
                showRestorePasswordDialog = false
                importUri = null
            },
            onConfirm = { password ->
                scope.launch {
                    val backupManager = BackupManager(context)

                    val result = backupManager.importBackup(
                        uri = importUri!!,
                        password = password
                    )

                    if (result.isSuccess) {
                        val backupData = result.getOrNull()!!

                        // Restore data
                        backupData.periodLogs.forEach {
                            app.periodRepository.insertLog(it)
                        }
                        backupData.partnerNotes.forEach {
                            app.partnerNoteRepository.insertNote(it.message)
                        }
                        app.settingsRepository.updateSettings(backupData.settings)

                        backupMessage = "Backup restored successfully"
                    } else {
                        backupMessage = "Restore failed: ${result.exceptionOrNull()?.message}"
                    }
                }
                showRestorePasswordDialog = false
                importUri = null
            }
        )
    }
}

@Composable
private fun PinSetupDialog(
    onDismiss: () -> Unit,
    onPinSet: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(0) } // 0 = enter, 1 = confirm
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (step == 0) "Set PIN" else "Confirm PIN")
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (step == 0) "Enter a 4-digit PIN" else "Re-enter your PIN",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // PIN dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val currentPin = if (step == 0) pin else confirmPin
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(
                                    if (index < currentPin.length) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline,
                                    androidx.compose.foundation.shape.CircleShape
                                )
                        )
                    }
                }

                if (error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "PINs do not match",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Number pad
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("", "0", "⌫")
                    ).forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { digit ->
                                if (digit.isNotEmpty()) {
                                    OutlinedButton(
                                        onClick = {
                                            val currentPin = if (step == 0) pin else confirmPin
                                            when (digit) {
                                                "⌫" -> {
                                                    if (step == 0 && pin.isNotEmpty()) {
                                                        pin = pin.dropLast(1)
                                                    } else if (step == 1 && confirmPin.isNotEmpty()) {
                                                        confirmPin = confirmPin.dropLast(1)
                                                    }
                                                }
                                                else -> {
                                                    if (currentPin.length < 4) {
                                                        if (step == 0) {
                                                            pin += digit
                                                            if (pin.length == 4) {
                                                                step = 1
                                                                error = false
                                                            }
                                                        } else {
                                                            confirmPin += digit
                                                            if (confirmPin.length == 4) {
                                                                if (pin == confirmPin) {
                                                                    onPinSet(pin)
                                                                } else {
                                                                    error = true
                                                                    confirmPin = ""
                                                                    step = 0
                                                                    pin = ""
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(60.dp)
                                    ) {
                                        Text(digit, style = MaterialTheme.typography.titleLarge)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(60.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun hashPin(pin: String): String {
    val digest = java.security.MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(pin.toByteArray())
    return hash.joinToString("") { "%02x".format(it) }
}

@Composable
private fun BackupPasswordDialog(
    isRestore: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isRestore) "Enter Backup Password" else "Set Backup Password")
        },
        text = {
            Column {
                Text(
                    text = if (isRestore) {
                        "Enter the password used to encrypt this backup"
                    } else {
                        "Choose a password to encrypt your backup"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        error = null
                    },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (!isRestore) {
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            error = null
                        },
                        label = { Text("Confirm Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (!isRestore && password != confirmPassword) {
                        error = "Passwords do not match"
                        return@TextButton
                    }
                    if (password.length < 4) {
                        error = "Password must be at least 4 characters"
                        return@TextButton
                    }
                    onConfirm(password)
                },
                enabled = password.isNotBlank() && (isRestore || confirmPassword.isNotBlank())
            ) {
                Text("Continue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
