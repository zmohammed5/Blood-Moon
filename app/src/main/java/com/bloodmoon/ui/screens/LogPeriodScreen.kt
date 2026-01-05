package com.bloodmoon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bloodmoon.data.local.entities.FlowIntensity
import com.bloodmoon.data.local.entities.PeriodLog
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogPeriodScreen(
    selectedDate: LocalDate,
    existingLog: PeriodLog? = null,
    onSave: (PeriodLog) -> Unit,
    onDelete: (() -> Unit)? = null,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPeriodStart by remember { mutableStateOf(existingLog?.isPeriodStart ?: false) }
    var isPeriodEnd by remember { mutableStateOf(existingLog?.isPeriodEnd ?: false) }
    var flowIntensity by remember { mutableStateOf(existingLog?.flowIntensity) }
    var mood by remember { mutableStateOf(existingLog?.mood ?: "") }
    var notes by remember { mutableStateOf(existingLog?.notes ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Period") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

        Divider()

        // Period Start/End
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Period Status",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Period Start")
                    Switch(
                        checked = isPeriodStart,
                        onCheckedChange = { isPeriodStart = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Period End")
                    Switch(
                        checked = isPeriodEnd,
                        onCheckedChange = { isPeriodEnd = it }
                    )
                }
            }
        }

        // Flow Intensity
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Flow Intensity",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // First row: Spotting, Light
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = flowIntensity == FlowIntensity.SPOTTING,
                            onClick = {
                                flowIntensity = if (flowIntensity == FlowIntensity.SPOTTING) null else FlowIntensity.SPOTTING
                            },
                            label = { Text(getFlowLabel(FlowIntensity.SPOTTING)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = flowIntensity == FlowIntensity.LIGHT,
                            onClick = {
                                flowIntensity = if (flowIntensity == FlowIntensity.LIGHT) null else FlowIntensity.LIGHT
                            },
                            label = { Text(getFlowLabel(FlowIntensity.LIGHT)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Second row: Medium, Heavy
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = flowIntensity == FlowIntensity.MEDIUM,
                            onClick = {
                                flowIntensity = if (flowIntensity == FlowIntensity.MEDIUM) null else FlowIntensity.MEDIUM
                            },
                            label = { Text(getFlowLabel(FlowIntensity.MEDIUM)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = flowIntensity == FlowIntensity.HEAVY,
                            onClick = {
                                flowIntensity = if (flowIntensity == FlowIntensity.HEAVY) null else FlowIntensity.HEAVY
                            },
                            label = { Text(getFlowLabel(FlowIntensity.HEAVY)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Third row: Very Heavy (centered)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilterChip(
                            selected = flowIntensity == FlowIntensity.VERY_HEAVY,
                            onClick = {
                                flowIntensity = if (flowIntensity == FlowIntensity.VERY_HEAVY) null else FlowIntensity.VERY_HEAVY
                            },
                            label = { Text(getFlowLabel(FlowIntensity.VERY_HEAVY)) },
                            modifier = Modifier.fillMaxWidth(0.5f)
                        )
                    }
                }

                // Show funny description for selected intensity
                flowIntensity?.let { intensity ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = getFlowDescription(intensity),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Mood
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Mood",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = mood,
                    onValueChange = { mood = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("How are you feeling?") },
                    singleLine = true
                )
            }
        }

        // Notes
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    placeholder = { Text("Add any notes...") },
                    maxLines = 5
                )
            }
        }

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    if (!isSaving) {
                        isSaving = true
                        val log = PeriodLog(
                            id = existingLog?.id ?: 0,
                            date = selectedDate,
                            isPeriodStart = isPeriodStart,
                            isPeriodEnd = isPeriodEnd,
                            isSafeDay = false, // Reserved for future predicted safe days feature
                            flowIntensity = flowIntensity,
                            mood = mood.ifBlank { null },
                            notes = notes.ifBlank { null }
                        )
                        onSave(log)
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.weight(1f)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isSaving) "Saving..." else "Save")
            }
        }

        // Delete button (only show if editing existing log)
        if (onDelete != null && existingLog != null) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete Entry")
            }
        }
    }
    }
}

/**
 * Get display label for flow intensity
 */
private fun getFlowLabel(intensity: FlowIntensity): String {
    return when (intensity) {
        FlowIntensity.SPOTTING -> "Spotting"
        FlowIntensity.LIGHT -> "Light"
        FlowIntensity.MEDIUM -> "Medium"
        FlowIntensity.HEAVY -> "Heavy"
        FlowIntensity.VERY_HEAVY -> "Very Heavy"
    }
}

/**
 * Get humorous but supportive descriptions for flow intensity
 */
private fun getFlowDescription(intensity: FlowIntensity): String {
    return when (intensity) {
        FlowIntensity.SPOTTING -> "Pre-period vibes - barely there"
        FlowIntensity.LIGHT -> "Just a little spotting"
        FlowIntensity.MEDIUM -> "Normal flow - typical day"
        FlowIntensity.HEAVY -> "Bring extra supplies"
        FlowIntensity.VERY_HEAVY -> "Absolute carnage - stock up on everything"
    }
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
