package com.powerunit.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerunit.core.common.DateUtils
import com.powerunit.domain.model.Reading
import com.powerunit.domain.model.Tariff
import com.powerunit.domain.repository.MeterRepository
import com.powerunit.domain.repository.ReadingRepository
import com.powerunit.domain.repository.TariffRepository
import com.powerunit.domain.usecase.CalculateBillUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

enum class HistoryFilter {
    ALL,
    ESTIMATED_ONLY,
    RESET_ONLY,
    HIGH_USAGE
}

data class ReadingItemUiModel(
    val id: Long,
    val readingValue: BigDecimal,
    val unitsDelta: BigDecimal,
    val costEstimate: BigDecimal,
    val formattedDate: String,
    val formattedTime: String,
    val isEstimated: Boolean,
    val isMeterReset: Boolean,
    val note: String?,
    val rawReading: Reading
)

data class MonthHistoryGroup(
    val monthTitle: String,
    val totalUnits: BigDecimal,
    val estimatedBill: BigDecimal,
    val items: List<ReadingItemUiModel>
)

data class HistoryUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val activeFilter: HistoryFilter = HistoryFilter.ALL,
    val monthGroups: List<MonthHistoryGroup> = emptyList(),
    val selectedDetailItem: ReadingItemUiModel? = null
)

sealed interface HistoryUiEffect {
    data class ShowUndoSnackbar(val reading: Reading, val message: String = "Reading deleted") : HistoryUiEffect
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val readingRepository: ReadingRepository,
    private val meterRepository: MeterRepository,
    private val tariffRepository: TariffRepository,
    private val calculateBillUseCase: CalculateBillUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _effects = Channel<HistoryUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var recentlyDeletedReading: Reading? = null

    init {
        loadReadings()
    }

    private fun loadReadings() {
        viewModelScope.launch {
            val meter = meterRepository.getActiveMeterSync()
            val meterId = meter?.id ?: 1L

            combine(
                readingRepository.getReadings(meterId),
                tariffRepository.getActiveTariff(),
                _uiState
            ) { readings, tariff, state ->
                processReadings(readings, tariff, state.searchQuery, state.activeFilter)
            }.collect { groups ->
                _uiState.update { it.copy(isLoading = false, monthGroups = groups) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onFilterChange(filter: HistoryFilter) {
        _uiState.update { it.copy(activeFilter = filter) }
    }

    fun onSelectItemForDetail(item: ReadingItemUiModel?) {
        _uiState.update { it.copy(selectedDetailItem = item) }
    }

    fun deleteReading(item: ReadingItemUiModel) {
        viewModelScope.launch {
            recentlyDeletedReading = item.rawReading
            readingRepository.deleteReading(item.id)
            _effects.send(HistoryUiEffect.ShowUndoSnackbar(item.rawReading))
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            val readingToRestore = recentlyDeletedReading ?: return@launch
            readingRepository.saveReading(readingToRestore)
            recentlyDeletedReading = null
        }
    }

    private fun processReadings(
        readings: List<Reading>,
        tariff: Tariff?,
        searchQuery: String,
        filter: HistoryFilter
    ): List<MonthHistoryGroup> {
        if (readings.isEmpty()) return emptyList()

        val activeTariff = tariff ?: Tariff(
            name = "Default",
            effectiveFrom = DateUtils.currentInstant(),
            vatPercent = BigDecimal("5.0"),
            serviceChargeFlat = BigDecimal("10.00"),
            demandChargeFlat = BigDecimal("35.00"),
            isActive = true
        )

        val items = mutableListOf<ReadingItemUiModel>()
        for (i in readings.indices) {
            val reading = readings[i]
            val prev = if (i < readings.size - 1) readings[i + 1] else null
            val item = createReadingItem(reading, prev)

            if (isEligible(item, searchQuery, filter)) {
                items.add(item)
            }
        }

        return groupIntoMonths(items, activeTariff)
    }

    private fun createReadingItem(reading: Reading, prev: Reading?): ReadingItemUiModel {
        val date = DateUtils.formatDate(reading.recordedAt)
        val time = DateUtils.formatTime(reading.recordedAt)

        val delta = if (reading.isMeterReset || prev == null) {
            BigDecimal.ZERO
        } else {
            (reading.readingValue - prev.readingValue).coerceAtLeast(BigDecimal.ZERO)
        }
        val cost = delta.multiply(BigDecimal("6.0")).setScale(2, RoundingMode.HALF_UP)

        return ReadingItemUiModel(
            id = reading.id,
            readingValue = reading.readingValue,
            unitsDelta = delta.setScale(1, RoundingMode.HALF_UP),
            costEstimate = cost,
            formattedDate = date,
            formattedTime = time,
            isEstimated = reading.isEstimated,
            isMeterReset = reading.isMeterReset,
            note = reading.note,
            rawReading = reading
        )
    }

    private fun isEligible(item: ReadingItemUiModel, searchQuery: String, filter: HistoryFilter): Boolean {
        val matchesSearch = searchQuery.isBlank() ||
            item.formattedDate.contains(searchQuery, ignoreCase = true) ||
            (item.note?.contains(searchQuery, ignoreCase = true) == true) ||
            item.readingValue.toPlainString().contains(searchQuery)

        val matchesFilter = when (filter) {
            HistoryFilter.ALL -> true
            HistoryFilter.ESTIMATED_ONLY -> item.isEstimated
            HistoryFilter.RESET_ONLY -> item.isMeterReset
            HistoryFilter.HIGH_USAGE -> item.unitsDelta > BigDecimal("20.0")
        }

        return matchesSearch && matchesFilter
    }

    private fun groupIntoMonths(items: List<ReadingItemUiModel>, activeTariff: Tariff): List<MonthHistoryGroup> {
        return items.groupBy { item ->
            DateUtils.formatDate(item.rawReading.recordedAt, "MMMM yyyy")
        }.map { (month, monthItems) ->
            val totalUnits = monthItems.fold(BigDecimal.ZERO) { acc, itm -> acc + itm.unitsDelta }
                .setScale(1, RoundingMode.HALF_UP)
            val monthBill = calculateBillUseCase(totalUnits, activeTariff).totalBill

            MonthHistoryGroup(
                monthTitle = month,
                totalUnits = totalUnits,
                estimatedBill = monthBill,
                items = monthItems
            )
        }
    }
}
