package com.bloodmoon.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodmoon.data.repository.PeriodRepository
import com.bloodmoon.data.repository.PartnerNoteRepository
import com.bloodmoon.data.repository.SettingsRepository
import com.bloodmoon.domain.CyclePredictionEngine
import com.bloodmoon.domain.CycleStatistics
import com.bloodmoon.domain.model.CyclePrediction
import com.bloodmoon.domain.model.CycleStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MainUiState(
    val isMetalMode: Boolean = false,
    val isPinEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val isUnlocked: Boolean = false,
    val prediction: CyclePrediction? = null,
    val cycleStatus: CycleStatus? = null,
    val cycleStatistics: CycleStatistics.Statistics? = null,
    val cycleInsights: List<String> = emptyList(),
    val dailyAffirmation: String = "",
    val partnerNote: String? = null,
    val showMetalModeUnlockMessage: Boolean = false
)

class MainViewModel(
    private val periodRepository: PeriodRepository,
    private val settingsRepository: SettingsRepository,
    private val partnerNoteRepository: PartnerNoteRepository,
    val predictionEngine: CyclePredictionEngine = CyclePredictionEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        loadPrediction()
        loadDailyContent()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                _uiState.update { it.copy(
                    isMetalMode = settings.isMetalModeEnabled,
                    isPinEnabled = settings.isPinEnabled,
                    isBiometricEnabled = settings.isBiometricEnabled,
                    showMetalModeUnlockMessage = settings.isMetalModeUnlocked &&
                        settings.moonyTapCount == 6
                )}
            }
        }
    }

    private fun loadPrediction() {
        viewModelScope.launch {
            periodRepository.getAllLogs().collect { logs ->
                val settings = settingsRepository.getSettingsOnce()
                val prediction = predictionEngine.predictNextCycle(
                    periodLogs = logs.filter { it.isPeriodStart },
                    averageCycleLength = settings.averageCycleLength,
                    averagePeriodLength = settings.averagePeriodLength,
                    lutealPhaseLength = settings.lutealPhaseLength
                )
                val status = predictionEngine.getCycleStatus(logs, prediction)

                // Calculate statistics and insights
                val statistics = CycleStatistics.calculateStatistics(logs)
                val insights = CycleStatistics.getInsights(statistics)

                _uiState.update { it.copy(
                    prediction = prediction,
                    cycleStatus = status,
                    cycleStatistics = statistics,
                    cycleInsights = insights
                )}
            }
        }
    }

    private fun loadDailyContent() {
        viewModelScope.launch {
            val settings = settingsRepository.getSettingsOnce()
            val affirmation = com.bloodmoon.util.Affirmations.getDailyAffirmation()

            val partnerNote = if (settings.partnerNotesEnabled) {
                partnerNoteRepository.getRandomNote()?.message
            } else {
                null
            }

            _uiState.update { it.copy(
                dailyAffirmation = affirmation,
                partnerNote = partnerNote
            )}
        }
    }

    fun unlockApp() {
        _uiState.update { it.copy(isUnlocked = true) }
    }

    fun handleMoonyTap() {
        viewModelScope.launch {
            val count = settingsRepository.incrementMoonyTapCount()
            if (count == 6) {
                _uiState.update { it.copy(showMetalModeUnlockMessage = true) }
            }

            // Reset tap count after 10 seconds
            kotlinx.coroutines.delay(10000)
            settingsRepository.resetMoonyTapCount()
        }
    }

    fun dismissMetalModeMessage() {
        _uiState.update { it.copy(showMetalModeUnlockMessage = false) }
    }
}
