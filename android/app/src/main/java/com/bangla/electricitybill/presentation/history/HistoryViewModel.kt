package com.bangla.electricitybill.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.domain.engine.ConsumptionCalculator
import com.bangla.electricitybill.domain.engine.ProgressiveTariffEngine
import com.bangla.electricitybill.domain.model.IntervalConsumptionResult
import com.bangla.electricitybill.domain.model.MeterReading
import com.bangla.electricitybill.domain.model.ReadingSource
import com.bangla.electricitybill.domain.model.Tariff
import com.bangla.electricitybill.domain.repository.MeterReadingRepository
import com.bangla.electricitybill.domain.repository.MeterRepository
import com.bangla.electricitybill.domain.repository.TariffRepository
import com.bangla.electricitybill.domain.usecase.HistoricalRecalculationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class HistoryItem(
    val reading: MeterReading,
    val previousReading: MeterReading?,
    val deltaUnits: BigDecimal?,
    val elapsedDays: Long?,
    val energyCost: BigDecimal?,
    val isMissingDatesWarning: Boolean
)

data class HistoryUiState(
    val items: List<HistoryItem> = emptyList(),
    val activeTariff: Tariff? = null,
    val isLoading: Boolean = true,
    val selectedReadingForEdit: MeterReading? = null,
    val showDeleteConfirmDialog: Boolean = false,
    val readingToDelete: MeterReading? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val meterRepository: MeterRepository,
    private val readingRepository: MeterReadingRepository,
    private val tariffRepository: TariffRepository,
    private val recalculationUseCase: HistoricalRecalculationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        observeHistory()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            val meter = meterRepository.getActiveMeter() ?: return@launch
            combine(
                readingRepository.getReadingsFlow(meter.id),
                tariffRepository.getActiveTariffFlow()
            ) { readings, tariff ->
                val sorted = readings.sortedBy { it.readingDateTime }
                val historyList = mutableListOf<HistoryItem>()

                for (i in sorted.indices) {
                    val curr = sorted[i]
                    val prev = if (i > 0) sorted[i - 1] else null

                    if (prev != null) {
                        val isRollover = curr.readingSource == ReadingSource.ROLLOVER || curr.normalizedReading < prev.normalizedReading
                        val deltaRes = ConsumptionCalculator.calculateConsumption(
                            previousReading = prev.normalizedReading,
                            currentReading = curr.normalizedReading,
                            isRollover = isRollover,
                            blackDigitCapacity = meter.blackDigitCapacity
                        )
                        val delta = deltaRes.getOrDefault(BigDecimal.ZERO)
                        val days = DateUtils.daysBetween(prev.readingDateTime, curr.readingDateTime)
                        val cost = if (tariff != null) {
                            ProgressiveTariffEngine.calculateDifferentialEnergyCost(
                                previousCumulativeConsumption = BigDecimal.ZERO,
                                newCumulativeConsumption = delta,
                                tariff = tariff
                            )
                        } else null

                        historyList.add(
                            HistoryItem(
                                reading = curr,
                                previousReading = prev,
                                deltaUnits = delta,
                                elapsedDays = days,
                                energyCost = cost,
                                isMissingDatesWarning = days > 1
                            )
                        )
                    } else {
                        // Baseline reading
                        historyList.add(
                            HistoryItem(
                                reading = curr,
                                previousReading = null,
                                deltaUnits = null,
                                elapsedDays = null,
                                energyCost = null,
                                isMissingDatesWarning = false
                            )
                        )
                    }
                }

                _uiState.update {
                    it.copy(
                        items = historyList.reversed(), // Recent first
                        activeTariff = tariff,
                        isLoading = false
                    )
                }
            }.launchIn(viewModelScope)
        }
    }

    fun promptDeleteReading(reading: MeterReading) {
        _uiState.update { it.copy(showDeleteConfirmDialog = true, readingToDelete = reading) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteConfirmDialog = false, readingToDelete = null) }
    }

    fun confirmDeleteReading() {
        val reading = _uiState.value.readingToDelete ?: return
        viewModelScope.launch {
            readingRepository.deleteReading(reading.id)
            recalculationUseCase.recalculateAll(reading.meterId)
            dismissDeleteDialog()
        }
    }
}
