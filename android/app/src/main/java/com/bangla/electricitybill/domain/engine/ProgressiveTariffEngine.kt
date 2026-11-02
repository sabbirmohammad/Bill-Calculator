package com.bangla.electricitybill.domain.engine

import com.bangla.electricitybill.core.util.BigDecimalMath
import com.bangla.electricitybill.domain.model.AdditionalCharge
import com.bangla.electricitybill.domain.model.AdditionalChargeType
import com.bangla.electricitybill.domain.model.ChargeCalculationMethod
import com.bangla.electricitybill.domain.model.ItemizedBill
import com.bangla.electricitybill.domain.model.SlabChargeBreakdown
import com.bangla.electricitybill.domain.model.Tariff
import com.bangla.electricitybill.domain.model.TariffBillingMode
import com.bangla.electricitybill.domain.model.TariffCalculationMethod
import com.bangla.electricitybill.domain.model.TariffSlab
import java.math.BigDecimal
import java.math.RoundingMode

object ProgressiveTariffEngine {

    /**
     * Calculates the pure energy charge for a given cumulative monthly consumption.
     */
    fun calculateEnergyCost(
        consumption: BigDecimal,
        tariff: Tariff
    ): Pair<BigDecimal, List<SlabChargeBreakdown>> {
        val units = BigDecimalMath.toCalculationScale(consumption)
        if (units <= BigDecimal.ZERO) {
            return Pair(BigDecimal.ZERO.setScale(BigDecimalMath.CURRENCY_SCALE, RoundingMode.HALF_UP), emptyList())
        }

        return when (tariff.billingMode) {
            TariffBillingMode.FLAT_RATE -> {
                val rate = tariff.flatRate ?: BigDecimal.ZERO
                val totalCost = units.multiply(rate).setScale(BigDecimalMath.CURRENCY_SCALE, RoundingMode.HALF_UP)
                val breakdown = listOf(
                    SlabChargeBreakdown(
                        slab = TariffSlab(
                            id = "flat",
                            tariffId = tariff.id,
                            minimumUnits = BigDecimal.ZERO,
                            maximumUnits = null,
                            ratePerUnit = rate,
                            displayOrder = 1
                        ),
                        unitsInSlab = units,
                        rate = rate,
                        cost = totalCost
                    )
                )
                Pair(totalCost, breakdown)
            }
            TariffBillingMode.SLAB_BASED -> {
                when (tariff.calculationMethod) {
                    TariffCalculationMethod.PROGRESSIVE_BLOCK -> calculateProgressiveSlabs(units, tariff.slabs)
                    TariffCalculationMethod.SINGLE_APPLICABLE_SLAB -> calculateSingleApplicableSlab(units, tariff.slabs)
                }
            }
        }
    }

    private fun calculateProgressiveSlabs(
        units: BigDecimal,
        slabs: List<TariffSlab>
    ): Pair<BigDecimal, List<SlabChargeBreakdown>> {
        val sortedSlabs = slabs.sortedBy { it.displayOrder }
        var totalCost = BigDecimal.ZERO
        val breakdowns = mutableListOf<SlabChargeBreakdown>()

        for (slab in sortedSlabs) {
            val min = slab.minimumUnits
            val max = slab.maximumUnits

            if (units > min) {
                val upperLimit = if (max != null) units.min(max) else units
                val unitsInThisSlab = upperLimit.subtract(min)

                if (unitsInThisSlab > BigDecimal.ZERO) {
                    val slabCost = unitsInThisSlab.multiply(slab.ratePerUnit)
                    totalCost = totalCost.add(slabCost)
                    breakdowns.add(
                        SlabChargeBreakdown(
                            slab = slab,
                            unitsInSlab = unitsInThisSlab,
                            rate = slab.ratePerUnit,
                            cost = slabCost.setScale(BigDecimalMath.CURRENCY_SCALE, RoundingMode.HALF_UP)
                        )
                    )
                }
            }
        }

        return Pair(totalCost.setScale(BigDecimalMath.CURRENCY_SCALE, RoundingMode.HALF_UP), breakdowns)
    }

    private fun calculateSingleApplicableSlab(
        units: BigDecimal,
        slabs: List<TariffSlab>
    ): Pair<BigDecimal, List<SlabChargeBreakdown>> {
        val matchingSlab = slabs.find { slab ->
            units >= slab.minimumUnits && (slab.maximumUnits == null || units <= slab.maximumUnits)
        } ?: slabs.maxByOrNull { it.displayOrder }

        if (matchingSlab == null) {
            return Pair(BigDecimal.ZERO, emptyList())
        }

        val totalCost = units.multiply(matchingSlab.ratePerUnit).setScale(BigDecimalMath.CURRENCY_SCALE, RoundingMode.HALF_UP)
        val breakdown = listOf(
            SlabChargeBreakdown(
                slab = matchingSlab,
                unitsInSlab = units,
                rate = matchingSlab.ratePerUnit,
                cost = totalCost
            )
        )
        return Pair(totalCost, breakdown)
    }

    /**
     * Calculates the EXACT daily or interval energy cost under slab tariffs using the
     * difference between cumulative period bills:
     * Delta Energy Cost = Bill(C_new) - Bill(C_prev)
     */
    fun calculateDifferentialEnergyCost(
        previousCumulativeConsumption: BigDecimal,
        newCumulativeConsumption: BigDecimal,
        tariff: Tariff
    ): BigDecimal {
        val (costPrev, _) = calculateEnergyCost(previousCumulativeConsumption, tariff)
        val (costNew, _) = calculateEnergyCost(newCumulativeConsumption, tariff)
        val delta = costNew.subtract(costPrev)
        return if (delta < BigDecimal.ZERO) BigDecimal.ZERO else delta.setScale(BigDecimalMath.CURRENCY_SCALE, RoundingMode.HALF_UP)
    }

    /**
     * Calculates the full monthly itemized bill including additional charges and VAT.
     */
    fun calculateMonthlyBill(
        monthlyConsumption: BigDecimal,
        tariff: Tariff
    ): ItemizedBill {
        val (energyCost, slabBreakdowns) = calculateEnergyCost(monthlyConsumption, tariff)

        var demandCharge = BigDecimal.ZERO
        var meterRent = BigDecimal.ZERO
        var serviceCharge = BigDecimal.ZERO
        var otherCharges = BigDecimal.ZERO
        var discountAmount = BigDecimal.ZERO

        val activeCharges = tariff.additionalCharges.filter { it.isEnabled }
        val vatCharges = mutableListOf<AdditionalCharge>()

        for (charge in activeCharges) {
            val amount = charge.amount
            val calculatedCharge = when (charge.calculationMethod) {
                ChargeCalculationMethod.FIXED_MONTHLY -> amount
                ChargeCalculationMethod.PER_UNIT -> monthlyConsumption.multiply(amount)
                ChargeCalculationMethod.PERCENTAGE_OF_ENERGY -> energyCost.multiply(amount).divide(BigDecimalMath.HUNDRED, BigDecimalMath.CALCULATION_SCALE, RoundingMode.HALF_UP)
                ChargeCalculationMethod.PERCENTAGE_OF_TOTAL -> BigDecimal.ZERO // computed after base charges if any
            }

            when (charge.chargeType) {
                AdditionalChargeType.DEMAND_CHARGE -> demandCharge = demandCharge.add(calculatedCharge)
                AdditionalChargeType.METER_RENT -> meterRent = meterRent.add(calculatedCharge)
                AdditionalChargeType.SERVICE_CHARGE -> serviceCharge = serviceCharge.add(calculatedCharge)
                AdditionalChargeType.OTHER,
                AdditionalChargeType.FIXED_CHARGE -> otherCharges = otherCharges.add(calculatedCharge)
                AdditionalChargeType.DISCOUNT -> discountAmount = discountAmount.add(calculatedCharge)
                AdditionalChargeType.VAT -> vatCharges.add(charge)
            }
        }

        // Calculate VAT based on configured rules: Energy Cost + any VAT-applicable additional charges
        var vatBase = energyCost
        for (charge in activeCharges) {
            if (charge.vatApplicable && charge.chargeType != AdditionalChargeType.VAT) {
                val chargeCost = when (charge.calculationMethod) {
                    ChargeCalculationMethod.FIXED_MONTHLY -> charge.amount
                    ChargeCalculationMethod.PER_UNIT -> monthlyConsumption.multiply(charge.amount)
                    ChargeCalculationMethod.PERCENTAGE_OF_ENERGY -> energyCost.multiply(charge.amount).divide(BigDecimalMath.HUNDRED, BigDecimalMath.CALCULATION_SCALE, RoundingMode.HALF_UP)
                    ChargeCalculationMethod.PERCENTAGE_OF_TOTAL -> BigDecimal.ZERO
                }
                vatBase = vatBase.add(chargeCost)
            }
        }

        var vatAmount = BigDecimal.ZERO
        for (vatCharge in vatCharges) {
            val vatRate = vatCharge.amount
            val vatPortion = vatBase.multiply(vatRate).divide(BigDecimalMath.HUNDRED, BigDecimalMath.CURRENCY_SCALE, RoundingMode.HALF_UP)
            vatAmount = vatAmount.add(vatPortion)
        }

        val totalBeforeDiscount = energyCost
            .add(demandCharge)
            .add(meterRent)
            .add(serviceCharge)
            .add(otherCharges)
            .add(vatAmount)

        val totalBill = totalBeforeDiscount.subtract(discountAmount)
            .max(BigDecimal.ZERO)
            .setScale(BigDecimalMath.CURRENCY_SCALE, RoundingMode.HALF_UP)

        return ItemizedBill(
            totalUnits = monthlyConsumption,
            slabBreakdowns = slabBreakdowns,
            energyCost = energyCost.setScale(BigDecimalMath.CURRENCY_SCALE, RoundingMode.HALF_UP),
            demandCharge = demandCharge.setScale(BigDecimalMath.CURRENCY_SCALE, RoundingMode.HALF_UP),
            meterRent = meterRent.setScale(BigDecimalMath.CURRENCY_SCALE, RoundingMode.HALF_UP),
            serviceCharge = serviceCharge.setScale(BigDecimalMath.CURRENCY_SCALE, RoundingMode.HALF_UP),
            otherCharges = otherCharges.setScale(BigDecimalMath.CURRENCY_SCALE, RoundingMode.HALF_UP),
            vatAmount = vatAmount.setScale(BigDecimalMath.CURRENCY_SCALE, RoundingMode.HALF_UP),
            discountAmount = discountAmount.setScale(BigDecimalMath.CURRENCY_SCALE, RoundingMode.HALF_UP),
            totalBill = totalBill
        )
    }
}
