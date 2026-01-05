package com.bloodmoon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bloodmoon.domain.CycleStatistics
import com.bloodmoon.domain.model.CyclePrediction
import com.bloodmoon.domain.model.CycleStatus
import com.bloodmoon.ui.components.Moony
import com.bloodmoon.ui.components.MoonyAnimation
import com.bloodmoon.ui.components.MoonyVariant
import com.bloodmoon.ui.theme.*
import com.bloodmoon.util.MoonPhaseCalculator
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    prediction: CyclePrediction?,
    cycleStatus: CycleStatus?,
    cycleStatistics: CycleStatistics.Statistics?,
    cycleInsights: List<String>,
    dailyAffirmation: String,
    partnerNote: String?,
    isMetalMode: Boolean,
    showCycleInsights: Boolean,
    showCycleStatistics: Boolean,
    showPeriodProbability: Boolean = true,
    showDetailedPredictions: Boolean = true,
    periodProbabilityToday: Int? = null,
    conceptionProbabilityToday: Int? = null,
    onMoonyTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val moonPhase = MoonPhaseCalculator.getMoonPhase()
    val moonEmoji = MoonPhaseCalculator.getMoonEmoji(moonPhase)
    val isFullMoon = MoonPhaseCalculator.isFullMoon()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = "Blood Moon",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )

        // Moon Phase
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = moonPhase.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = moonEmoji,
                    style = MaterialTheme.typography.displayMedium
                )
            }
        }

        // Cycle Information
        if (prediction != null && cycleStatus != null) {
            CycleInfoCard(prediction, cycleStatus)
        }

        // Period Probability (Today)
        if (showPeriodProbability && periodProbabilityToday != null && prediction != null) {
            PeriodProbabilityCard(periodProbabilityToday)
        }

        // Detailed Predictions (Conception Probability & Confidence)
        if (showDetailedPredictions && prediction != null) {
            DetailedPredictionsCard(
                prediction = prediction,
                conceptionProbability = conceptionProbabilityToday
            )
        }

        // Cycle Insights
        if (showCycleInsights && cycleInsights.isNotEmpty()) {
            CycleInsightsCard(cycleInsights)
        }

        // Cycle Statistics
        if (showCycleStatistics && cycleStatistics != null) {
            CycleStatisticsCard(cycleStatistics)
        }

        // Moony Character
        Moony(
            variant = when {
                isMetalMode -> MoonyVariant.HORNS
                isFullMoon -> MoonyVariant.CORPSE_PAINT
                else -> MoonyVariant.DEFAULT
            },
            animation = MoonyAnimation.FLOAT,
            onTap = onMoonyTap,
            modifier = Modifier.size(80.dp)
        )

        // Daily Affirmation
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dailyAffirmation,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Partner Note (if available)
        partnerNote?.let { note ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CycleInfoCard(prediction: CyclePrediction, status: CycleStatus) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cycle Day
            Text(
                text = "Cycle Day ${status.cycleDay}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Divider(color = MaterialTheme.colorScheme.outline)

            // Days until period
            status.daysUntilPeriod?.let { days ->
                when {
                    days == 0 -> {
                        Text(
                            text = "Period expected today",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    days > 0 -> {
                        Text(
                            text = "$days days until expected period",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Current phase
            Text(
                text = "Phase: ${status.currentPhase.name.lowercase().replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // Next period prediction
            Text(
                text = "Next period: ${formatDate(prediction.predictedPeriodStart)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // Confidence
            val confidenceText = when (prediction.confidence.level) {
                com.bloodmoon.domain.model.ConfidenceLevel.HIGH -> "High confidence"
                com.bloodmoon.domain.model.ConfidenceLevel.MEDIUM -> "Medium confidence"
                com.bloodmoon.domain.model.ConfidenceLevel.LOW -> "Low confidence (more data needed)"
            }
            Text(
                text = confidenceText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun CycleInsightsCard(insights: List<String>) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Cycle Insights",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            insights.forEach { insight ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = insight,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun CycleStatisticsCard(stats: CycleStatistics.Statistics) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Cycle Statistics",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            // Cycles tracked
            StatRow("Cycles Tracked", "${stats.totalCyclesTracked}")

            // Days logged
            StatRow("Days Logged", "${stats.totalDaysLogged}")

            // Average cycle length
            stats.averageCycleLength?.let {
                StatRow("Average Cycle", "$it days")
            }

            // Cycle range
            if (stats.shortestCycle != null && stats.longestCycle != null) {
                StatRow("Cycle Range", "${stats.shortestCycle}-${stats.longestCycle} days")
            }

            // Regularity score
            if (stats.totalCyclesTracked >= 2) {
                val regularityText = when {
                    stats.regularityScore == 50 -> "Need more data"
                    stats.regularityScore >= 80 -> "${stats.regularityScore}% (Very Regular)"
                    stats.regularityScore >= 60 -> "${stats.regularityScore}% (Regular)"
                    stats.regularityScore >= 40 -> "${stats.regularityScore}% (Somewhat Irregular)"
                    else -> "${stats.regularityScore}% (Irregular)"
                }
                StatRow("Regularity", regularityText)
            }

            // Average period length
            stats.averagePeriodLength?.let {
                StatRow("Average Period", "$it days")
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

private fun formatDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    return date.format(formatter)
}

@Composable
private fun PeriodProbabilityCard(probabilityPercent: Int) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Period Probability Today",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            // Large percentage display
            Text(
                text = "$probabilityPercent%",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            // Interpretation
            val interpretation = when {
                probabilityPercent >= 80 -> "Very likely to start today"
                probabilityPercent >= 50 -> "Moderately likely"
                probabilityPercent >= 20 -> "Low probability"
                else -> "Unlikely today"
            }

            Text(
                text = interpretation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Divider(color = MaterialTheme.colorScheme.outline)

            Text(
                text = "Based on statistical analysis of your cycle history",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DetailedPredictionsCard(
    prediction: CyclePrediction,
    conceptionProbability: Int?
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Scientific Predictions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Prediction Confidence
            val confidenceText = when (prediction.confidence.level.name) {
                "HIGH" -> "High Confidence"
                "MEDIUM" -> "Medium Confidence"
                "LOW" -> "Low Confidence"
                else -> "Unknown"
            }

            val confidenceColor = when (prediction.confidence.level.name) {
                "HIGH" -> MaterialTheme.colorScheme.tertiary
                "MEDIUM" -> MaterialTheme.colorScheme.primary
                "LOW" -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Prediction Confidence",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Text(
                    text = confidenceText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = confidenceColor
                )
            }

            // Conception Probability (if available)
            conceptionProbability?.let { prob ->
                Divider(color = MaterialTheme.colorScheme.outline)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Conception Risk Today",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Based on Wilcox et al. (1995)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    Text(
                        text = "$prob%",
                        style = MaterialTheme.typography.titleLarge,
                        color = when {
                            prob >= 25 -> MaterialTheme.colorScheme.error
                            prob >= 10 -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.tertiary
                        }
                    )
                }
            }

            // Cycle Regularity Indicator
            if (prediction.isIrregular) {
                Divider(color = MaterialTheme.colorScheme.outline)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⚠",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Irregular cycles detected - predictions may vary",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline)

            // Scientific disclaimer
            Text(
                text = "All predictions are statistical estimates based on peer-reviewed research. Individual variation is normal.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}
