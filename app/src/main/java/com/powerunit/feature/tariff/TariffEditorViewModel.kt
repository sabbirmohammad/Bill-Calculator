package com.powerunit.feature.tariff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerunit.core.common.DateUtils
import com.powerunit.domain.model.BillBreakdown
import com.powerunit.domain.model.Tariff
import com.powerunit.domain.model.TariffSlab
import com.powerunit.domain.repository.TariffRepository
import com.powerunit.domain.usecase.CalculateBillUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class EditableSlab(
    val slabIndex: Int,
    val fromUnit: String,
    val toUnit: String,
    val ratePerUnit: String
)

data class TariffEditorUiState(
    val isLoading: Boolean = true,
    val tariffId: Long = 1L,
    val tariffName: String = "Bangladesh Electricity Tariff",
    val slabs: List<EditableSlab> = emptyList(),
    val demandCharge: String = "35.00",
    val serviceCharge: String = "10.00",
    val vatPercent: String = "5.0",
    val testUnits: String = "150",
    val previewBill: BillBreakdown? = null,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TariffEditorViewModel @Inject constructor(
    private val tariffRepository: TariffRepository,
    private val calculateBillUseCase: CalculateBillUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TariffEditorUiState())
    val uiState: StateFlow<TariffEditorUiState> = _uiState.asStateFlow()

    init {
        loadActiveTariff()
    }

    private fun loadActiveTariff() {
        viewModelScope.launch {
            val tariff = tariffRepository.getActiveTariffSync()
            if (tariff != null) {
                val editableSlabs = tariff.slabs.mapIndexed { idx, s ->
                    EditableSlab(
                        slabIndex = idx + 1,
                        fromUnit = s.fromUnit.stripTrailingZeros().toPlainString(),
                        toUnit = s.toUnit?.stripTrailingZeros()?.toPlainString() ?: "",
                        ratePerUnit = s.ratePerUnit.stripTrailingZeros().toPlainString()
                    )
                }
                _uiState.value = TariffEditorUiState(
                    isLoading = false,
                    tariffId = tariff.id,
                    tariffName = tariff.name,
                    slabs = editableSlabs,
                    demandCharge = tariff.demandChargeFlat.stripTrailingZeros().toPlainString(),
                    serviceCharge = tariff.serviceChargeFlat.stripTrailingZeros().toPlainString(),
                    vatPercent = tariff.vatPercent.stripTrailingZeros().toPlainString()
                )
            } else {
                _uiState.value = TariffEditorUiState(
                    isLoading = false,
                    slabs = listOf(
                        EditableSlab(1, "0", "75", "4.85"),
                        EditableSlab(2, "76", "200", "6.63"),
                        EditableSlab(3, "201", "300", "6.95")
                    )
                )
            }
            recomputePreview()
        }
    }

    fun updateTariffName(name: String) {
        _uiState.value = _uiState.value.copy(tariffName = name)
    }

    fun updateSlab(index: Int, from: String, to: String, rate: String) {
        val currentSlabs = _uiState.value.slabs.toMutableList()
        if (index in currentSlabs.indices) {
            currentSlabs[index] = currentSlabs[index].copy(
                fromUnit = from,
                toUnit = to,
                ratePerUnit = rate
            )
            _uiState.value = _uiState.value.copy(slabs = currentSlabs)
            recomputePreview()
        }
    }

    fun addSlab() {
        val currentSlabs = _uiState.value.slabs.toMutableList()
        val nextIndex = currentSlabs.size + 1
        val lastTo = currentSlabs.lastOrNull()?.toUnit?.toDoubleOrNull()?.toInt() ?: 0
        currentSlabs.add(
            EditableSlab(
                slabIndex = nextIndex,
                fromUnit = "${lastTo + 1}",
                toUnit = "",
                ratePerUnit = "8.00"
            )
        )
        _uiState.value = _uiState.value.copy(slabs = currentSlabs)
        recomputePreview()
    }

    fun removeSlab(index: Int) {
        val currentSlabs = _uiState.value.slabs.toMutableList()
        if (currentSlabs.size > 1 && index in currentSlabs.indices) {
            currentSlabs.removeAt(index)
            val reIndexed = currentSlabs.mapIndexed { idx, s -> s.copy(slabIndex = idx + 1) }
            _uiState.value = _uiState.value.copy(slabs = reIndexed)
            recomputePreview()
        }
    }

    fun updateDemandCharge(value: String) {
        _uiState.value = _uiState.value.copy(demandCharge = value)
        recomputePreview()
    }

    fun updateServiceCharge(value: String) {
        _uiState.value = _uiState.value.copy(serviceCharge = value)
        recomputePreview()
    }

    fun updateVatPercent(value: String) {
        _uiState.value = _uiState.value.copy(vatPercent = value)
        recomputePreview()
    }

    fun updateTestUnits(value: String) {
        _uiState.value = _uiState.value.copy(testUnits = value)
        recomputePreview()
    }

    private fun recomputePreview() {
        val testUnitsDecimal = _uiState.value.testUnits.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val tariff = buildTariffModel() ?: return
        val bill = calculateBillUseCase(testUnitsDecimal, tariff)
        _uiState.value = _uiState.value.copy(previewBill = bill)
    }

    private fun buildTariffModel(): Tariff? {
        val state = _uiState.value
        val domainSlabs = state.slabs.mapIndexed { idx, s ->
            val from = s.fromUnit.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val to = if (s.toUnit.isBlank()) null else s.toUnit.toBigDecimalOrNull()
            val rate = s.ratePerUnit.toBigDecimalOrNull() ?: BigDecimal.ZERO
            TariffSlab(
                id = (idx + 1).toLong(),
                tariffId = state.tariffId,
                fromUnit = from,
                toUnit = to,
                ratePerUnit = rate
            )
        }

        return Tariff(
            id = state.tariffId,
            name = state.tariffName,
            effectiveFrom = DateUtils.currentInstant(),
            vatPercent = state.vatPercent.toBigDecimalOrNull() ?: BigDecimal("5.0"),
            serviceChargeFlat = state.serviceCharge.toBigDecimalOrNull() ?: BigDecimal("10.00"),
            demandChargeFlat = state.demandCharge.toBigDecimalOrNull() ?: BigDecimal("35.00"),
            slabs = domainSlabs,
            isActive = true
        )
    }

    fun saveTariff() {
        viewModelScope.launch {
            val tariff = buildTariffModel() ?: return@launch
            tariffRepository.updateTariff(tariff)
            _uiState.value = _uiState.value.copy(saveSuccess = true)
        }
    }

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }
}
