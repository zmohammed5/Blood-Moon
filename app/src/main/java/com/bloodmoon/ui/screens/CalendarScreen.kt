package com.bloodmoon.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bloodmoon.data.local.entities.PeriodLog
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@Composable
fun CalendarScreen(
    onDateSelected: (LocalDate) -> Unit,
    onDeleteLog: (PeriodLog) -> Unit = {},
    periodDates: Set<LocalDate> = emptySet(),
    fertileDates: Set<LocalDate> = emptySet(),
    safeDates: Set<LocalDate> = emptySet(),
    periodLogs: List<PeriodLog> = emptyList(),
    modifier: Modifier = Modifier
) {
    // Save current month across navigation using string representation
    var currentMonth by rememberSaveable(
        stateSaver = androidx.compose.runtime.saveable.Saver(
            save = { it.toString() },
            restore = { YearMonth.parse(it) }
        )
    ) { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showViewDialog by remember { mutableStateOf(false) }
    var viewingLog by remember { mutableStateOf<PeriodLog?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.ChevronLeft, "Previous month")
            }

            Text(
                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.ChevronRight, "Next month")
            }
        }

        // Day of week headers
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Calendar grid
        CalendarGrid(
            yearMonth = currentMonth,
            periodDates = periodDates,
            fertileDates = fertileDates,
            selectedDate = selectedDate,
            periodLogs = periodLogs,
            onDateClick = { date ->
                selectedDate = date
                // Single tap - show view dialog if there's data
                val log = periodLogs.find { it.date == date }
                if (log != null) {
                    viewingLog = log
                    showViewDialog = true
                } else {
                    // No data - open edit screen
                    onDateSelected(date)
                }
            },
            onDateDoubleClick = { date ->
                // Double tap - always open edit screen
                selectedDate = date
                onDateSelected(date)
            }
        )

        // Legend
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Legend",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                LegendItem(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    label = "Period Days"
                )
                LegendItem(
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                    label = "Ovulation Days"
                )
                LegendItem(
                    color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                    label = "Safe Days (<5% conception risk)"
                )
                LegendItem(
                    color = MaterialTheme.colorScheme.outline,
                    label = "Today",
                    isBorder = true
                )
            }
        }
    }

    // View dialog for period data
    if (showViewDialog && viewingLog != null) {
        PeriodViewDialog(
            log = viewingLog!!,
            onDismiss = { showViewDialog = false },
            onEdit = {
                showViewDialog = false
                onDateSelected(viewingLog!!.date)
            },
            onDelete = {
                onDeleteLog(viewingLog!!)
                showViewDialog = false
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    periodDates: Set<LocalDate>,
    fertileDates: Set<LocalDate>,
    selectedDate: LocalDate?,
    periodLogs: List<PeriodLog>,
    onDateClick: (LocalDate) -> Unit,
    onDateDoubleClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = yearMonth.lengthOfMonth()

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.height(300.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        // Empty cells before month starts
        items(startDayOfWeek) {
            Box(modifier = Modifier.aspectRatio(1f))
        }

        // Days of the month
        items(daysInMonth) { dayIndex ->
            val date = yearMonth.atDay(dayIndex + 1)
            val isToday = date == LocalDate.now()
            val isPeriod = periodDates.contains(date)
            val isFertile = fertileDates.contains(date)
            val isSafe = safeDates.contains(date)
            val isSelected = date == selectedDate

            DayCell(
                day = dayIndex + 1,
                isToday = isToday,
                isPeriod = isPeriod,
                isFertile = isFertile,
                isSafe = isSafe,
                isSelected = isSelected,
                onClick = { onDateClick(date) },
                onDoubleClick = { onDateDoubleClick(date) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DayCell(
    day: Int,
    isToday: Boolean,
    isPeriod: Boolean,
    isFertile: Boolean,
    isSafe: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit
) {
    val backgroundColor = when {
        isPeriod -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        isFertile -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
        isSafe -> Color(0xFF4CAF50).copy(alpha = 0.2f) // Green for safe days
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (isToday) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
                } else {
                    Modifier
                }
            )
            .combinedClickable(
                onClick = onClick,
                onDoubleClick = onDoubleClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onBackground
            }
        )
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    isBorder: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .then(
                    if (isBorder) {
                        Modifier.border(2.dp, color, CircleShape)
                    } else {
                        Modifier.background(color)
                    }
                )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PeriodViewDialog(
    log: PeriodLog,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(log.date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (log.isPeriodStart) {
                    Text("✓ Period Start", style = MaterialTheme.typography.bodyMedium)
                }
                if (log.isPeriodEnd) {
                    Text("✓ Period End", style = MaterialTheme.typography.bodyMedium)
                }

                log.flowIntensity?.let { intensity ->
                    val flowLabel = when (intensity) {
                        com.bloodmoon.data.local.entities.FlowIntensity.SPOTTING -> "Spotting"
                        com.bloodmoon.data.local.entities.FlowIntensity.LIGHT -> "Light"
                        com.bloodmoon.data.local.entities.FlowIntensity.MEDIUM -> "Medium"
                        com.bloodmoon.data.local.entities.FlowIntensity.HEAVY -> "Heavy"
                        com.bloodmoon.data.local.entities.FlowIntensity.VERY_HEAVY -> "Very Heavy"
                    }
                    Text(
                        "Flow: $flowLabel",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                log.mood?.let {
                    if (it.isNotBlank()) {
                        Text(
                            "Mood: $it",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                log.notes?.let {
                    if (it.isNotBlank()) {
                        Divider()
                        Text(
                            "Notes:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) {
                    Text("Edit")
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        }
    )
}
