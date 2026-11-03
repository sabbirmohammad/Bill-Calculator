package com.bangla.electricitybill.presentation.reading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangla.electricitybill.core.util.BanglaDigitNormalizer
import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.data.datastore.PreferencesManager
import com.bangla.electricitybill.domain.engine.ConsumptionCalculator
import com.bangla.electricitybill.domain.engine.MeterReadingParser
import com.bangla.electricitybill.domain.engine.ProgressiveTariffEngine
import com.bangla.electricitybill.domain.model.Meter
import com.bangla.electricitybill.domain.model.MeterReading
import com.bangla.electricitybill.domain.model.Tariff
import com.bangla.electricitybill.domain.repository.MeterReadingRepository
import com.bangla.electricitybill.domain.repository.MeterRepository
import com.bangla.electricitybill.domain.repository.TariffRepository
import com.bangla.electricitybill.domain.usecase.SaveReadingUseCase
import com.bangla.electricitybill.domain.usecase.SaveReadingValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject

data class AddReadingUiState(
    val activeMeter: Meter? = null,
    val activeTariff: Tariff? = null,
    val previousReading: MeterReading? = null,
    val readingDateTime: LocalDateTime = DateUtils.now(),
    val isAutoDetectedDate: Boolean = true,
    val blackDigits: String = "",
    val redDigit: String = "",
    val reconstructedReading: BigDecimal? = null,
    val previewDeltaUnits: BigDecimal? = null,
    val previewElapsedDays: Long = 1L,
    val previewAvgDaily: BigDecimal? = null,
    val previewEnergyCost: BigDecimal? = null,
    val errorMessage: String? = null,
    val showRolloverDialog: Boolean = false,
    val rolloverValue: BigDecimal? = null,
    val isSaving: Boolean = false,
    val isSaveSuccess: Boolean = false,
    val isBangla: Boolean = false
)

@HiltViewModel
class AddReadingViewModel @Inject constructor(
    private val meterRepository: MeterRepository,
    private val readingRepository: MeterReadingRepository,
    private val tariffRepository: TariffRepository,
    private val saveReadingUseCase: SaveReadingUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddReadingUiState())
    val uiState: StateFlow<AddReadingUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            preferencesManager.appLanguage.collect { lang ->
                _uiState.update { it.copy(isBangla = lang == "bn") }
            }
        }
        viewModelScope.launch {
            val meter = meterRepository.getActiveMeter()
            val tariff = tariffRepository.getActiveTariff()
            val latest = meter?.let { readingRepository.getLatestReading(it.id) }
            _uiState.update {
                it.copy(
                    activeMeter = meter,
                    activeTariff = tariff,
                    previousReading = latest
                )
            }
        }
    }

    fun updateReadingDateTime(dateTime: LocalDateTime) {
        _uiState.update { state ->
            val updated = state.copy(
                readingDateTime = dateTime,
                isAutoDetectedDate = false,
                errorMessage = null
            )
            recalculateLivePreview(updated)
        }
    }

    fun updateBlackDigits(rawInput: String) {
        val normalized = BanglaDigitNormalizer.normalize(rawInput).filter { it.isDigit() }
        _uiState.update { state ->
            val updated = state.copy(blackDigits = normalized, errorMessage = null)
            recalculateLivePreview(updated)
        }
    }

    fun updateRedDigit(rawInput: String) {
        val normalized = BanglaDigitNormalizer.normalize(rawInput).filter { it.isDigit() }.take(1)
        _uiState.update { state ->
            val updated = state.copy(redDigit = normalized, errorMessage = null)
            recalculateLivePreview(updated)
        }
    }

    private fun recalculateLivePreview(state: AddReadingUiState): AddReadingUiState {
        val parseResult = MeterReadingParser.parse(state.blackDigits, state.redDigit)
        if (parseResult.isFailure) {
            return state.copy(
                reconstructedReading = null,
                previewDeltaUnits = null,
                previewAvgDaily = null,
                previewEnergyCost = null
            )
        }

        val currentVal = parseResult.getOrThrow()
        val prev = state.previousReading

        if (prev != null) {
            val deltaRes = ConsumptionCalculator.calculateConsumption(
                previousReading = prev.normalizedReading,
                currentReading = currentVal,
                blackDigitCapacity = state.activeMeter?.blackDigitCapacity ?: 5
            )

            if (deltaRes.isSuccess) {
                val delta = deltaRes.getOrThrow()
                val days = DateUtils.daysBetween(prev.readingDateTime, state.readingDateTime).coerceAtLeast(1L)
                val avg = ConsumptionCalculator.calculateAverageDaily(delta, days)
                val cost = if (state.activeTariff != null) {
                    ProgressiveTariffEngine.calculateDifferentialEnergyCost(
                        previousCumulativeConsumption = BigDecimal.ZERO,
                        newCumulativeConsumption = delta,
                        tariff = state.activeTariff
                    )
                } else null

                return state.copy(
                    reconstructedReading = currentVal,
                    previewDeltaUnits = delta,
                    previewElapsedDays = days,
                    previewAvgDaily = avg,
                    previewEnergyCost = cost,
                    errorMessage = null
                )
            } else {
                return state.copy(
                    reconstructedReading = currentVal,
                    previewDeltaUnits = null,
                    previewAvgDaily = null,
                    previewEnergyCost = null,
                    errorMessage = "Current reading cannot be lower than previous reading (${prev.normalizedReading} kWh)."
                )
            }
        }

        return state.copy(reconstructedReading = currentVal)
    }

    fun saveReading(isRolloverConfirmed: Boolean = false) {
        val state = _uiState.value
        val meter = state.activeMeter

        if (meter == null) {
            _uiState.update { it.copy(errorMessage = "Active meter not found.") }
            return
        }

        if (state.blackDigits.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter the black digits.") }
            return
        }

        if (state.redDigit.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter the red decimal digit.") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            val result = saveReadingUseCase.saveReading(
                meterId = meter.id,
                blackDigits = state.blackDigits,
                redDigit = state.redDigit,
                readingDateTime = state.readingDateTime,
                isRolloverConfirmed = isRolloverConfirmed
            )

            when (result) {
                is SaveReadingValidationResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, isSaveSuccess = true) }
                }
                is SaveReadingValidationResult.RequiresRolloverConfirmation -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            showRolloverDialog = true,
                            rolloverValue = result.readingValue
                        )
                    }
                }
                is SaveReadingValidationResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun dismissRolloverDialog() {
        _uiState.update { it.copy(showRolloverDialog = false) }
    }
}
