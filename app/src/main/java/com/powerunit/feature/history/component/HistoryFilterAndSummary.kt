package com.powerunit.feature.history.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.powerunit.core.common.Formatters
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.feature.history.MonthChipUiModel
import com.powerunit.feature.history.MonthSummaryUiModel

@Composable
fun HistoryHeaderAndYearSelector(
    selectedYear: Int,
    availableYears: List<Int>,
    useBengaliDigits: Boolean,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onExportPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canGoPrevious = true
    val canGoNext = availableYears.isNotEmpty() && selectedYear < availableYears.maxOrNull()!!
    val yearDisplay = if (useBengaliDigits) {
        Formatters.toBengaliDigits(selectedYear.toString())
    } else {
        selectedYear.toString()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = Dimens.screenHorizontalPadding,
                end = Dimens.screenHorizontalPadding,
                top = Dimens.contentTopPadding,
                bottom = Dimens.xs
            )
    ) {
        // Title & PDF Export Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Past readings & monthly statements",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = onExportPdf,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("history_export_pdf_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "Export PDF Statement",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "PDF",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Year Stepper Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("history_year_selector"),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousYear,
                enabled = canGoPrevious,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous Year",
                    tint = if (canGoPrevious) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = yearDisplay,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("history_selected_year_text")
                )
            }

            IconButton(
                onClick = onNextYear,
                enabled = canGoNext,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next Year",
                    tint = if (canGoNext) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Composable
fun HistoryMonthChipsRow(
    chips: List<MonthChipUiModel>,
    selectedMonth: Int,
    onSelectMonth: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedMonth) {
        val targetIndex = (selectedMonth - 1).coerceIn(0, (chips.size - 1).coerceAtLeast(0))
        listState.animateScrollToItem(targetIndex)
    }

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = Dimens.screenHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("history_month_chips_row")
            .padding(bottom = Dimens.xs)
    ) {
        items(chips, key = { it.monthNumber }) { chip ->
            val isSelected = chip.isSelected
            val hasData = chip.hasData

            val containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primary
                hasData -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
            }

            val contentColor = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                hasData -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            }

            val border = when {
                isSelected -> null
                hasData -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = containerColor,
                contentColor = contentColor,
                border = border,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelectMonth(chip.monthNumber) }
                    .testTag("history_month_chip_${chip.monthNumber}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = chip.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                    if (hasData && !isSelected) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthSummaryCard(
    summary: MonthSummaryUiModel,
    useBengaliDigits: Boolean,
    isBangla: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("history_month_summary"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isNarrow = maxWidth < 300.dp
                if (isNarrow) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SummaryStatItem(
                                icon = Icons.Default.Bolt,
                                iconColor = MaterialTheme.colorScheme.primary,
                                label = if (isBangla) "মোট ব্যবহার" else "Total Usage",
                                value = "${Formatters.formatUnits(summary.totalUnits, useBengaliDigits)} kWh",
                                modifier = Modifier.weight(1f)
                            )
                            SummaryStatItem(
                                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                iconColor = MaterialTheme.colorScheme.secondary,
                                label = if (isBangla) "আনুমানিক বিল" else "Est. Cost",
                                value = Formatters.formatMoney(summary.totalCost, includeSymbol = true, useBengaliDigits = useBengaliDigits),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SummaryStatItem(
                                icon = Icons.Default.CalendarMonth,
                                iconColor = MaterialTheme.colorScheme.tertiary,
                                label = if (isBangla) "রিডিং সংখ্যা" else "Readings",
                                value = if (useBengaliDigits) {
                                    "${Formatters.toBengaliDigits(summary.readingsCount.toString())}টি"
                                } else {
                                    "${summary.readingsCount} entries"
                                },
                                modifier = Modifier.weight(1f)
                            )
                            SummaryStatItem(
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                iconColor = MaterialTheme.colorScheme.primary,
                                label = if (isBangla) "দৈনিক গড়" else "Daily Avg",
                                value = "${Formatters.formatUnits(summary.avgPerDay, useBengaliDigits)} kWh/d",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SummaryStatItem(
                            icon = Icons.Default.Bolt,
                            iconColor = MaterialTheme.colorScheme.primary,
                            label = if (isBangla) "মোট বিদ্যুৎ" else "Total Units",
                            value = "${Formatters.formatUnits(summary.totalUnits, useBengaliDigits)} kWh",
                            modifier = Modifier.weight(1f)
                        )
                        SummaryStatItem(
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            iconColor = MaterialTheme.colorScheme.secondary,
                            label = if (isBangla) "আনুমানিক বিল" else "Est. Bill",
                            value = Formatters.formatMoney(summary.totalCost, includeSymbol = true, useBengaliDigits = useBengaliDigits),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryStatItem(
                            icon = Icons.Default.CalendarMonth,
                            iconColor = MaterialTheme.colorScheme.tertiary,
                            label = if (isBangla) "রিডিং" else "Readings",
                            value = if (useBengaliDigits) {
                                "${Formatters.toBengaliDigits(summary.readingsCount.toString())}টি"
                            } else {
                                "${summary.readingsCount} total"
                            },
                            modifier = Modifier.weight(1f)
                        )
                        SummaryStatItem(
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            iconColor = MaterialTheme.colorScheme.primary,
                            label = if (isBangla) "দৈনিক গড়" else "Daily Avg",
                            value = "${Formatters.formatUnits(summary.avgPerDay, useBengaliDigits)} kWh",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryStatItem(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = iconColor.copy(alpha = 0.12f),
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
