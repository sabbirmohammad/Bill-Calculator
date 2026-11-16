package com.powerunit.domain.usecase

import com.powerunit.domain.model.PrepaidAlertLevel
import com.powerunit.domain.model.PrepaidStatus
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CalculatePrepaidStatusUseCase @Inject constructor() {

    operator fun invoke(
        currentBalanceBdt: BigDecimal,
        averageDailyCostBdt: BigDecimal,
        averageCostPerUnitBdt: BigDecimal,
        monthlyFixedChargesBdt: BigDecimal = BigDecimal.ZERO,
        daysLeftInMonth: Int = 30
    ): PrepaidStatus {
        val balance = currentBalanceBdt.coerceAtLeast(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)
        val dailyCost = averageDailyCostBdt.coerceAtLeast(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)
        val costPerUnit = averageCostPerUnitBdt.coerceAtLeast(BigDecimal.ZERO)

        val exactDays = if (dailyCost > BigDecimal.ZERO) {
            balance.divide(dailyCost, 2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
        }

        val estimatedDays = if (dailyCost > BigDecimal.ZERO) {
            balance.divide(dailyCost, 0, RoundingMode.DOWN).toInt()
        } else {
            30
        }

        val hoursRemaining = if (dailyCost > BigDecimal.ZERO) {
            exactDays.multiply(BigDecimal("24")).setScale(0, RoundingMode.HALF_UP).toInt()
        } else {
            720
        }

        val estimatedUnits = if (costPerUnit > BigDecimal.ZERO) {
            balance.divide(costPerUnit, 1, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }

        // Fixed deductions occur on 1st of month in BD prepaid meters (demand charge, meter rent, VAT)
        val willHitMonthTurnover = estimatedDays >= daysLeftInMonth && monthlyFixedChargesBdt > BigDecimal.ZERO
        val netBalanceAtTurnover = if (willHitMonthTurnover) {
            balance - (dailyCost.multiply(BigDecimal(daysLeftInMonth))) - monthlyFixedChargesBdt
        } else {
            balance
        }

        val alertLevel = when {
            balance <= BigDecimal("30.00") || estimatedDays <= 1 || (willHitMonthTurnover && netBalanceAtTurnover <= BigDecimal.ZERO) -> {
                PrepaidAlertLevel.CRITICAL
            }
            balance <= BigDecimal("100.00") || estimatedDays <= 3 -> {
                PrepaidAlertLevel.LOW_BALANCE
            }
            else -> {
                PrepaidAlertLevel.NORMAL
            }
        }

        val isAlert = alertLevel != PrepaidAlertLevel.NORMAL

        return PrepaidStatus(
            currentBalance = balance,
            estimatedUnitsRemaining = estimatedUnits,
            dailyBurnCost = dailyCost,
            estimatedDaysRemaining = estimatedDays,
            isRechargeSoonAlert = isAlert,
            exactDaysRemaining = exactDays,
            hoursRemaining = hoursRemaining,
            alertLevel = alertLevel,
            estimatedMonthEndDeduction = if (willHitMonthTurnover) monthlyFixedChargesBdt else BigDecimal.ZERO
        )
    }
}
