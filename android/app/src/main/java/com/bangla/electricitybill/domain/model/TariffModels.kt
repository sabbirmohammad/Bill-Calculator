package com.bangla.electricitybill.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

enum class TariffBillingMode {
    FLAT_RATE,
    SLAB_BASED
}

enum class TariffCalculationMethod {
    PROGRESSIVE_BLOCK,
    SINGLE_APPLICABLE_SLAB
}

enum class AdditionalChargeType {
    DEMAND_CHARGE,
    METER_RENT,
    SERVICE_CHARGE,
    FIXED_CHARGE,
    VAT,
    DISCOUNT,
    OTHER
}

enum class ChargeCalculationMethod {
    FIXED_MONTHLY,
    PER_UNIT,
    PERCENTAGE_OF_ENERGY,
    PERCENTAGE_OF_TOTAL
}

data class Tariff(
    val id: String,
    val tariffName: String,
    val billingMode: TariffBillingMode = TariffBillingMode.SLAB_BASED,
    val calculationMethod: TariffCalculationMethod = TariffCalculationMethod.PROGRESSIVE_BLOCK,
    val effectiveFrom: LocalDateTime,
    val effectiveUntil: LocalDateTime? = null,
    val currency: String = "BDT",
    val isActive: Boolean = true,
    val flatRate: BigDecimal? = null,
    val slabs: List<TariffSlab> = emptyList(),
    val additionalCharges: List<AdditionalCharge> = emptyList()
)

data class TariffSlab(
    val id: String,
    val tariffId: String,
    val minimumUnits: BigDecimal,
    val maximumUnits: BigDecimal?, // Null indicates infinity (e.g. 500+)
    val ratePerUnit: BigDecimal,
    val displayOrder: Int
)

data class AdditionalCharge(
    val id: String,
    val tariffId: String,
    val chargeType: AdditionalChargeType,
    val calculationMethod: ChargeCalculationMethod,
    val amount: BigDecimal,
    val isEnabled: Boolean = true,
    val vatApplicable: Boolean = false,
    val customName: String? = null
)
