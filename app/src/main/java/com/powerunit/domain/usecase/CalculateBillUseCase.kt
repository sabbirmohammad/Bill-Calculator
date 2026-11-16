package com.powerunit.domain.usecase

import com.powerunit.domain.model.BillBreakdown
import com.powerunit.domain.model.SlabCharge
import com.powerunit.domain.model.SlabInfo
import com.powerunit.domain.model.SplitBillBreakdown
import com.powerunit.domain.model.Tariff
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CalculateBillUseCase @Inject constructor() {

    fun calculateEnergyCharge(units: BigDecimal, tariff: Tariff): BigDecimal {
        val nonNegativeUnits = units.coerceAtLeast(BigDecimal.ZERO).setScale(1, RoundingMode.HALF_UP)
        val sortedSlabs = tariff.slabs.sortedBy { it.fromUnit }

        var remainingUnits = nonNegativeUnits
        var energyChargeAcc = BigDecimal.ZERO
        var previousUpperBound = BigDecimal.ZERO

        for ((index, slab) in sortedSlabs.withIndex()) {
            if (remainingUnits <= BigDecimal.ZERO) break

            val effectiveLowerBound = if (index == 0) {
                BigDecimal.ZERO
            } else {
                sortedSlabs[index - 1].toUnit ?: previousUpperBound
            }

            val slabCapacity = if (slab.toUnit != null) {
                (slab.toUnit - effectiveLowerBound).coerceAtLeast(BigDecimal.ZERO)
            } else {
                null // Unlimited
            }

            val unitsInSlab = if (slabCapacity != null) {
                remainingUnits.min(slabCapacity)
            } else {
                remainingUnits
            }

            val slabAmount = unitsInSlab.multiply(slab.ratePerUnit).setScale(2, RoundingMode.HALF_UP)
            energyChargeAcc = energyChargeAcc.add(slabAmount)
            remainingUnits = (remainingUnits - unitsInSlab).coerceAtLeast(BigDecimal.ZERO)

            if (slab.toUnit != null) {
                previousUpperBound = slab.toUnit
            }
        }
        return energyChargeAcc.setScale(2, RoundingMode.HALF_UP)
    }

    fun getSlabForUnits(units: BigDecimal, tariff: Tariff): SlabInfo {
        val sortedSlabs = tariff.slabs.sortedBy { it.fromUnit }
        if (sortedSlabs.isEmpty()) {
            return SlabInfo(
                slabNumber = 1,
                ratePerUnit = BigDecimal("6.00"),
                fromUnit = BigDecimal.ZERO,
                toUnit = null
            )
        }
        val nonNegativeUnits = units.coerceAtLeast(BigDecimal.ZERO)
        for ((index, slab) in sortedSlabs.withIndex()) {
            val toUnit = slab.toUnit
            if (toUnit == null || nonNegativeUnits <= toUnit || index == sortedSlabs.lastIndex) {
                return SlabInfo(
                    slabNumber = index + 1,
                    ratePerUnit = slab.ratePerUnit,
                    fromUnit = slab.fromUnit,
                    toUnit = slab.toUnit
                )
            }
        }
        val last = sortedSlabs.last()
        return SlabInfo(
            slabNumber = sortedSlabs.size,
            ratePerUnit = last.ratePerUnit,
            fromUnit = last.fromUnit,
            toUnit = last.toUnit
        )
    }

    /**
     * Calculates how many additional units can be consumed before entering the next tariff slab.
     * Returns null if already in the highest (unbounded) slab or if slabs are empty.
     */
    fun getUnitsToNextSlab(units: BigDecimal, tariff: Tariff): BigDecimal? {
        val currentSlab = getSlabForUnits(units, tariff)
        val upperLimit = currentSlab.toUnit ?: return null
        val diff = upperLimit - units.coerceAtLeast(BigDecimal.ZERO)
        return diff.coerceAtLeast(BigDecimal.ZERO).setScale(1, RoundingMode.HALF_UP)
    }

    operator fun invoke(units: BigDecimal, tariff: Tariff): BillBreakdown {
        val nonNegativeUnits = units.coerceAtLeast(BigDecimal.ZERO).setScale(1, RoundingMode.HALF_UP)
        val sortedSlabs = tariff.slabs.sortedBy { it.fromUnit }

        var remainingUnits = nonNegativeUnits
        var energyChargeAcc = BigDecimal.ZERO
        val slabCharges = mutableListOf<SlabCharge>()

        var previousUpperBound = BigDecimal.ZERO

        for ((index, slab) in sortedSlabs.withIndex()) {
            val effectiveLowerBound = if (index == 0) {
                BigDecimal.ZERO
            } else {
                sortedSlabs[index - 1].toUnit ?: previousUpperBound
            }

            val slabCapacity = if (slab.toUnit != null) {
                (slab.toUnit - effectiveLowerBound).coerceAtLeast(BigDecimal.ZERO)
            } else {
                null // Unlimited
            }

            if (remainingUnits <= BigDecimal.ZERO) {
                // Record remaining slabs with 0 units for complete transparency
                slabCharges.add(
                    SlabCharge(
                        slabIndex = index + 1,
                        fromUnit = slab.fromUnit,
                        toUnit = slab.toUnit,
                        unitsBilled = BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP),
                        ratePerUnit = slab.ratePerUnit,
                        amount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                    )
                )
                if (slab.toUnit != null) {
                    previousUpperBound = slab.toUnit
                }
                continue
            }

            val unitsInSlab = if (slabCapacity != null) {
                remainingUnits.min(slabCapacity)
            } else {
                remainingUnits
            }

            val slabAmount = unitsInSlab.multiply(slab.ratePerUnit).setScale(2, RoundingMode.HALF_UP)
            energyChargeAcc = energyChargeAcc.add(slabAmount)
            remainingUnits = (remainingUnits - unitsInSlab).coerceAtLeast(BigDecimal.ZERO)

            if (slab.toUnit != null) {
                previousUpperBound = slab.toUnit
            }

            slabCharges.add(
                SlabCharge(
                    slabIndex = index + 1,
                    fromUnit = slab.fromUnit,
                    toUnit = slab.toUnit,
                    unitsBilled = unitsInSlab.setScale(1, RoundingMode.HALF_UP),
                    ratePerUnit = slab.ratePerUnit,
                    amount = slabAmount
                )
            )
        }

        val rebateAmount = if (tariff.rebatePercent > BigDecimal.ZERO) {
            energyChargeAcc.multiply(tariff.rebatePercent).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
        }

        val netEnergyCharge = (energyChargeAcc - rebateAmount).coerceAtLeast(BigDecimal.ZERO)

        val subtotal = netEnergyCharge
            .add(tariff.demandChargeFlat)
            .add(tariff.serviceChargeFlat)
            .add(tariff.otherCharges)
            .setScale(2, RoundingMode.HALF_UP)

        // Calculate VAT with full precision before 2-decimal rounding to avoid truncation errors
        val vatAmount = subtotal.multiply(tariff.vatPercent)
            .divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
        val totalBill = subtotal.add(vatAmount).setScale(2, RoundingMode.HALF_UP)

        return BillBreakdown(
            slabCharges = slabCharges,
            energyCharge = energyChargeAcc.setScale(2, RoundingMode.HALF_UP),
            demandCharge = tariff.demandChargeFlat.setScale(2, RoundingMode.HALF_UP),
            serviceCharge = tariff.serviceChargeFlat.setScale(2, RoundingMode.HALF_UP),
            subtotal = subtotal,
            vatPercent = tariff.vatPercent.setScale(1, RoundingMode.HALF_UP),
            vatAmount = vatAmount,
            totalBill = totalBill,
            rebateAmount = rebateAmount,
            otherCharges = tariff.otherCharges.setScale(2, RoundingMode.HALF_UP)
        )
    }

    /**
     * Calculates bill for a billing cycle that spans across a tariff change date.
     * Prorates total consumption and fixed charges across the two periods based on days count,
     * strictly conserving total units and charges.
     */
    fun calculateSplitBill(
        totalUnits: BigDecimal,
        firstTariff: Tariff,
        firstPeriodDays: Int,
        secondTariff: Tariff,
        secondPeriodDays: Int
    ): SplitBillBreakdown {
        val totalDays = (firstPeriodDays + secondPeriodDays).coerceAtLeast(1)
        val totalDaysBd = BigDecimal(totalDays)
        val nonNegativeUnits = totalUnits.coerceAtLeast(BigDecimal.ZERO).setScale(1, RoundingMode.HALF_UP)

        // Prorate units with exact conservation (largest remainder on anchor period)
        val period1Units = nonNegativeUnits
            .multiply(BigDecimal(firstPeriodDays))
            .divide(totalDaysBd, 1, RoundingMode.HALF_UP)
        val period2Units = (nonNegativeUnits - period1Units).coerceAtLeast(BigDecimal.ZERO)

        // Prorate fixed charges by days
        val proratedTariff1 = firstTariff.copy(
            demandChargeFlat = firstTariff.demandChargeFlat.multiply(BigDecimal(firstPeriodDays)).divide(totalDaysBd, 2, RoundingMode.HALF_UP),
            serviceChargeFlat = firstTariff.serviceChargeFlat.multiply(BigDecimal(firstPeriodDays)).divide(totalDaysBd, 2, RoundingMode.HALF_UP),
            otherCharges = firstTariff.otherCharges.multiply(BigDecimal(firstPeriodDays)).divide(totalDaysBd, 2, RoundingMode.HALF_UP)
        )

        val proratedTariff2 = secondTariff.copy(
            demandChargeFlat = secondTariff.demandChargeFlat.multiply(BigDecimal(secondPeriodDays)).divide(totalDaysBd, 2, RoundingMode.HALF_UP),
            serviceChargeFlat = secondTariff.serviceChargeFlat.multiply(BigDecimal(secondPeriodDays)).divide(totalDaysBd, 2, RoundingMode.HALF_UP),
            otherCharges = secondTariff.otherCharges.multiply(BigDecimal(secondPeriodDays)).divide(totalDaysBd, 2, RoundingMode.HALF_UP)
        )

        val bill1 = invoke(period1Units, proratedTariff1)
        val bill2 = invoke(period2Units, proratedTariff2)

        val totalEnergy = bill1.energyCharge + bill2.energyCharge
        val totalDemand = bill1.demandCharge + bill2.demandCharge
        val totalService = bill1.serviceCharge + bill2.serviceCharge
        val totalOther = bill1.otherCharges + bill2.otherCharges
        val totalRebate = bill1.rebateAmount + bill2.rebateAmount
        val totalVat = bill1.vatAmount + bill2.vatAmount
        val totalBill = bill1.totalBill + bill2.totalBill

        return SplitBillBreakdown(
            period1Days = firstPeriodDays,
            period2Days = secondPeriodDays,
            period1Units = period1Units,
            period2Units = period2Units,
            period1Bill = bill1,
            period2Bill = bill2,
            totalEnergyCharge = totalEnergy,
            totalDemandCharge = totalDemand,
            totalServiceCharge = totalService,
            totalOtherCharges = totalOther,
            totalRebateAmount = totalRebate,
            totalVatAmount = totalVat,
            totalBill = totalBill
        )
    }
}
