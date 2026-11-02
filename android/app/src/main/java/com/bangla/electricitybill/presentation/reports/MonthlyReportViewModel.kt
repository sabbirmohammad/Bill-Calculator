package com.bangla.electricitybill.presentation.reports

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.domain.engine.ProgressiveTariffEngine
import com.bangla.electricitybill.domain.model.BillingPeriod
import com.bangla.electricitybill.domain.model.ItemizedBill
import com.bangla.electricitybill.domain.model.MeterReading
import com.bangla.electricitybill.domain.model.MonthlySummary
import com.bangla.electricitybill.domain.repository.BillingPeriodRepository
import com.bangla.electricitybill.domain.repository.MeterReadingRepository
import com.bangla.electricitybill.domain.repository.MeterRepository
import com.bangla.electricitybill.domain.repository.TariffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.YearMonth
import javax.inject.Inject

data class MonthlyReportUiState(
    val period: BillingPeriod? = null,
    val summary: MonthlySummary? = null,
    val itemizedBill: ItemizedBill? = null,
    val readingsInMonth: List<MeterReading> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class MonthlyReportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val billingPeriodRepository: BillingPeriodRepository,
    private val readingRepository: MeterReadingRepository,
    private val tariffRepository: TariffRepository
) : ViewModel() {

    private val periodId: String? = savedStateHandle["periodId"]
    private val _uiState = MutableStateFlow(MonthlyReportUiState())
    val uiState: StateFlow<MonthlyReportUiState> = _uiState.asStateFlow()

    init {
        loadReport()
    }

    private fun loadReport() {
        if (periodId == null) return
        viewModelScope.launch {
            val period = billingPeriodRepository.getBillingPeriodById(periodId)
            val summary = billingPeriodRepository.getMonthlySummary(periodId)
            val tariff = period?.tariffVersionId?.let { tariffRepository.getTariffById(it) }
                ?: tariffRepository.getActiveTariff()

            if (period != null && tariff != null) {
                val allReadings = readingRepository.getReadings(period.meterId)
                val yearMonth = YearMonth.from(period.periodStart)
                val monthReadings = allReadings.filter { YearMonth.from(it.readingDateTime) == yearMonth }

                val units = summary?.totalConsumption ?: BigDecimal.ZERO
                val itemized = ProgressiveTariffEngine.calculateMonthlyBill(units, tariff)

                _uiState.update {
                    it.copy(
                        period = period,
                        summary = summary,
                        itemizedBill = itemized,
                        readingsInMonth = monthReadings,
                        isLoading = false
                    )
                }
            }
        }
    }
}
