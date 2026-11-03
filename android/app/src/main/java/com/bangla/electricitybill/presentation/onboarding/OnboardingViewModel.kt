package com.bangla.electricitybill.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangla.electricitybill.core.util.BanglaDigitNormalizer
import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.data.datastore.PreferencesManager
import com.bangla.electricitybill.domain.engine.MeterReadingParser
import com.bangla.electricitybill.domain.model.AdditionalCharge
import com.bangla.electricitybill.domain.model.AdditionalChargeType
import com.bangla.electricitybill.domain.model.ChargeCalculationMethod
import com.bangla.electricitybill.domain.model.Meter
import com.bangla.electricitybill.domain.model.MeterEvent
import com.bangla.electricitybill.domain.model.MeterEventType
import com.bangla.electricitybill.domain.model.MeterReading
import com.bangla.electricitybill.domain.model.MeterStatus
import com.bangla.electricitybill.domain.model.ReadingSource
import com.bangla.electricitybill.domain.model.Tariff
import com.bangla.electricitybill.domain.model.TariffBillingMode
import com.bangla.electricitybill.domain.model.TariffCalculationMethod
import com.bangla.electricitybill.domain.model.TariffSlab
import com.bangla.electricitybill.domain.repository.MeterReadingRepository
import com.bangla.electricitybill.domain.repository.MeterRepository
import com.bangla.electricitybill.domain.repository.TariffRepository
import com.bangla.electricitybill.domain.usecase.HistoricalRecalculationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

data class EditableSlabState(
    val min: String,
    val max: String?,
    val rate: String
)

data class OnboardingUiState(
    val currentStep: Int = 1, // 1: Meter Name, 2: Current Units, 3: Slabs & Rates, 4: Optional Charges

    // Step 1: Meter Info
    val meterName: String = "বাসা - মূল মিটার",
    val meterNumber: String = "",
    val blackDigitCapacity: Int = 5,

    // Step 2: Current Units (Baseline Reading)
    val baselineBlackDigits: String = "12500",
    val baselineRedDigit: String = "0",
    val baselineDateTime: LocalDateTime = DateUtils.now(),
    val reconstructedBaseline: BigDecimal? = BigDecimal("12500.0"),

    // Step 3: Area Slab Config & Rates
    val billingMode: TariffBillingMode = TariffBillingMode.SLAB_BASED,
    val flatRate: String = "8.00",
    val selectedPreset: String = "BERC_6", // "BERC_6", "STANDARD_3", "FLAT_1", "CUSTOM"
    val slabs: List<EditableSlabState> = listOf(
        EditableSlabState("0", "75", "5.26"),
        EditableSlabState("75", "200", "7.20"),
        EditableSlabState("200", "300", "7.59"),
        EditableSlabState("300", "400", "8.02"),
        EditableSlabState("400", "600", "12.67"),
        EditableSlabState("600", null, "14.61")
    ),

    // Step 4: Optional Charges (Service Charge, Demand Charge, Meter Rent, VAT)
    val isServiceChargeEnabled: Boolean = false,
    val serviceCharge: String = "",
    val isDemandChargeEnabled: Boolean = false,
    val demandCharge: String = "",
    val isMeterRentEnabled: Boolean = false,
    val meterRent: String = "",
    val isVatEnabled: Boolean = false,
    val vatPercentage: String = "",

    val errorMessage: String? = null,
    val isCompleting: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val meterRepository: MeterRepository,
    private val tariffRepository: TariffRepository,
    private val readingRepository: MeterReadingRepository,
    private val preferencesManager: PreferencesManager,
    private val recalculationUseCase: HistoricalRecalculationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun nextStep() {
        val current = _uiState.value.currentStep
        when (current) {
            1 -> {
                // Step 1 Validation: Meter Name is required
                if (_uiState.value.meterName.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "দয়া করে মিটারের একটি নাম লিখুন (Meter name is required)") }
                    return
                }
            }
            2 -> {
                // Step 2 Validation: Current reading must be valid
                val parseResult = MeterReadingParser.parse(
                    _uiState.value.baselineBlackDigits,
                    _uiState.value.baselineRedDigit
                )
                if (parseResult.isFailure) {
                    _uiState.update { it.copy(errorMessage = "সঠিক কারেন্ট ইউনিট রিডিং প্রদান করুন") }
                    return
                }
            }
            3 -> {
                // Step 3 Validation: All slab rates must be valid numbers >= 0
                if (_uiState.value.billingMode == TariffBillingMode.FLAT_RATE) {
                    val rate = _uiState.value.flatRate.toDoubleOrNull()
                    if (rate == null || rate < 0) {
                        _uiState.update { it.copy(errorMessage = "সঠিক ফ্ল্যাট রেট প্রদান করুন") }
                        return
                    }
                } else {
                    val invalidSlab = _uiState.value.slabs.any {
                        it.rate.isBlank() || (it.rate.toDoubleOrNull() ?: -1.0) < 0
                    }
                    if (invalidSlab) {
                        _uiState.update { it.copy(errorMessage = "সকল স্ল্যাবের জন্য সঠিক ইউনিট দর প্রদান করুন") }
                        return
                    }
                }
            }
        }

        if (current < 4) {
            _uiState.update { it.copy(currentStep = current + 1, errorMessage = null) }
        }
    }

    fun previousStep() {
        val current = _uiState.value.currentStep
        if (current > 1) {
            _uiState.update { it.copy(currentStep = current - 1, errorMessage = null) }
        }
    }

    fun goToStep(step: Int) {
        if (step in 1..4) {
            _uiState.update { it.copy(currentStep = step, errorMessage = null) }
        }
    }

    // --- Step 1: Meter Info Updates ---
    fun updateMeterName(name: String) = _uiState.update { it.copy(meterName = name, errorMessage = null) }
    fun updateMeterNumber(number: String) = _uiState.update { it.copy(meterNumber = number) }
    fun updateBlackCapacity(capacity: Int) = _uiState.update { it.copy(blackDigitCapacity = capacity) }

    // --- Step 2: Current Units / Baseline Updates ---
    fun updateBaselineBlackDigits(digits: String) {
        val normalized = BanglaDigitNormalizer.normalize(digits).filter { it.isDigit() }
        _uiState.update { state ->
            val result = MeterReadingParser.parse(normalized, state.baselineRedDigit)
            state.copy(
                baselineBlackDigits = normalized,
                reconstructedBaseline = result.getOrNull(),
                errorMessage = null
            )
        }
    }

    fun updateBaselineRedDigit(digit: String) {
        val normalized = BanglaDigitNormalizer.normalize(digit).filter { it.isDigit() }.take(1)
        _uiState.update { state ->
            val result = MeterReadingParser.parse(state.baselineBlackDigits, normalized)
            state.copy(
                baselineRedDigit = normalized,
                reconstructedBaseline = result.getOrNull(),
                errorMessage = null
            )
        }
    }

    // --- Step 3: Area Slab Config & Rates Updates ---
    fun updateBillingMode(mode: TariffBillingMode) = _uiState.update { it.copy(billingMode = mode, errorMessage = null) }
    fun updateFlatRate(rate: String) = _uiState.update { it.copy(flatRate = rate) }

    fun updateSlabRate(index: Int, newRate: String) {
        val normalized = BanglaDigitNormalizer.normalize(newRate).filter { it.isDigit() || it == '.' }
        _uiState.update { state ->
            val updated = state.slabs.toMutableList()
            if (index in updated.indices) {
                updated[index] = updated[index].copy(rate = normalized)
            }
            state.copy(slabs = updated, selectedPreset = "CUSTOM", errorMessage = null)
        }
    }

    fun updateSlabMin(index: Int, newMin: String) {
        val normalized = BanglaDigitNormalizer.normalize(newMin).filter { it.isDigit() }
        _uiState.update { state ->
            val updated = state.slabs.toMutableList()
            if (index in updated.indices) {
                updated[index] = updated[index].copy(min = normalized)
            }
            state.copy(slabs = updated, selectedPreset = "CUSTOM")
        }
    }

    fun updateSlabMax(index: Int, newMax: String?) {
        val normalized = newMax?.let { BanglaDigitNormalizer.normalize(it).filter { c -> c.isDigit() } }
        _uiState.update { state ->
            val updated = state.slabs.toMutableList()
            if (index in updated.indices) {
                updated[index] = updated[index].copy(max = normalized?.ifBlank { null })
            }
            state.copy(slabs = updated, selectedPreset = "CUSTOM")
        }
    }

    fun addSlab() {
        _uiState.update { state ->
            val currentSlabs = state.slabs.toMutableList()
            val lastIdx = currentSlabs.size - 1
            if (lastIdx >= 0) {
                val lastSlab = currentSlabs[lastIdx]
                val prevMin = lastSlab.min.toIntOrNull() ?: 0
                val fixedMax = lastSlab.max ?: (prevMin + 100).toString()
                currentSlabs[lastIdx] = lastSlab.copy(max = fixedMax)
                val newMin = fixedMax
                currentSlabs.add(EditableSlabState(min = newMin, max = null, rate = "10.00"))
            } else {
                currentSlabs.add(EditableSlabState(min = "0", max = null, rate = "7.00"))
            }
            state.copy(slabs = currentSlabs, selectedPreset = "CUSTOM")
        }
    }

    fun removeSlab() {
        _uiState.update { state ->
            if (state.slabs.size > 1) {
                val currentSlabs = state.slabs.dropLast(1).toMutableList()
                val lastIdx = currentSlabs.size - 1
                currentSlabs[lastIdx] = currentSlabs[lastIdx].copy(max = null)
                state.copy(slabs = currentSlabs, selectedPreset = "CUSTOM")
            } else {
                state
            }
        }
    }

    fun applyPreset(presetKey: String) {
        when (presetKey) {
            "BERC_6" -> {
                _uiState.update {
                    it.copy(
                        selectedPreset = "BERC_6",
                        billingMode = TariffBillingMode.SLAB_BASED,
                        slabs = listOf(
                            EditableSlabState("0", "75", "5.26"),
                            EditableSlabState("75", "200", "7.20"),
                            EditableSlabState("200", "300", "7.59"),
                            EditableSlabState("300", "400", "8.02"),
                            EditableSlabState("400", "600", "12.67"),
                            EditableSlabState("600", null, "14.61")
                        )
                    )
                }
            }
            "STANDARD_3" -> {
                _uiState.update {
                    it.copy(
                        selectedPreset = "STANDARD_3",
                        billingMode = TariffBillingMode.SLAB_BASED,
                        slabs = listOf(
                            EditableSlabState("0", "100", "6.00"),
                            EditableSlabState("100", "300", "8.50"),
                            EditableSlabState("300", null, "12.00")
                        )
                    )
                }
            }
            "FLAT_1" -> {
                _uiState.update {
                    it.copy(
                        selectedPreset = "FLAT_1",
                        billingMode = TariffBillingMode.FLAT_RATE,
                        flatRate = "8.00",
                        slabs = listOf(
                            EditableSlabState("0", null, "8.00")
                        )
                    )
                }
            }
        }
    }

    // --- Step 4: Optional Charges Updates ---
    fun setServiceChargeEnabled(enabled: Boolean) = _uiState.update {
        it.copy(isServiceChargeEnabled = enabled, serviceCharge = if (enabled && it.serviceCharge.isBlank()) "30.00" else it.serviceCharge)
    }
    fun updateServiceCharge(charge: String) {
        val normalized = BanglaDigitNormalizer.normalize(charge).filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(serviceCharge = normalized) }
    }

    fun setDemandChargeEnabled(enabled: Boolean) = _uiState.update {
        it.copy(isDemandChargeEnabled = enabled, demandCharge = if (enabled && it.demandCharge.isBlank()) "42.00" else it.demandCharge)
    }
    fun updateDemandCharge(charge: String) {
        val normalized = BanglaDigitNormalizer.normalize(charge).filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(demandCharge = normalized) }
    }

    fun setMeterRentEnabled(enabled: Boolean) = _uiState.update {
        it.copy(isMeterRentEnabled = enabled, meterRent = if (enabled && it.meterRent.isBlank()) "10.00" else it.meterRent)
    }
    fun updateMeterRent(rent: String) {
        val normalized = BanglaDigitNormalizer.normalize(rent).filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(meterRent = normalized) }
    }

    fun setVatEnabled(enabled: Boolean) = _uiState.update {
        it.copy(isVatEnabled = enabled, vatPercentage = if (enabled && it.vatPercentage.isBlank()) "5.00" else it.vatPercentage)
    }
    fun updateVatPercentage(vat: String) {
        val normalized = BanglaDigitNormalizer.normalize(vat).filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(vatPercentage = normalized) }
    }

    // --- Complete Setup ---
    fun completeOnboarding(onSuccess: () -> Unit) {
        val state = _uiState.value
        val parseResult = MeterReadingParser.parse(state.baselineBlackDigits, state.baselineRedDigit)
        if (parseResult.isFailure) {
            _uiState.update { it.copy(errorMessage = parseResult.exceptionOrNull()?.message) }
            return
        }

        val baselineReadingValue = parseResult.getOrThrow()
        _uiState.update { it.copy(isCompleting = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val meterId = UUID.randomUUID().toString()
                val tariffId = UUID.randomUUID().toString()
                val now = DateUtils.now()

                // 1. Create and save meter
                val meter = Meter(
                    id = meterId,
                    meterName = state.meterName.ifBlank { "বাসা - মূল মিটার" },
                    meterNumber = state.meterNumber.ifBlank { null },
                    meterType = "MECHANICAL_COUNTER",
                    blackDigitCapacity = state.blackDigitCapacity,
                    decimalPlaces = 1,
                    meterStatus = MeterStatus.ACTIVE,
                    installedAt = state.baselineDateTime,
                    createdAt = now
                )
                meterRepository.saveMeter(meter)

                // 2. Create and save tariff with slabs and optional charges
                val isFlat = state.billingMode == TariffBillingMode.FLAT_RATE
                val slabs = if (isFlat) {
                    val flatVal = BigDecimal(state.flatRate.ifBlank { "8.00" })
                    listOf(
                        TariffSlab(
                            id = UUID.randomUUID().toString(),
                            tariffId = tariffId,
                            minimumUnits = BigDecimal.ZERO,
                            maximumUnits = null,
                            ratePerUnit = flatVal,
                            displayOrder = 1
                        )
                    )
                } else {
                    state.slabs.mapIndexed { idx, s ->
                        TariffSlab(
                            id = UUID.randomUUID().toString(),
                            tariffId = tariffId,
                            minimumUnits = BigDecimal(s.min.ifBlank { "0" }),
                            maximumUnits = s.max?.ifBlank { null }?.let { BigDecimal(it) },
                            ratePerUnit = BigDecimal(s.rate.ifBlank { "0.0" }),
                            displayOrder = idx + 1
                        )
                    }
                }

                val charges = mutableListOf<AdditionalCharge>()

                // Service Charge (Optional)
                if (state.isServiceChargeEnabled && state.serviceCharge.isNotBlank()) {
                    val amt = state.serviceCharge.toDoubleOrNull() ?: 0.0
                    if (amt > 0.0) {
                        charges.add(
                            AdditionalCharge(
                                id = UUID.randomUUID().toString(),
                                tariffId = tariffId,
                                chargeType = AdditionalChargeType.SERVICE_CHARGE,
                                calculationMethod = ChargeCalculationMethod.FIXED_MONTHLY,
                                amount = BigDecimal(state.serviceCharge),
                                isEnabled = true,
                                vatApplicable = true,
                                customName = "সার্ভিস চার্জ"
                            )
                        )
                    }
                }

                // Demand Charge (Optional)
                if (state.isDemandChargeEnabled && state.demandCharge.isNotBlank()) {
                    val amt = state.demandCharge.toDoubleOrNull() ?: 0.0
                    if (amt > 0.0) {
                        charges.add(
                            AdditionalCharge(
                                id = UUID.randomUUID().toString(),
                                tariffId = tariffId,
                                chargeType = AdditionalChargeType.DEMAND_CHARGE,
                                calculationMethod = ChargeCalculationMethod.FIXED_MONTHLY,
                                amount = BigDecimal(state.demandCharge),
                                isEnabled = true,
                                vatApplicable = true,
                                customName = "ডিমান্ড চার্জ"
                            )
                        )
                    }
                }

                // Meter Rent (Optional)
                if (state.isMeterRentEnabled && state.meterRent.isNotBlank()) {
                    val amt = state.meterRent.toDoubleOrNull() ?: 0.0
                    if (amt > 0.0) {
                        charges.add(
                            AdditionalCharge(
                                id = UUID.randomUUID().toString(),
                                tariffId = tariffId,
                                chargeType = AdditionalChargeType.METER_RENT,
                                calculationMethod = ChargeCalculationMethod.FIXED_MONTHLY,
                                amount = BigDecimal(state.meterRent),
                                isEnabled = true,
                                vatApplicable = true,
                                customName = "মিটার ভাড়া"
                            )
                        )
                    }
                }

                // VAT (Optional)
                if (state.isVatEnabled && state.vatPercentage.isNotBlank()) {
                    val amt = state.vatPercentage.toDoubleOrNull() ?: 0.0
                    if (amt > 0.0) {
                        charges.add(
                            AdditionalCharge(
                                id = UUID.randomUUID().toString(),
                                tariffId = tariffId,
                                chargeType = AdditionalChargeType.VAT,
                                calculationMethod = ChargeCalculationMethod.PERCENTAGE_OF_TOTAL,
                                amount = BigDecimal(state.vatPercentage),
                                isEnabled = true,
                                vatApplicable = false,
                                customName = "ভ্যাট"
                            )
                        )
                    }
                }

                val tariff = Tariff(
                    id = tariffId,
                    tariffName = "আবাসিক ট্যারিফ (${state.meterName})",
                    billingMode = state.billingMode,
                    calculationMethod = TariffCalculationMethod.PROGRESSIVE_BLOCK,
                    effectiveFrom = now,
                    effectiveUntil = null,
                    currency = "BDT",
                    isActive = true,
                    flatRate = if (isFlat) BigDecimal(state.flatRate.ifBlank { "8.00" }) else null,
                    slabs = slabs,
                    additionalCharges = charges
                )
                tariffRepository.saveTariff(tariff)

                // 3. Save Baseline Reading with user's current reading
                val baselineReading = MeterReading(
                    id = UUID.randomUUID().toString(),
                    meterId = meterId,
                    blackDigits = state.baselineBlackDigits,
                    redDigit = state.baselineRedDigit,
                    normalizedReading = baselineReadingValue,
                    readingDateTime = state.baselineDateTime,
                    createdAt = now,
                    updatedAt = now,
                    isBaseline = true,
                    note = "প্রারম্ভিক বেসলাইন রিডিং (${state.meterName})",
                    readingSource = ReadingSource.INITIAL_BASELINE
                )
                readingRepository.saveReading(baselineReading)

                // 4. Log initialization event
                meterRepository.logMeterEvent(
                    MeterEvent(
                        id = UUID.randomUUID().toString(),
                        meterId = meterId,
                        eventType = MeterEventType.INITIALIZATION,
                        eventDate = state.baselineDateTime,
                        description = "মিটার ইনিশিয়ালাইজেশন ও বেসলাইন রিডিং সম্পন্ন ($baselineReadingValue kWh)",
                        createdAt = now
                    )
                )

                // 5. Initialize period and calculate
                recalculationUseCase.recalculateAll(meterId)

                // 6. Set onboarding completed
                preferencesManager.setOnboardingCompleted(true)

                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isCompleting = false, errorMessage = "ত্রুটি: ${e.message}") }
            }
        }
    }
}
