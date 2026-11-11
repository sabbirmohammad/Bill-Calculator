package com.powerunit.feature.analytics.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.powerunit.core.common.Formatters
import com.powerunit.feature.analytics.ChartBarData
import com.powerunit.core.designsystem.theme.CardShape
import java.math.BigDecimal

@Composable
fun UsageBarAndLineChart(
    bars: List<ChartBarData>,
    selectedBarIndex: Int?,
    onBarSelected: (Int?) -> Unit,
    isTableView: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("analytics_hero_chart_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Usage & Cost Trend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                // Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .height(12.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Units (kWh)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .height(3.dp)
                            .background(MaterialTheme.colorScheme.tertiary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Cost (৳)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (bars.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No readings available for this period",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (isTableView) {
                ChartTableView(bars = bars)
            } else {
                ChartCanvasView(
                    bars = bars,
                    selectedBarIndex = selectedBarIndex,
                    onBarSelected = onBarSelected
                )
            }
        }
    }
}

@Composable
private fun ChartCanvasView(
    bars: List<ChartBarData>,
    selectedBarIndex: Int?,
    onBarSelected: (Int?) -> Unit
) {
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(bars) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 600))
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val errorColor = MaterialTheme.colorScheme.error
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant

    val maxUnits = remember(bars) {
        val max = bars.maxOfOrNull { it.units } ?: BigDecimal.ONE
        if (max > BigDecimal.ZERO) max.toFloat() else 1f
    }
    val maxCost = remember(bars) {
        val max = bars.maxOfOrNull { it.cost } ?: BigDecimal.ONE
        if (max > BigDecimal.ZERO) max.toFloat() else 1f
    }

    Column {
        selectedBarIndex?.let { idx ->
            if (idx in bars.indices) {
                SelectedBarTooltip(bar = bars[idx])
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .pointerInput(bars) {
                        detectTapGestures { offset ->
                            val slotWidth = size.width / bars.size
                            val clickedIndex = (offset.x / slotWidth).toInt()
                            if (clickedIndex in bars.indices) {
                                onBarSelected(if (selectedBarIndex == clickedIndex) null else clickedIndex)
                            }
                        }
                    }
            ) {
                drawGridAndBars(
                    bars = bars,
                    selectedBarIndex = selectedBarIndex,
                    maxUnits = maxUnits,
                    maxCost = maxCost,
                    progress = animationProgress.value,
                    primaryColor = primaryColor,
                    primaryContainer = primaryContainer,
                    tertiaryColor = tertiaryColor,
                    errorColor = errorColor,
                    outlineVariant = outlineVariant
                )
            }

            // Labels row under Canvas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(28.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bars.forEach { bar ->
                    Text(
                        text = bar.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedBarTooltip(bar: ChartBarData) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.inverseSurface,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = bar.fullDate.ifEmpty { bar.label },
                color = MaterialTheme.colorScheme.inverseOnSurface,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${Formatters.formatUnits(bar.units)} kWh • ${Formatters.formatBDT(bar.cost)}",
                color = MaterialTheme.colorScheme.inverseOnSurface,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Suppress("LongParameterList")
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGridAndBars(
    bars: List<ChartBarData>,
    selectedBarIndex: Int?,
    maxUnits: Float,
    maxCost: Float,
    progress: Float,
    primaryColor: Color,
    primaryContainer: Color,
    tertiaryColor: Color,
    errorColor: Color,
    outlineVariant: Color
) {
    val width = size.width
    val height = size.height - 30.dp.toPx()
    val slotWidth = width / bars.size
    val barWidth = (slotWidth * 0.55f).coerceAtMost(28.dp.toPx())

    // Dashed grid line
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    drawLine(
        color = outlineVariant,
        start = Offset(0f, height * 0.5f),
        end = Offset(width, height * 0.5f),
        strokeWidth = 1.dp.toPx(),
        pathEffect = pathEffect
    )
    drawLine(
        color = outlineVariant,
        start = Offset(0f, height),
        end = Offset(width, height),
        strokeWidth = 1.dp.toPx()
    )

    val costPoints = mutableListOf<Offset>()

    bars.forEachIndexed { index, bar ->
        val xCenter = (index * slotWidth) + (slotWidth / 2f)
        val barHeight = ((bar.units.toFloat() / maxUnits) * height) * progress
        val topY = height - barHeight

        val isSelected = selectedBarIndex == index
        val barColor = when {
            bar.isAnomaly -> errorColor
            isSelected || bar.isPeak -> primaryColor
            else -> primaryContainer
        }

        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(barColor, barColor.copy(alpha = 0.65f))
            ),
            topLeft = Offset(xCenter - (barWidth / 2f), topY),
            size = Size(barWidth, barHeight.coerceAtLeast(4f)),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )

        val costY = height - (((bar.cost.toFloat() / maxCost) * height) * progress)
        costPoints.add(Offset(xCenter, costY))
    }

    if (costPoints.size > 1) {
        val linePath = Path().apply {
            moveTo(costPoints.first().x, costPoints.first().y)
            for (i in 1 until costPoints.size) {
                lineTo(costPoints[i].x, costPoints[i].y)
            }
        }
        drawPath(
            path = linePath,
            color = tertiaryColor,
            style = Stroke(width = 2.5.dp.toPx())
        )
        costPoints.forEach { pt ->
            drawCircle(
                color = tertiaryColor,
                radius = 3.5.dp.toPx(),
                center = pt
            )
        }
    }
}

@Composable
private fun ChartTableView(bars: List<ChartBarData>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Period / Date",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Usage (kWh)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Cost (৳)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        bars.forEach { bar ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bar.fullDate.ifEmpty { bar.label },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (bar.isPeak) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = "${Formatters.formatUnits(bar.units)} kWh",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = Formatters.formatBDT(bar.cost),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
