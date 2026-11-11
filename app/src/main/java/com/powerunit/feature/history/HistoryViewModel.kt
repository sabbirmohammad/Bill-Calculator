package com.powerunit.feature.history

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerunit.core.common.DateUtils
import com.powerunit.core.common.Formatters
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.core.export.ExportManager
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.YearMonth
import javax.inject.Inject

val englishShortMonths = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
val banglaShortMonths = listOf("জানু", "ফেব্রু", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টে", "অক্টো", "নভে", "ডিসে")
val englishFullMonths = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
val banglaFullMonths = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")

data class ReadingItemUiModel(
    val id: Long,
    val readingValue: BigDecimal,
    val unitsDelta: BigDecimal,
    val costEstimate: BigDecimal,
    val formattedDate: String,
    val formattedTime: String,
    val dayString: String = "",
    val monthString: String = "",
    val isEstimated: Boolean,
    val isMeterReset: Boolean,
    val note: String?,
    val rawReading: Reading
)

fun Reading.toUiModel(
    prevReading: Reading? = null,
    useBengaliDigits: Boolean = false,
    isBangla: Boolean = false
): ReadingItemUiModel {
    val date = DateUtils.formatDate(recordedAt)
    val time = DateUtils.formatTime(recordedAt)
    val ldt = recordedAt.toLocalDateTime(DateUtils.DHAKA_ZONE)
    val dayNumber = ldt.dayOfMonth
    val monthNumber = ldt.monthNumber
    val rawDayStr = dayNumber.toString().padStart(2, '0')
    val dayString = if (useBengaliDigits) Formatters.toBengaliDigits(rawDayStr) else rawDayStr
    val monthString = if (isBangla) {
        banglaShortMonths.getOrElse(monthNumber - 1) { "DATE" }
    } else {
        englishShortMonths.getOrElse(monthNumber - 1) { "DATE" }
    }

    val delta = if (isMeterReset || prevReading == null) {
        BigDecimal.ZERO
    } else {
        (readingValue - prevReading.readingValue).coerceAtLeast(BigDecimal.ZERO)
    }
    val cost = delta.multiply(BigDecimal("6.0")).setScale(2, RoundingMode.HALF_UP)

    return ReadingItemUiModel(
        id = id,
        readingValue = readingValue,
        unitsDelta = delta.setScale(1, RoundingMode.HALF_UP),
        costEstimate = cost,
        formattedDate = date,
        formattedTime = time,
        dayString = dayString,
        monthString = monthString,
        isEstimated = isEstimated,
        isMeterReset = isMeterReset,
        note = note,
        rawReading = this
    )
}

data class MonthSummaryUiModel(
    val totalUnits: BigDecimal,
    val totalCost: BigDecimal,
    val readingsCount: Int,
    val avgPerDay: BigDecimal
)

data class MonthChipUiModel(
    val monthNumber: Int,
    val label: String,
    val hasData: Boolean,
    val isSelected: Boolean
)

data class MonthHistoryGroup(
    val monthTitle: String,
    val totalUnits: BigDecimal,
    val estimatedBill: BigDecimal,
    val items: List<ReadingItemUiModel>
)

data class HistoryUiState(
    val isLoading: Boolean = true,
    val selectedYear: Int = 2026,
    val selectedMonth: Int = 11,
    val availableYears: List<Int> = listOf(2026),
    val monthChips: List<MonthChipUiModel> = emptyList(),
    val monthSummary: MonthSummaryUiModel? = null,
    val monthGroups: List<MonthHistoryGroup> = emptyList(),
    val items: List<ReadingItemUiModel> = emptyList(),
    val selectedDetailItem: ReadingItemUiModel? = null,
    val useBengaliDigits: Boolean = false,
    val appLanguage: String = "en"
)

sealed interface HistoryUiEffect {
    data class ShowUndoSnackbar(val reading: Reading, val message: String = "Reading deleted") : HistoryUiEffect
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val readingRepository: ReadingRepository,
    private val meterRepository: MeterRepository,
    private val tariffRepository: TariffRepository,
    private val calculateBillUseCase: CalculateBillUseCase,
    private val exportManager: ExportManager,
    private val settingsPreferences: SettingsPreferences
) : ViewModel() {

    companion object {
        const val KEY_SELECTED_YEAR = "history_selected_year"
        const val KEY_SELECTED_MONTH = "history_selected_month"
    }

    private val currentDhaka = DateUtils.currentDhakaDateTime()
    val defaultYear = currentDhaka.year
    val defaultMonth = currentDhaka.monthNumber

    private val _selectedYear = savedStateHandle.getStateFlow(KEY_SELECTED_YEAR, defaultYear)
    private val _selectedMonth = savedStateHandle.getStateFlow(KEY_SELECTED_MONTH, defaultMonth)

    private val _uiState = MutableStateFlow(
        HistoryUiState(
            selectedYear = _selectedYear.value,
            selectedMonth = _selectedMonth.value
        )
    )
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

            val prefsFlow = combine(
                settingsPreferences.useBengaliDigits,
                settingsPreferences.appLanguage
            ) { useBengaliDigits, appLanguage ->
                Pair(useBengaliDigits, appLanguage)
            }

            combine(
                readingRepository.getReadings(meterId),
                tariffRepository.getActiveTariff(),
                _selectedYear,
                _selectedMonth,
                prefsFlow
            ) { readings, tariff, selectedYear, selectedMonth, prefs ->
                val (useBengaliDigits, appLanguage) = prefs
                processHistoryData(
                    readings = readings,
                    tariff = tariff,
                    selectedYear = selectedYear,
                    selectedMonth = selectedMonth,
                    useBengaliDigits = useBengaliDigits,
                    appLanguage = appLanguage
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun processHistoryData(
        readings: List<Reading>,
        tariff: Tariff?,
        selectedYear: Int,
        selectedMonth: Int,
        useBengaliDigits: Boolean,
        appLanguage: String
    ): HistoryUiState {
        val isBangla = appLanguage.equals("bn", ignoreCase = true) || useBengaliDigits

        // 1. Determine available years (years with data + default/current year)
        val yearsWithData = readings.map { reading ->
            reading.recordedAt.toLocalDateTime(DateUtils.DHAKA_ZONE).year
        }.distinct()
        val availableYears = (yearsWithData + defaultYear).distinct().sortedDescending()

        // 2. Fallback active tariff
        val activeTariff = tariff ?: Tariff(
            name = "Default",
            effectiveFrom = DateUtils.currentInstant(),
            vatPercent = BigDecimal("5.0"),
            serviceChargeFlat = BigDecimal("10.00"),
            demandChargeFlat = BigDecimal("35.00"),
            isActive = true
        )

        // 3. Process all readings into UI models with delta computed against adjacent chronologically preceding reading
        val allUiItems = mutableListOf<ReadingItemUiModel>()
        for (i in readings.indices) {
            val reading = readings[i]
            val prev = if (i < readings.size - 1) readings[i + 1] else null
            allUiItems.add(reading.toUiModel(prev, useBengaliDigits, isBangla))
        }

        // 4. Months with data in the currently selected year
        val monthsWithDataInYear = readings.filter { reading ->
            reading.recordedAt.toLocalDateTime(DateUtils.DHAKA_ZONE).year == selectedYear
        }.map { reading ->
            reading.recordedAt.toLocalDateTime(DateUtils.DHAKA_ZONE).monthNumber
        }.toSet()

        // 5. Month chips Jan..Dec
        val monthChips = (1..12).map { monthNum ->
            val label = if (isBangla) {
                banglaShortMonths.getOrElse(monthNum - 1) { "M$monthNum" }
            } else {
                englishShortMonths.getOrElse(monthNum - 1) { "M$monthNum" }
            }
            MonthChipUiModel(
                monthNumber = monthNum,
                label = label,
                hasData = monthsWithDataInYear.contains(monthNum),
                isSelected = monthNum == selectedMonth
            )
        }

        // 6. Filter readings belonging specifically to the selected month & year
        val selectedMonthItems = allUiItems.filter { item ->
            val ldt = item.rawReading.recordedAt.toLocalDateTime(DateUtils.DHAKA_ZONE)
            ldt.year == selectedYear && ldt.monthNumber == selectedMonth
        }

        // 7. Calculate month summary metrics
        val monthSummary = if (selectedMonthItems.isNotEmpty()) {
            val totalUnits = selectedMonthItems.fold(BigDecimal.ZERO) { acc, itm ->
                acc + itm.unitsDelta
            }.setScale(1, RoundingMode.HALF_UP)
            val monthBill = calculateBillUseCase(totalUnits, activeTariff).totalBill
            val count = selectedMonthItems.size
            val daysInMonth = try {
                YearMonth.of(selectedYear, selectedMonth).lengthOfMonth()
            } catch (e: Exception) {
                30
            }
            val avgPerDay = if (daysInMonth > 0 && totalUnits > BigDecimal.ZERO) {
                totalUnits.divide(BigDecimal(daysInMonth), 1, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }
            MonthSummaryUiModel(
                totalUnits = totalUnits,
                totalCost = monthBill,
                readingsCount = count,
                avgPerDay = avgPerDay
            )
        } else {
            null
        }

        val monthName = if (isBangla) {
            banglaFullMonths.getOrElse(selectedMonth - 1) { "মাস" }
        } else {
            englishFullMonths.getOrElse(selectedMonth - 1) { "Month" }
        }
        val monthTitle = "$monthName $selectedYear"

        val monthGroups = if (selectedMonthItems.isNotEmpty() && monthSummary != null) {
            listOf(
                MonthHistoryGroup(
                    monthTitle = monthTitle,
                    totalUnits = monthSummary.totalUnits,
                    estimatedBill = monthSummary.totalCost,
                    items = selectedMonthItems
                )
            )
        } else {
            emptyList()
        }

        return HistoryUiState(
            isLoading = false,
            selectedYear = selectedYear,
            selectedMonth = selectedMonth,
            availableYears = availableYears,
            monthChips = monthChips,
            monthSummary = monthSummary,
            monthGroups = monthGroups,
            items = selectedMonthItems,
            selectedDetailItem = _uiState.value.selectedDetailItem,
            useBengaliDigits = useBengaliDigits,
            appLanguage = appLanguage
        )
    }

    fun onSelectYear(year: Int) {
        savedStateHandle[KEY_SELECTED_YEAR] = year
    }

    fun onSelectMonth(month: Int) {
        savedStateHandle[KEY_SELECTED_MONTH] = month
    }

    fun onPreviousYear() {
        val current = _selectedYear.value
        val available = _uiState.value.availableYears
        val prev = available.filter { it < current }.maxOrNull() ?: (current - 1)
        onSelectYear(prev)
    }

    fun onNextYear() {
        val current = _selectedYear.value
        val available = _uiState.value.availableYears
        val next = available.filter { it > current }.minOrNull() ?: (current + 1)
        if (next <= defaultYear) {
            onSelectYear(next)
        }
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

    fun exportPdf(onReady: (Intent) -> Unit) {
        viewModelScope.launch {
            val meter = meterRepository.getActiveMeterSync()
            val meterId = meter?.id ?: 1L
            val tariff = tariffRepository.getActiveTariff().first() ?: Tariff(
                name = "Default",
                effectiveFrom = DateUtils.currentInstant(),
                vatPercent = BigDecimal("5.0"),
                serviceChargeFlat = BigDecimal("10.00"),
                demandChargeFlat = BigDecimal("35.00"),
                isActive = true
            )
            val currentItems = _uiState.value.items
            val totalUnits = currentItems.fold(BigDecimal.ZERO) { acc, r -> acc + r.unitsDelta }
            val bill = calculateBillUseCase(totalUnits.coerceAtLeast(BigDecimal.ONE), tariff)
            val file = exportManager.exportBillToPdf(bill, meter, null)
            val intent = exportManager.getShareIntent(file, "application/pdf")
            onReady(intent)
        }
    }
}
