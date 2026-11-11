package com.powerunit.feature.tariff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerunit.core.common.DateUtils
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.domain.model.Tariff
import com.powerunit.domain.model.TariffSlab
import com.powerunit.domain.repository.TariffRepository
import com.powerunit.domain.usecase.CalculateCycleUsageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class EditableSlab(
    val slabIndex: Int,
    val fromUnit: String,
    val toUnit: String, // Blank means unlimited / open-ended
    val ratePerUnit: String
)

enum class TariffEffectiveScope {
    CURRENT_BILLING_CYCLE,
    TODAY
}

data class TariffDiffItem(
    val label: String,
    val oldValue: String,
    val newValue: String
)

data class TariffEditorUiState(
    val isLoading: Boolean = true,
    val tariffId: Long = 1L,
    val tariffName: String = "Bangladesh Electricity Tariff",
    val slabs: List<EditableSlab> = emptyList(),
    val demandCharge: String = "35.00",
    val serviceCharge: String = "10.00",
    val vatPercent: String = "5.0",
    val validationErrors: List<String> = emptyList(),
    val isDirty: Boolean = false,
    val showConfirmDialog: Boolean = false,
    val showUnsavedDialog: Boolean = false,
    val showHistorySheet: Boolean = false,
    val historyTariffs: List<Tariff> = emptyList(),
    val diffItems: List<TariffDiffItem> = emptyList(),
    val selectedScope: TariffEffectiveScope = TariffEffectiveScope.CURRENT_BILLING_CYCLE,
    val isAppLockEnabled: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TariffEditorViewModel @Inject constructor(
    private val tariffRepository: TariffRepository,
    private val settingsPreferences: SettingsPreferences,
    private val calculateCycleUsageUseCase: CalculateCycleUsageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TariffEditorUiState())
    val uiState: StateFlow<TariffEditorUiState> = _uiState.asStateFlow()

    private var originalTariff: Tariff? = null

    val appLockEnabled: StateFlow<Boolean> = settingsPreferences.appLockEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        loadActiveTariff()
    }

    fun loadActiveTariff() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val tariff = tariffRepository.getActiveTariffSync()
            originalTariff = tariff
            val isLock = settingsPreferences.appLockEnabled.first()

            if (tariff != null) {
                val editableSlabs = tariff.slabs.sortedBy { it.fromUnit }.mapIndexed { idx, s ->
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
                    vatPercent = tariff.vatPercent.stripTrailingZeros().toPlainString(),
                    isAppLockEnabled = isLock
                )
            } else {
                _uiState.value = TariffEditorUiState(
                    isLoading = false,
                    slabs = listOf(
                        EditableSlab(1, "0", "75", "4.85"),
                        EditableSlab(2, "76", "200", "6.63"),
                        EditableSlab(3, "201", "", "7.95")
                    ),
                    isAppLockEnabled = isLock
                )
            }
            recomputeDirtyAndValidation()
        }
    }

    fun updateTariffName(name: String) {
        _uiState.value = _uiState.value.copy(tariffName = name)
        recomputeDirtyAndValidation()
    }

    fun updateSlab(index: Int, from: String, to: String, rate: String) {
        val currentSlabs = _uiState.value.slabs.toMutableList()
        if (index in currentSlabs.indices) {
            currentSlabs[index] = currentSlabs[index].copy(
                fromUnit = from.trim(),
                toUnit = to.trim(),
                ratePerUnit = rate.trim()
            )
            _uiState.value = _uiState.value.copy(slabs = currentSlabs)
            recomputeDirtyAndValidation()
        }
    }

    fun addSlab(from: String = "", to: String = "", rate: String = "8.00") {
        val currentSlabs = _uiState.value.slabs.toMutableList()
        val nextIndex = currentSlabs.size + 1
        val lastTo = currentSlabs.lastOrNull()?.toUnit?.toDoubleOrNull()?.toInt()
        val calculatedFrom = if (from.isNotBlank()) from else if (lastTo != null) "${lastTo + 1}" else "0"

        // If the previous last slab was open-ended, give it an upper bound
        if (currentSlabs.isNotEmpty() && currentSlabs.last().toUnit.isBlank()) {
            val prevFrom = currentSlabs.last().fromUnit.toIntOrNull() ?: 0
            val autoTo = (prevFrom + 100).toString()
            val updatedPrev = currentSlabs.last().copy(toUnit = autoTo)
            currentSlabs[currentSlabs.lastIndex] = updatedPrev
        }

        currentSlabs.add(
            EditableSlab(
                slabIndex = nextIndex,
                fromUnit = calculatedFrom,
                toUnit = to.trim(),
                ratePerUnit = rate.trim()
            )
        )
        _uiState.value = _uiState.value.copy(slabs = currentSlabs)
        recomputeDirtyAndValidation()
    }

    fun removeSlab(index: Int) {
        val currentSlabs = _uiState.value.slabs.toMutableList()
        if (currentSlabs.size > 1 && index in currentSlabs.indices) {
            currentSlabs.removeAt(index)
            // Ensure final slab is open-ended
            val reIndexed = currentSlabs.mapIndexed { idx, s ->
                if (idx == currentSlabs.size - 1) {
                    s.copy(slabIndex = idx + 1, toUnit = "")
                } else {
                    s.copy(slabIndex = idx + 1)
                }
            }
            _uiState.value = _uiState.value.copy(slabs = reIndexed)
            recomputeDirtyAndValidation()
        }
    }

    fun updateDemandCharge(value: String) {
        _uiState.value = _uiState.value.copy(demandCharge = value.trim())
        recomputeDirtyAndValidation()
    }

    fun updateServiceCharge(value: String) {
        _uiState.value = _uiState.value.copy(serviceCharge = value.trim())
        recomputeDirtyAndValidation()
    }

    fun updateVatPercent(value: String) {
        _uiState.value = _uiState.value.copy(vatPercent = value.trim())
        recomputeDirtyAndValidation()
    }

    fun setScope(scope: TariffEffectiveScope) {
        _uiState.value = _uiState.value.copy(selectedScope = scope)
    }

    fun onSaveClicked() {
        val errors = validateTariff()
        if (errors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(validationErrors = errors)
            return
        }

        val diffs = calculateDiff()
        if (diffs.isEmpty() && !_uiState.value.isDirty) {
            _uiState.value = _uiState.value.copy(errorMessage = "No changes detected.")
            return
        }

        _uiState.value = _uiState.value.copy(
            diffItems = diffs,
            showConfirmDialog = true
        )
    }

    fun dismissConfirmDialog() {
        _uiState.value = _uiState.value.copy(showConfirmDialog = false)
    }

    fun onConfirmSave() {
        _uiState.value = _uiState.value.copy(showConfirmDialog = false)
        executeSave()
    }

    fun onBackRequested(): Boolean {
        if (_uiState.value.isDirty) {
            _uiState.value = _uiState.value.copy(showUnsavedDialog = true)
            return false
        }
        return true
    }

    fun dismissUnsavedDialog() {
        _uiState.value = _uiState.value.copy(showUnsavedDialog = false)
    }

    fun openHistorySheet() {
        viewModelScope.launch {
            val all = tariffRepository.getActiveTariffSync()
            val tariffs = mutableListOf<Tariff>()
            if (all != null) tariffs.add(all)
            _uiState.value = _uiState.value.copy(
                showHistorySheet = true,
                historyTariffs = tariffs
            )
        }
    }

    fun dismissHistorySheet() {
        _uiState.value = _uiState.value.copy(showHistorySheet = false)
    }

    fun revertToTariff(tariff: Tariff) {
        val editableSlabs = tariff.slabs.sortedBy { it.fromUnit }.mapIndexed { idx, s ->
            EditableSlab(
                slabIndex = idx + 1,
                fromUnit = s.fromUnit.stripTrailingZeros().toPlainString(),
                toUnit = s.toUnit?.stripTrailingZeros()?.toPlainString() ?: "",
                ratePerUnit = s.ratePerUnit.stripTrailingZeros().toPlainString()
            )
        }
        _uiState.value = _uiState.value.copy(
            showHistorySheet = false,
            tariffName = tariff.name,
            slabs = editableSlabs,
            demandCharge = tariff.demandChargeFlat.stripTrailingZeros().toPlainString(),
            serviceCharge = tariff.serviceChargeFlat.stripTrailingZeros().toPlainString(),
            vatPercent = tariff.vatPercent.stripTrailingZeros().toPlainString()
        )
        recomputeDirtyAndValidation()
    }

    private fun executeSave() {
        viewModelScope.launch {
            val state = _uiState.value
            val cycleDay = settingsPreferences.billingCycleDay.first()
            val todayDate = DateUtils.currentDhakaDate()

            val effectiveDate = when (state.selectedScope) {
                TariffEffectiveScope.CURRENT_BILLING_CYCLE -> {
                    val (start, _) = calculateCycleUsageUseCase.computeCycleRange(cycleDay, todayDate)
                    DateUtils.startOfDayInstant(start)
                }
                TariffEffectiveScope.TODAY -> DateUtils.currentInstant()
            }

            val domainSlabs = state.slabs.mapIndexed { idx, s ->
                val from = s.fromUnit.toBigDecimalOrNull() ?: BigDecimal.ZERO
                val to = if (s.toUnit.isBlank()) null else s.toUnit.toBigDecimalOrNull()
                val rate = s.ratePerUnit.toBigDecimalOrNull() ?: BigDecimal.ZERO
                TariffSlab(
                    id = (idx + 1).toLong(),
                    tariffId = 0,
                    fromUnit = from,
                    toUnit = to,
                    ratePerUnit = rate
                )
            }

            val newTariff = Tariff(
                id = 0,
                name = state.tariffName.ifBlank { "Bangladesh Residential Tariff" },
                effectiveFrom = effectiveDate,
                vatPercent = state.vatPercent.toBigDecimalOrNull() ?: BigDecimal("5.0"),
                serviceChargeFlat = state.serviceCharge.toBigDecimalOrNull() ?: BigDecimal("10.00"),
                demandChargeFlat = state.demandCharge.toBigDecimalOrNull() ?: BigDecimal("35.00"),
                slabs = domainSlabs,
                isActive = true
            )

            tariffRepository.saveNewTariffVersion(newTariff)
            originalTariff = newTariff

            _uiState.value = _uiState.value.copy(
                isDirty = false,
                saveSuccess = true,
                errorMessage = null
            )
        }
    }

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun validateTariff(): List<String> {
        val errors = mutableListOf<String>()
        val state = _uiState.value

        // VAT validation
        val vat = state.vatPercent.toBigDecimalOrNull()
        if (vat == null || vat < BigDecimal.ZERO || vat > BigDecimal("100")) {
            errors.add("VAT must be a valid percentage between 0% and 100%.")
        }

        // Fixed charges validation
        val demand = state.demandCharge.toBigDecimalOrNull()
        if (demand == null || demand < BigDecimal.ZERO) {
            errors.add("Demand charge must be a non-negative amount.")
        }
        val service = state.serviceCharge.toBigDecimalOrNull()
        if (service == null || service < BigDecimal.ZERO) {
            errors.add("Service charge must be a non-negative amount.")
        }

        // Slab validation
        if (state.slabs.isEmpty()) {
            errors.add("At least one consumption slab is required.")
            return errors
        }

        val slabCount = state.slabs.size
        for (i in 0 until slabCount) {
            val slab = state.slabs[i]
            val from = slab.fromUnit.toBigDecimalOrNull()
            if (from == null || from < BigDecimal.ZERO) {
                errors.add("Slab ${i + 1}: 'From' unit must be 0 or greater.")
            }

            val rate = slab.ratePerUnit.toBigDecimalOrNull()
            if (rate == null || rate < BigDecimal.ZERO) {
                errors.add("Slab ${i + 1}: Rate must be non-negative (>= ৳0).")
            }

            if (from != null) {
                if (i < slabCount - 1) {
                    // Not last slab: must have toUnit
                    val to = slab.toUnit.toBigDecimalOrNull()
                    if (to == null) {
                        errors.add("Slab ${i + 1}: Must specify an upper bound. Only the final slab can be open-ended.")
                    } else if (to <= from) {
                        errors.add("Slab ${i + 1}: 'To' bound ($to) must be greater than 'From' bound ($from).")
                    }
                } else if (slab.toUnit.isNotBlank()) {
                    // Last slab: must be open-ended or to > from
                    val to = slab.toUnit.toBigDecimalOrNull()
                    if (to != null && to <= from) {
                        errors.add("Slab ${i + 1}: 'To' bound ($to) must be greater than 'From' bound ($from).")
                    }
                }
            }
        }

        // Check continuity and non-overlapping
        for (i in 0 until slabCount - 1) {
            val currTo = state.slabs[i].toUnit.toBigDecimalOrNull()
            val nextFrom = state.slabs[i + 1].fromUnit.toBigDecimalOrNull()

            if (currTo != null && nextFrom != null) {
                if (nextFrom < currTo) {
                    errors.add("Slabs ${i + 1} and ${i + 2} overlap: Slab ${i + 1} ends at $currTo, but Slab ${i + 2} starts at $nextFrom.")
                } else if (nextFrom > currTo + BigDecimal.ONE) {
                    errors.add("Gap detected between Slab ${i + 1} (ends at $currTo) and Slab ${i + 2} (starts at $nextFrom).")
                }
            }
        }

        return errors
    }

    private fun calculateDiff(): List<TariffDiffItem> {
        val orig = originalTariff ?: return emptyList()
        val state = _uiState.value
        val diffs = mutableListOf<TariffDiffItem>()

        if (state.tariffName != orig.name) {
            diffs.add(TariffDiffItem("Plan Name", orig.name, state.tariffName))
        }

        val origDemand = orig.demandChargeFlat.stripTrailingZeros().toPlainString()
        if (state.demandCharge != origDemand) {
            diffs.add(TariffDiffItem("Demand Charge", "৳$origDemand", "৳${state.demandCharge}"))
        }

        val origService = orig.serviceChargeFlat.stripTrailingZeros().toPlainString()
        if (state.serviceCharge != origService) {
            diffs.add(TariffDiffItem("Service Charge", "৳$origService", "৳${state.serviceCharge}"))
        }

        val origVat = orig.vatPercent.stripTrailingZeros().toPlainString()
        if (state.vatPercent != origVat) {
            diffs.add(TariffDiffItem("VAT", "$origVat%", "${state.vatPercent}%"))
        }

        // Slabs diff
        if (state.slabs.size != orig.slabs.size) {
            diffs.add(TariffDiffItem("Slab Count", "${orig.slabs.size} slabs", "${state.slabs.size} slabs"))
        } else {
            val sortedOrigSlabs = orig.slabs.sortedBy { it.fromUnit }
            for (i in state.slabs.indices) {
                val current = state.slabs[i]
                val prev = sortedOrigSlabs[i]
                val prevFrom = prev.fromUnit.stripTrailingZeros().toPlainString()
                val prevTo = prev.toUnit?.stripTrailingZeros()?.toPlainString() ?: ""
                val prevRate = prev.ratePerUnit.stripTrailingZeros().toPlainString()

                if (current.fromUnit != prevFrom || current.toUnit != prevTo || current.ratePerUnit != prevRate) {
                    val oldLabel = "${prevFrom}-${if (prevTo.isBlank()) "∞" else prevTo}: ৳$prevRate"
                    val newLabel = "${current.fromUnit}-${if (current.toUnit.isBlank()) "∞" else current.toUnit}: ৳${current.ratePerUnit}"
                    diffs.add(TariffDiffItem("Slab ${i + 1}", oldLabel, newLabel))
                }
            }
        }

        return diffs
    }

    private fun recomputeDirtyAndValidation() {
        val orig = originalTariff
        val state = _uiState.value
        val isDirty = if (orig == null) {
            true
        } else {
            state.tariffName != orig.name ||
                state.demandCharge != orig.demandChargeFlat.stripTrailingZeros().toPlainString() ||
                state.serviceCharge != orig.serviceChargeFlat.stripTrailingZeros().toPlainString() ||
                state.vatPercent != orig.vatPercent.stripTrailingZeros().toPlainString() ||
                state.slabs.size != orig.slabs.size ||
                state.slabs.indices.any { i ->
                    val s = state.slabs[i]
                    val o = orig.slabs.getOrNull(i)
                    o == null ||
                        s.fromUnit != o.fromUnit.stripTrailingZeros().toPlainString() ||
                        s.toUnit != (o.toUnit?.stripTrailingZeros()?.toPlainString() ?: "") ||
                        s.ratePerUnit != o.ratePerUnit.stripTrailingZeros().toPlainString()
                }
        }
        val errors = validateTariff()
        _uiState.value = _uiState.value.copy(
            isDirty = isDirty,
            validationErrors = errors
        )
    }
}
