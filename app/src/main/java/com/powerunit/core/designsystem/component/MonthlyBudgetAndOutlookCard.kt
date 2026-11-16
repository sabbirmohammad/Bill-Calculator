package com.powerunit.core.designsystem.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.style.TextOverflow
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
 * Modern visual Monthly Budget & Outlook Card (§5.5 & Redesign):
 * - Header: Title + Dynamic Status Chip (Guaranteed no wrap/overflow)
 * - Horizontal Progress Bar: 10dp sleek dual-segment bar with spent % and budget limit edit
 * - Cycle Timeline Pill: Visual track showing days elapsed vs days remaining
 * - Bento Financial Stat Tiles: 2 balanced tiles for Spent so far & Projected month-end bill
 * - Smart Alert Banner: Highlighted insight message (under budget / alert)
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
                .padding(Dimens.cardPaddingStandard),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
        ) {
            val badge = getBudgetBadgeInfo(budget.percentageUsed, budget.isOverBudget)

            // 1. Header: Title + Dynamic Status Chip (Guaranteed no wrap/overflow)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                        text = "Monthly Budget",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(Dimens.spacingSm))

                Surface(
                    shape = PillShape,
                    color = badge.containerColor,
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
                            color = badge.contentColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            // 2. Horizontal Budget Progress Bar Section
            BudgetHorizontalProgressSection(
                budget = budget,
                forecast = forecast,
                hideAmounts = hideAmounts,
                useBengaliDigits = useBengaliDigits,
                onEditBudgetClick = onEditBudgetClick
            )

            // 3. Cycle Progress Timeline Pill
            CycleTimelinePill(
                daysElapsed = daysElapsed,
                totalDays = totalDays,
                daysLeft = daysLeft,
                useBengaliDigits = useBengaliDigits
            )

            // 4. Spent & Projected Bento Stat Tiles
            BudgetFinancialTilesRow(
                budget = budget,
                forecast = forecast,
                hideAmounts = hideAmounts,
                useBengaliDigits = useBengaliDigits
            )

            // 5. Smart Insight Banner
            val insightText = formatInsightText(budget, forecast, daysLeft, hideAmounts)
            val isAlert = budget.isOverBudget || (forecast != null && forecast.expectedBill > budget.monthlyLimitBDT)

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isAlert) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isAlert) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = insightText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isAlert) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetHorizontalProgressSection(
    budget: BudgetUiModel,
    forecast: MonthEndForecast?,
    hideAmounts: Boolean,
    useBengaliDigits: Boolean,
    onEditBudgetClick: () -> Unit
) {
    val spentFormatted = if (hideAmounts) "৳ ••••" else Formatters.formatBDT(budget.currentSpentBDT, useBengaliDigits)
    val limitFormatted = if (hideAmounts) "৳ ••••" else Formatters.formatBDT(budget.monthlyLimitBDT, useBengaliDigits)
    val percentInt = (budget.percentageUsed * 100).toInt()
    val percentText = if (useBengaliDigits) Formatters.toBengaliDigits("$percentInt%") else "$percentInt%"

    val animatedProgress by animateFloatAsState(
        targetValue = budget.percentageUsed.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "budgetProgress"
    )

    val progressColor = when {
        budget.isOverBudget || budget.percentageUsed >= 1.0f -> MaterialTheme.colorScheme.error
        budget.percentageUsed >= 0.70f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    val limitFloat = budget.monthlyLimitBDT.toFloat().coerceAtLeast(1f)
    val projRatio = ((forecast?.expectedBill?.toFloat() ?: budget.currentSpentBDT.toFloat()) / limitFloat).coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        // Values row above progress bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = spentFormatted,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.alignByBaseline()
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "($percentText used)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alignByBaseline()
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(PillShape)
                    .clickable(onClick = onEditBudgetClick)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Limit: $limitFormatted",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Budget",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spacingSm))

        // Full-width horizontal dual-segment progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(PillShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            // Projected fill (translucent extension)
            if (projRatio > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(projRatio)
                        .height(10.dp)
                        .clip(PillShape)
                        .background(progressColor.copy(alpha = 0.35f))
                )
            }
            // Actual spent fill (solid)
            if (animatedProgress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(10.dp)
                        .clip(PillShape)
                        .background(progressColor)
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spacingXs))

        // Micro-legend below progress bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(progressColor))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (forecast != null && forecast.expectedBill > budget.currentSpentBDT) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(progressColor.copy(alpha = 0.35f)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Projected",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            val bufferDiff = budget.monthlyLimitBDT.subtract(budget.currentSpentBDT)
            val bufferText = if (bufferDiff >= BigDecimal.ZERO) {
                val amt = if (hideAmounts) "৳ ••••" else Formatters.formatBDT(bufferDiff, useBengaliDigits)
                "$amt buffer left"
            } else {
                val amt = if (hideAmounts) "৳ ••••" else Formatters.formatBDT(bufferDiff.abs(), useBengaliDigits)
                "Over by $amt"
            }
            Text(
                text = bufferText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (bufferDiff < BigDecimal.ZERO) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CycleTimelinePill(
    daysElapsed: Int,
    totalDays: Int,
    daysLeft: Int,
    useBengaliDigits: Boolean
) {
    val dayProgressFraction = if (totalDays > 0) (daysElapsed.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f) else 0f
    val elapsedText = if (useBengaliDigits) Formatters.toBengaliDigits(daysElapsed.toString()) else daysElapsed.toString()
    val totalText = if (useBengaliDigits) Formatters.toBengaliDigits(totalDays.toString()) else totalDays.toString()
    val daysLeftText = if (useBengaliDigits) Formatters.toBengaliDigits(daysLeft.toString()) else daysLeft.toString()

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Day $elapsedText of $totalText",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "$daysLeftText days remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Thin 3dp cycle progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(PillShape)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                if (dayProgressFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(dayProgressFraction)
                            .height(3.dp)
                            .clip(PillShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.75f))
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetFinancialTilesRow(
    budget: BudgetUiModel,
    forecast: MonthEndForecast?,
    hideAmounts: Boolean,
    useBengaliDigits: Boolean
) {
    val percentInt = (budget.percentageUsed * 100).toInt()
    val percentText = if (useBengaliDigits) Formatters.toBengaliDigits("$percentInt%") else "$percentInt%"

    val projectedValue = forecast?.expectedBill ?: budget.currentSpentBDT
    val projSubtitle = forecast?.let { f ->
        val diff = budget.monthlyLimitBDT.subtract(f.expectedBill)
        if (diff >= BigDecimal.ZERO) {
            val bufStr = if (hideAmounts) "৳ ••••" else Formatters.formatBDT(diff, useBengaliDigits)
            "$bufStr buffer"
        } else {
            val overStr = if (hideAmounts) "৳ ••••" else Formatters.formatBDT(diff.abs(), useBengaliDigits)
            "Over by $overStr"
        }
    } ?: "Based on current rate"

    val isProjOver = forecast != null && forecast.expectedBill > budget.monthlyLimitBDT
    val projAccentColor = if (isProjOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
    ) {
        // Spent So Far Tile
        BudgetMetricTile(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.AccountBalanceWallet,
            label = "Spent so far",
            value = if (hideAmounts) "৳ ••••" else Formatters.formatBDT(budget.currentSpentBDT, useBengaliDigits),
            subtitle = "$percentText of limit",
            accentColor = MaterialTheme.colorScheme.primary
        )

        // Projected Bill Tile
        BudgetMetricTile(
            modifier = Modifier.weight(1f),
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            label = "Projected bill",
            value = if (hideAmounts) "৳ ••••" else Formatters.formatBDT(projectedValue, useBengaliDigits),
            subtitle = projSubtitle,
            accentColor = projAccentColor
        )
    }
}

@Composable
private fun BudgetMetricTile(
    icon: ImageVector,
    label: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(11.dp),
                        tint = accentColor
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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
        val budgetStatus = if (diff >= BigDecimal.ZERO) {
            "Projected ${if (hideAmounts) "৳ ••••" else Formatters.formatBDT(diff)} under budget"
        } else {
            "Projected to exceed budget by ${if (hideAmounts) "৳ ••••" else Formatters.formatBDT(diff.abs())}"
        }
        val slabAlert = if (forecast.unitsToNextSlab != null && forecast.unitsToNextSlab > BigDecimal.ZERO && forecast.unitsToNextSlab <= BigDecimal("50.0")) {
            " • ${forecast.unitsToNextSlab} kWh to next slab"
        } else {
            " • $daysLeft days remaining"
        }
        budgetStatus + slabAlert
    } else {
        val rem = budget.monthlyLimitBDT.subtract(budget.currentSpentBDT)
        if (rem >= BigDecimal.ZERO) {
            "${if (hideAmounts) "৳ ••••" else Formatters.formatBDT(rem)} remaining in monthly budget"
        } else {
            "Currently over budget by ${if (hideAmounts) "৳ ••••" else Formatters.formatBDT(rem.abs())}"
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
