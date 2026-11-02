package com.bangla.electricitybill.presentation.bill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangla.electricitybill.core.util.BigDecimalMath
import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.domain.engine.ConsumptionCalculator
import com.bangla.electricitybill.domain.engine.ProgressiveTariffEngine
import com.bangla.electricitybill.domain.model.BillingPeriod
import com.bangla.electricitybill.domain.model.ItemizedBill
import com.bangla.electricitybill.domain.model.Meter
import com.bangla.electricitybill.domain.model.PeriodStatus
import com.bangla.electricitybill.domain.model.ReadingSource
import com.bangla.electricitybill.domain.model.Tariff
import com.bangla.electricitybill.domain.repository.BillingPeriodRepository
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class MonthlyBillUiState(
    val selectedMonth: YearMonth = YearMonth.from(DateUtils.now()),
    val activeMeter: Meter? = null,
    val activeTariff: Tariff? = null,
    val billingPeriod: BillingPeriod? = null,
    val itemizedBill: ItemizedBill? = null,
    val totalUnits: BigDecimal = BigDecimal.ZERO,
    val billingPeriodText: String = "",
    val status: PeriodStatus = PeriodStatus.IN_PROGRESS,
    val showEditTariffDialog: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class MonthlyBillViewModel @Inject constructor(
    private val meterRepository: MeterRepository,
    private val readingRepository: MeterReadingRepository,
    private val billingPeriodRepository: BillingPeriodRepository,
    private val tariffRepository: TariffRepository,
    private val recalculationUseCase: HistoricalRecalculationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MonthlyBillUiState())
    val uiState: StateFlow<MonthlyBillUiState> = _uiState.asStateFlow()

    private val _selectedMonth = MutableStateFlow(YearMonth.from(DateUtils.now()))

    init {
        loadMonthlyBill()
    }

    private fun loadMonthlyBill() {
        viewModelScope.launch {
            val meter = meterRepository.getActiveMeter() ?: return@launch
            combine(
                readingRepository.getReadingsFlow(meter.id),
                billingPeriodRepository.getBillingPeriodsFlow(meter.id),
                tariffRepository.getActiveTariffFlow(),
                _selectedMonth
            ) { readings, periods, tariff, selMonth ->
                val sorted = readings.sortedBy { it.readingDateTime }
                val monthReadings = sorted.filter { YearMonth.from(it.readingDateTime) == selMonth }

                // Period identification
                val periodId = DateUtils.getCycleId(meter.id, selMonth)
                val period = periods.find { it.id == periodId }

                // Calculate total consumption units in selected month
                val totalUnits = if (period?.openingReading != null && monthReadings.isNotEmpty()) {
                    val isRollover = monthReadings.any { it.readingSource == ReadingSource.ROLLOVER } ||
                        monthReadings.last().normalizedReading < period.openingReading
                    ConsumptionCalculator.calculateConsumption(
                        previousReading = period.openingReading,
                        currentReading = monthReadings.last().normalizedReading,
                        isRollover = isRollover,
                        blackDigitCapacity = meter.blackDigitCapacity
                    ).getOrDefault(BigDecimal.ZERO).max(BigDecimal.ZERO)
                } else if (monthReadings.size >= 2) {
                    val isRollover = monthReadings.any { it.readingSource == ReadingSource.ROLLOVER } ||
                        monthReadings.last().normalizedReading < monthReadings.first().normalizedReading
                    ConsumptionCalculator.calculateConsumption(
                        previousReading = monthReadings.first().normalizedReading,
                        currentReading = monthReadings.last().normalizedReading,
                        isRollover = isRollover,
                        blackDigitCapacity = meter.blackDigitCapacity
                    ).getOrDefault(BigDecimal.ZERO).max(BigDecimal.ZERO)
                } else {
                    BigDecimal.ZERO
                }

                // Calculate itemized bill
                val itemized = if (tariff != null) {
                    ProgressiveTariffEngine.calculateMonthlyBill(
                        monthlyConsumption = totalUnits,
                        tariff = tariff
                    )
                } else null

                // Format billing period string (e.g. "Jan 1 – Jan 31, 2025")
                val firstDay = selMonth.atDay(1)
                val lastDay = selMonth.atEndOfMonth()
                val monthName = selMonth.format(DateTimeFormatter.ofPattern("MMM"))
                val periodText = "$monthName 1 – $monthName ${lastDay.dayOfMonth}, ${selMonth.year}"

                val status = period?.periodStatus ?: if (totalUnits > BigDecimal.ZERO) PeriodStatus.IN_PROGRESS else PeriodStatus.PARTIAL_TRACKING

                _uiState.update {
                    it.copy(
                        selectedMonth = selMonth,
                        activeMeter = meter,
                        activeTariff = tariff,
                        billingPeriod = period,
                        itemizedBill = itemized,
                        totalUnits = totalUnits,
                        billingPeriodText = periodText,
                        status = status,
                        isLoading = false
                    )
                }
            }.launchIn(viewModelScope)
        }
    }

    fun selectMonth(month: YearMonth) {
        _selectedMonth.update { month }
    }

    fun openEditTariff() {
        _uiState.update { it.copy(showEditTariffDialog = true) }
    }

    fun closeEditTariff() {
        _uiState.update { it.copy(showEditTariffDialog = false) }
    }

    fun saveUpdatedTariff(updatedTariff: Tariff) {
        viewModelScope.launch {
            tariffRepository.saveTariff(updatedTariff)
            val meter = _uiState.value.activeMeter
            if (meter != null) {
                recalculationUseCase.recalculateAll(meter.id)
            }
        }
    }

    fun buildShareReportText(): String {
        val state = _uiState.value
        val bill = state.itemizedBill
        val monthStr = state.selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        val billAmount = bill?.totalBill?.let { BigDecimalMath.formatCurrency(it) } ?: "৳ 0.00"
        val units = bill?.totalUnits?.let { BigDecimalMath.formatKwh(it) } ?: "0.0"

        return """
            ⚡ Electricity Bill Summary — $monthStr
            ------------------------------------------------
            Total Consumed Units: $units kWh
            Billing Period: ${state.billingPeriodText}
            Energy Cost: ${bill?.energyCost?.let { BigDecimalMath.formatCurrency(it) } ?: "৳ 0.00"}
            Demand Charge: ${bill?.demandCharge?.let { BigDecimalMath.formatCurrency(it) } ?: "৳ 0.00"}
            Meter Rent: ${bill?.meterRent?.let { BigDecimalMath.formatCurrency(it) } ?: "৳ 0.00"}
            VAT (5%): ${bill?.vatAmount?.let { BigDecimalMath.formatCurrency(it) } ?: "৳ 0.00"}
            ------------------------------------------------
            Total Estimated Bill: $billAmount
            
            (Generated via Bangladesh Electricity Bill Calculator)
        """.trimIndent()
    }
}
