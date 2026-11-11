package com.powerunit.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.powerunit.core.common.Formatters
import com.powerunit.core.designsystem.theme.ButtonShape
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.PillShape
import com.powerunit.core.designsystem.theme.UsageLow
import com.powerunit.core.designsystem.theme.UsageMedium
import com.powerunit.domain.model.MonthEndForecast
import com.powerunit.feature.dashboard.BudgetUiModel
import java.math.BigDecimal

private data class BudgetBadgeInfo(
    val text: String,
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color
)

@Composable
private fun getBudgetBadgeInfo(percent: Float, isOverBudget: Boolean): BudgetBadgeInfo {
    return when {
        isOverBudget || percent >= 1.0f -> BudgetBadgeInfo(
            text = "Over budget",
            icon = Icons.Default.ErrorOutline,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
        percent in 0.70f..0.999f -> BudgetBadgeInfo(
            text = "Watch out",
            icon = Icons.Default.Warning,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
        else -> BudgetBadgeInfo(
            text = "On track",
            icon = Icons.Default.CheckCircle,
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Modern visual Monthly Budget & Outlook Card (§5.5 & Fix Issue 4):
 * - Left side: 110dp UsageRing with percentage and dynamic status chip (On track / Watch out / Over budget)
 * - Right side: 3 compact stat rows with tinted icon wells (Spent so far, Projected month-end, Budget limit + Edit)
 * - Bottom row: Segmented horizontal bar [ Spent ] [ Projected ] [ Buffer ] with single-line insight.
 */
@Composable
fun MonthlyBudgetAndOutlookCard(
    budget: BudgetUiModel,
    forecast: MonthEndForecast?,
    daysElapsed: Int,
    totalDays: Int,
    daysLeft: Int,
    onEditBudgetClick: () -> Unit,
    modifier: Modifier = Modifier,
    hideAmounts: Boolean = false,
    useBengaliDigits: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard)
        ) {
            val badge = getBudgetBadgeInfo(budget.percentageUsed, budget.isOverBudget)

            // Header: Title + Dynamic Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(Dimens.spacingSm))
                    Text(
                        text = "Monthly Budget & Outlook",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = PillShape,
                    color = badge.containerColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = badge.icon,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = badge.contentColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = badge.text,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = badge.contentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacingMd))

            // Body: Responsive Left UsageRing + Right 3 Stat Rows
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val configuration = LocalConfiguration.current
                val isStacked = maxWidth < 320.dp || configuration.fontScale > 1.3f

                if (isStacked) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
                    ) {
                        UsageRing(
                            progress = budget.percentageUsed,
                            size = 110.dp,
                            strokeWidth = 10.dp,
                            subText = "of budget"
                        )
                        BudgetStatRowsColumn(
                            budget = budget,
                            forecast = forecast,
                            daysElapsed = daysElapsed,
                            totalDays = totalDays,
                            hideAmounts = hideAmounts,
                            onEditBudgetClick = onEditBudgetClick
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
                    ) {
                        Box(
                            modifier = Modifier.width(115.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            UsageRing(
                                progress = budget.percentageUsed,
                                size = 110.dp,
                                strokeWidth = 10.dp,
                                subText = "of budget"
                            )
                        }

                        BudgetStatRowsColumn(
                            modifier = Modifier.weight(1f),
                            budget = budget,
                            forecast = forecast,
                            daysElapsed = daysElapsed,
                            totalDays = totalDays,
                            hideAmounts = hideAmounts,
                            onEditBudgetClick = onEditBudgetClick
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacingMd))

            // Bottom Section: Segmented Bar [ Spent ] [ Projected ] [ Buffer ]
            BudgetSegmentedBar(
                spentBDT = budget.currentSpentBDT,
                projectedBDT = forecast?.expectedBill ?: budget.currentSpentBDT,
                limitBDT = budget.monthlyLimitBDT
            )

            Spacer(modifier = Modifier.height(Dimens.spacingXs))

            val insightText = formatInsightText(budget, forecast, daysLeft, hideAmounts)
            val isAlert = budget.isOverBudget || (forecast != null && forecast.expectedBill > budget.monthlyLimitBDT)

            Text(
                text = insightText,
                style = MaterialTheme.typography.bodySmall,
                color = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BudgetStatRowsColumn(
    budget: BudgetUiModel,
    forecast: MonthEndForecast?,
    daysElapsed: Int,
    totalDays: Int,
    hideAmounts: Boolean,
    onEditBudgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
    ) {
        BudgetStatRow(
            icon = Icons.Default.AccountBalanceWallet,
            label = "Spent so far",
            value = if (hideAmounts) "৳ ••••" else Formatters.formatBDT(budget.currentSpentBDT),
            subtitle = "Day $daysElapsed of $totalDays"
        )
        BudgetStatRow(
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            label = "Projected month-end",
            value = if (hideAmounts) "৳ ••••" else Formatters.formatBDT(forecast?.expectedBill ?: budget.currentSpentBDT),
            subtitle = forecast?.let {
                if (hideAmounts) "Range: ৳••••" else "Range: ${Formatters.formatBDT(it.lowBill)} – ${Formatters.formatBDT(it.highBill)}"
            } ?: "Based on current rate"
        )
        BudgetStatRow(
            icon = Icons.Default.Flag,
            label = "Budget limit",
            value = if (hideAmounts) "৳ ••••" else Formatters.formatBDT(budget.monthlyLimitBDT),
            subtitle = if (budget.isOverBudget) {
                "Over by ${if (hideAmounts) "৳ ••••" else Formatters.formatBDT(budget.currentSpentBDT.subtract(budget.monthlyLimitBDT))}"
            } else {
                "${if (hideAmounts) "৳ ••••" else Formatters.formatBDT(budget.monthlyLimitBDT.subtract(budget.currentSpentBDT))} buffer left"
            },
            onEditClick = onEditBudgetClick
        )
    }
}

private fun formatInsightText(
    budget: BudgetUiModel,
    forecast: MonthEndForecast?,
    daysLeft: Int,
    hideAmounts: Boolean
): String {
    return if (forecast != null) {
        val diff = budget.monthlyLimitBDT.subtract(forecast.expectedBill)
        if (diff >= BigDecimal.ZERO) {
            "Projected ${if (hideAmounts) "৳ ••••" else Formatters.formatBDT(diff)} under budget • $daysLeft days remaining"
        } else {
            "Projected to exceed budget by ${if (hideAmounts) "৳ ••••" else Formatters.formatBDT(diff.abs())}"
        }
    } else {
        val rem = budget.monthlyLimitBDT.subtract(budget.currentSpentBDT)
        if (rem >= BigDecimal.ZERO) {
            "${if (hideAmounts) "৳ ••••" else Formatters.formatBDT(rem)} remaining in monthly budget"
        } else {
            "Currently over budget by ${if (hideAmounts) "৳ ••••" else Formatters.formatBDT(rem.abs())}"
        }
    }
}

@Composable
private fun BudgetStatRow(
    icon: ImageVector,
    label: String,
    value: String,
    subtitle: String,
    onEditClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(Dimens.spacingXs))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (onEditClick != null) {
                    Text(
                        text = "Edit",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable(onClick = onEditClick)
                            .padding(start = 4.dp, top = 2.dp, bottom = 2.dp)
                    )
                }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BudgetSegmentedBar(
    spentBDT: BigDecimal,
    projectedBDT: BigDecimal,
    limitBDT: BigDecimal
) {
    val limit = limitBDT.toFloat().coerceAtLeast(1f)
    val spent = spentBDT.toFloat().coerceAtLeast(0f)
    val proj = projectedBDT.toFloat().coerceAtLeast(spent)

    val spentRatio = (spent / limit).coerceIn(0f, 1f)
    val projRemainingRatio = ((proj - spent) / limit).coerceIn(0f, (1f - spentRatio).coerceAtLeast(0f))

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                if (spentRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(spentRatio)
                            .height(8.dp)
                            .background(UsageLow)
                    )
                }
                if (projRemainingRatio > 0f) {
                    val remainingTotal = 1f - spentRatio
                    val relativeProj = if (remainingTotal > 0f) projRemainingRatio / remainingTotal else 0f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(relativeProj)
                            .height(8.dp)
                            .background(UsageMedium)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(UsageLow))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Spent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(UsageMedium))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Projected", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Buffer", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * Beautiful CTA Card displayed when user has not yet configured a monthly budget.
 */
@Composable
fun SetBudgetCtaCard(
    onSetBudgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            Text(
                text = "Set a monthly budget",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Get alerts before your bill crosses a threshold and keep your electricity spending on track.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Dimens.spacingSm)
            )

            Spacer(modifier = Modifier.height(Dimens.spacingMd))

            Button(
                onClick = onSetBudgetClick,
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(Dimens.spacingXs))
                Text(
                    text = "+ Set monthly budget",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Dialog to set or edit monthly budget amount in BDT.
 */
@Composable
fun SetBudgetDialog(
    currentLimit: BigDecimal?,
    onSaveBudget: (BigDecimal) -> Unit,
    onDismiss: () -> Unit
) {
    var textValue by remember { mutableStateOf(currentLimit?.toPlainString() ?: "2500") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Monthly Budget Limit",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)) {
                Text(
                    text = "Enter your monthly electricity spending limit in BDT (৳):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        isError = it.toBigDecimalOrNull()?.let { b -> b <= BigDecimal.ZERO } ?: true
                    },
                    label = { Text("Budget Limit (৳)") },
                    prefix = { Text("৳ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("1500", "2500", "3500", "5000").forEach { preset ->
                        FilterChip(
                            selected = textValue == preset,
                            onClick = {
                                textValue = preset
                                isError = false
                            },
                            label = { Text("৳$preset", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = textValue.toBigDecimalOrNull()
                    if (amount != null && amount > BigDecimal.ZERO) {
                        onSaveBudget(amount)
                        onDismiss()
                    } else {
                        isError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Save Budget")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
