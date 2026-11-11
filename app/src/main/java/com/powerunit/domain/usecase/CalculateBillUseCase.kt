package com.powerunit.domain.usecase

import com.powerunit.domain.model.BillBreakdown
import com.powerunit.domain.model.SlabCharge
import com.powerunit.domain.model.Tariff
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CalculateBillUseCase @Inject constructor() {

    operator fun invoke(units: BigDecimal, tariff: Tariff): BillBreakdown {
        val nonNegativeUnits = units.coerceAtLeast(BigDecimal.ZERO).setScale(1, RoundingMode.HALF_UP)
        val sortedSlabs = tariff.slabs.sortedBy { it.fromUnit }

        var remainingUnits = nonNegativeUnits
        var energyChargeAcc = BigDecimal.ZERO
        val slabCharges = mutableListOf<SlabCharge>()

        var previousUpperBound = BigDecimal.ZERO

        for ((index, slab) in sortedSlabs.withIndex()) {
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
                continue
            }

            val slabCapacity = if (slab.toUnit != null) {
                (slab.toUnit - previousUpperBound).coerceAtLeast(BigDecimal.ZERO)
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

        val subtotal = energyChargeAcc
            .add(tariff.demandChargeFlat)
            .add(tariff.serviceChargeFlat)
            .setScale(2, RoundingMode.HALF_UP)

        val vatRate = tariff.vatPercent.divide(BigDecimal("100"), 4, RoundingMode.HALF_UP)
        val vatAmount = subtotal.multiply(vatRate).setScale(2, RoundingMode.HALF_UP)
        val totalBill = subtotal.add(vatAmount).setScale(2, RoundingMode.HALF_UP)

        return BillBreakdown(
            slabCharges = slabCharges,
            energyCharge = energyChargeAcc.setScale(2, RoundingMode.HALF_UP),
            demandCharge = tariff.demandChargeFlat.setScale(2, RoundingMode.HALF_UP),
            serviceCharge = tariff.serviceChargeFlat.setScale(2, RoundingMode.HALF_UP),
            subtotal = subtotal,
            vatPercent = tariff.vatPercent.setScale(1, RoundingMode.HALF_UP),
            vatAmount = vatAmount,
            totalBill = totalBill
        )
    }
}
