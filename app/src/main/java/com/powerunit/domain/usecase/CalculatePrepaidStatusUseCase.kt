package com.powerunit.domain.usecase

import com.powerunit.domain.model.PrepaidStatus
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CalculatePrepaidStatusUseCase @Inject constructor() {

    operator fun invoke(
        currentBalanceBdt: BigDecimal,
        averageDailyCostBdt: BigDecimal,
        averageCostPerUnitBdt: BigDecimal
    ): PrepaidStatus {
        val balance = currentBalanceBdt.coerceAtLeast(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)
        val dailyCost = averageDailyCostBdt.coerceAtLeast(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)
        val costPerUnit = averageCostPerUnitBdt.coerceAtLeast(BigDecimal.ZERO)

        val estimatedDays = if (dailyCost > BigDecimal.ZERO) {
            balance.divide(dailyCost, 0, RoundingMode.DOWN).toInt()
        } else {
            30
        }

        val estimatedUnits = if (costPerUnit > BigDecimal.ZERO) {
            balance.divide(costPerUnit, 1, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }

        // Trigger alert if <= 3 days left or balance <= 100 BDT
        val isAlert = estimatedDays <= 3 || balance <= BigDecimal("100.00")

        return PrepaidStatus(
            currentBalance = balance,
            estimatedUnitsRemaining = estimatedUnits,
            dailyBurnCost = dailyCost,
            estimatedDaysRemaining = estimatedDays,
            isRechargeSoonAlert = isAlert
        )
    }
}
