package com.bangla.electricitybill.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bangla.electricitybill.core.util.BanglaDigitNormalizer
import com.bangla.electricitybill.core.util.BigDecimalMath
import com.bangla.electricitybill.presentation.theme.AppDivider
import com.bangla.electricitybill.presentation.theme.CardOutline
import com.bangla.electricitybill.presentation.theme.DeepPurple
import com.bangla.electricitybill.presentation.theme.LightPurple
import com.bangla.electricitybill.presentation.theme.PrimaryPurple
import com.bangla.electricitybill.presentation.theme.PrimaryText
import com.bangla.electricitybill.presentation.theme.SecondaryText
import com.bangla.electricitybill.presentation.theme.SuccessGreen
import com.bangla.electricitybill.presentation.theme.SuccessGreenLight
import java.math.BigDecimal

/**
 * Segmented Date Filter Tabs ("This Month", "Last Month", "Custom")
 */
@Composable
fun SegmentedDateFilters(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf("This Month", "Last Month", "Custom")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LightPurple.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = filter == selectedFilter
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) PrimaryPurple else Color.Transparent)
                    .clickable { onFilterSelected(filter) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else SecondaryText,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/**
 * Screen 3 — Monthly Usage Hero Card with Percentage Difference Badge
 */
@Composable
fun AnalyticsUsageHeroCard(
    monthTitle: String,
    totalUnits: BigDecimal,
    percentageDiff: Int? = null,
    isDiffDecrease: Boolean = true,
    previousUnits: BigDecimal? = null,
    modifier: Modifier = Modifier,
    isBangla: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, CardOutline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = monthTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                val formattedUnits = BigDecimalMath.formatKwh(totalUnits, isBangla)
                Text(
                    text = "$formattedUnits Units",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryText,
                    fontSize = 28.sp
                )

                if (percentageDiff != null && previousUnits != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SuccessGreenLight)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            val arrow = if (isDiffDecrease) "↓" else "↑"
                            Text(
                                text = "$arrow $percentageDiff%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen,
                                fontSize = 11.sp
                            )
                        }

                        val prevStr = BigDecimalMath.formatKwh(previousUnits, isBangla)
                        Text(
                            text = "vs. last month ($prevStr units)",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LightPurple),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

data class DailyChartPoint(
    val dayNumber: Int,
    val units: Float,
    val dateLabel: String
)

/**
 * Screen 3 — Professional Consumption Bar Chart with Interactive Tooltip
 */
@Composable
fun InteractiveConsumptionBarChart(
    dataPoints: List<DailyChartPoint>,
    modifier: Modifier = Modifier,
    initialSelectedIndex: Int = -1
) {
    if (dataPoints.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, CardOutline)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No consumption intervals recorded for this period.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }
        }
        return
    }

    var selectedIndex by remember(dataPoints) {
        mutableIntStateOf(if (initialSelectedIndex in dataPoints.indices) initialSelectedIndex else dataPoints.lastIndex)
    }

    var animateTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateTrigger = true
    }

    val animationProgress by animateFloatAsState(
        targetValue = if (animateTrigger) 1f else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "chartAnim"
    )

    val textMeasurer = rememberTextMeasurer()
    val ySteps = listOf(10f, 8f, 6f, 4f, 2f, 0f)
    val maxUnits = (dataPoints.maxOfOrNull { it.units } ?: 10f).coerceAtLeast(10f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, CardOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .pointerInput(dataPoints) {
                        detectTapGestures { offset ->
                            val leftPadding = 24.dp.toPx()
                            val chartWidth = size.width - leftPadding
                            val count = dataPoints.size
                            if (count > 0 && offset.x >= leftPadding) {
                                val xInChart = offset.x - leftPadding
                                val itemWidth = chartWidth / count
                                val clickedIdx = (xInChart / itemWidth).toInt().coerceIn(0, count - 1)
                                selectedIndex = clickedIdx
                            }
                        }
                    }
            ) {
                val leftPadding = 24.dp.toPx()
                val bottomPadding = 24.dp.toPx()
                val tooltipHeight = 28.dp.toPx()
                val chartTop = tooltipHeight
                val chartHeight = size.height - bottomPadding - chartTop
                val chartWidth = size.width - leftPadding

                val labelStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 10.sp,
                    color = SecondaryText
                )

                // 1. Draw horizontal grid lines & Y-axis labels
                ySteps.forEach { yVal ->
                    val yPos = chartTop + chartHeight * (1f - (yVal / 10f))
                    // Grid line
                    drawLine(
                        color = AppDivider,
                        start = Offset(leftPadding, yPos),
                        end = Offset(size.width, yPos),
                        strokeWidth = 1.dp.toPx()
                    )
                    // Y Label
                    val textResult = textMeasurer.measure(
                        text = yVal.toInt().toString(),
                        style = labelStyle
                    )
                    drawText(
                        textLayoutResult = textResult,
                        topLeft = Offset(leftPadding - textResult.size.width - 4.dp.toPx(), yPos - textResult.size.height / 2f)
                    )
                }

                // 2. Draw bars
                val count = dataPoints.size
                val barSpacing = 4.dp.toPx()
                val totalSpacing = barSpacing * (count + 1)
                val rawBarWidth = ((chartWidth - totalSpacing) / count).coerceAtLeast(6.dp.toPx()).coerceAtMost(22.dp.toPx())
                val effectiveSpacing = (chartWidth - (rawBarWidth * count)) / (count + 1)

                var selectedBarTopCenter: Offset? = null
                var selectedBarUnits = 0f

                dataPoints.forEachIndexed { index, point ->
                    val isSelected = index == selectedIndex
                    val targetHeight = (point.units / maxUnits) * chartHeight
                    val currentHeight = (targetHeight * animationProgress).coerceAtLeast(4.dp.toPx())

                    val x = leftPadding + effectiveSpacing + index * (rawBarWidth + effectiveSpacing)
                    val y = chartTop + chartHeight - currentHeight

                    val barColor = if (isSelected) PrimaryPurple else LightPurple

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, y),
                        size = Size(rawBarWidth, currentHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    if (isSelected) {
                        selectedBarTopCenter = Offset(x + rawBarWidth / 2f, y)
                        selectedBarUnits = point.units
                    }

                    // Draw X date label for select days (e.g. 1st, 5th, 10th, 15th, 18th or spaced)
                    val showLabel = count <= 7 || index == 0 || index == count - 1 || index % (count / 4).coerceAtLeast(1) == 0
                    if (showLabel) {
                        val dateText = point.dateLabel
                        val measured = textMeasurer.measure(
                            text = dateText,
                            style = labelStyle
                        )
                        drawText(
                            textLayoutResult = measured,
                            topLeft = Offset(x + (rawBarWidth - measured.size.width) / 2f, size.height - bottomPadding + 6.dp.toPx())
                        )
                    }
                }

                // 3. Draw Selected Bar Tooltip Pill
                selectedBarTopCenter?.let { topCenter ->
                    val tooltipText = "%.1f units".format(selectedBarUnits)
                    val tooltipStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    val measuredTooltip = textMeasurer.measure(
                        text = tooltipText,
                        style = tooltipStyle
                    )

                    val pillWidth = measuredTooltip.size.width + 16.dp.toPx()
                    val pillHeight = 22.dp.toPx()
                    val pillX = (topCenter.x - pillWidth / 2f).coerceIn(leftPadding, size.width - pillWidth)
                    val pillY = (topCenter.y - pillHeight - 6.dp.toPx()).coerceAtLeast(0f)

                    // Draw Tooltip pill background
                    drawRoundRect(
                        color = DeepPurple,
                        topLeft = Offset(pillX, pillY),
                        size = Size(pillWidth, pillHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // Draw Tooltip down pointer triangle
                    val arrowPath = Path().apply {
                        moveTo(topCenter.x - 4.dp.toPx(), pillY + pillHeight)
                        lineTo(topCenter.x + 4.dp.toPx(), pillY + pillHeight)
                        lineTo(topCenter.x, pillY + pillHeight + 4.dp.toPx())
                        close()
                    }
                    drawPath(arrowPath, color = DeepPurple)

                    // Draw text inside pill
                    drawText(
                        textLayoutResult = measuredTooltip,
                        topLeft = Offset(pillX + 8.dp.toPx(), pillY + (pillHeight - measuredTooltip.size.height) / 2f)
                    )
                }
            }
        }
    }
}

/**
 * Screen 3 — Two Summary Cards (Daily Average & Estimated Bill)
 */
@Composable
fun AnalyticsSummaryCardsRow(
    dailyAverageUnits: String,
    estimatedBillText: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Daily Average Card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
            border = BorderStroke(1.dp, CardOutline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(LightPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = "Daily Average",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryText,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dailyAverageUnits,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Estimated Bill Card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
            border = BorderStroke(1.dp, CardOutline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SuccessGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = "Estimated Bill",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryText,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = estimatedBillText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

/**
 * Screen 3 — Navigable Section Tile ("Usage Trend", "Daily Readings")
 */
@Composable
fun NavigableSectionTile(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, CardOutline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = SecondaryText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
