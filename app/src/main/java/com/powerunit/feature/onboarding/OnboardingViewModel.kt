package com.powerunit.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerunit.core.common.DateUtils
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.domain.model.Meter
import com.powerunit.domain.model.MeterType
import com.powerunit.domain.model.Reading
import com.powerunit.domain.repository.MeterRepository
import com.powerunit.domain.repository.ReadingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class OnboardingUiState(
    val step: Int = 0,
    val meterName: String = "Household Meter",
    val meterType: MeterType = MeterType.POSTPAID,
    val initialReading: String = "1000.0",
    val initialReadingDate: kotlinx.datetime.Instant = DateUtils.currentInstant(),
    val cycleDay: Int = 1,
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val meterRepository: MeterRepository,
    private val readingRepository: ReadingRepository,
    private val settingsPreferences: SettingsPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onNextStep() {
        _uiState.update { it.copy(step = it.step + 1) }
    }

    fun onPreviousStep() {
        _uiState.update { it.copy(step = (it.step - 1).coerceAtLeast(0)) }
    }

    fun onMeterNameChange(name: String) {
        _uiState.update { it.copy(meterName = name) }
    }

    fun onMeterTypeChange(type: MeterType) {
        _uiState.update { it.copy(meterType = type) }
    }

    fun onInitialReadingChange(reading: String) {
        _uiState.update { it.copy(initialReading = reading) }
    }

    fun onInitialReadingDateChange(date: kotlinx.datetime.Instant) {
        _uiState.update { it.copy(initialReadingDate = date) }
    }

    fun onCycleDayChange(day: Int) {
        _uiState.update { it.copy(cycleDay = day.coerceIn(1, 28)) }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val readingBd = try {
                BigDecimal(_uiState.value.initialReading.trim())
            } catch (e: NumberFormatException) {
                BigDecimal.ZERO
            }

            val meter = Meter(
                name = _uiState.value.meterName.ifBlank { "Household Meter" },
                type = _uiState.value.meterType,
                startReading = readingBd,
                multiplierFactor = BigDecimal.ONE,
                isActive = true,
                createdAt = DateUtils.currentInstant()
            )

            val savedMeterId = meterRepository.saveMeter(meter)
            val actualMeterId = if (savedMeterId > 0) savedMeterId else 1L

            val reading = Reading(
                id = 0,
                meterId = actualMeterId,
                readingValue = readingBd,
                recordedAt = _uiState.value.initialReadingDate,
                note = "Initial Reading",
                isEstimated = false,
                isMeterReset = false,
                createdAt = DateUtils.currentInstant(),
                updatedAt = DateUtils.currentInstant()
            )
            readingRepository.saveReading(reading)

            settingsPreferences.setBillingCycleDay(_uiState.value.cycleDay)
            settingsPreferences.setOnboardingCompleted(true)

            _uiState.update { it.copy(isSaving = false, isCompleted = true) }
        }
    }
}
