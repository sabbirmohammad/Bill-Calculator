package com.bangla.electricitybill.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bangla.electricitybill.core.util.BanglaDigitNormalizer
import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.domain.engine.ConsumptionCalculator
import com.bangla.electricitybill.domain.model.MeterReading
import com.bangla.electricitybill.domain.model.ReadingSource
import com.bangla.electricitybill.presentation.theme.AccentCyan
import com.bangla.electricitybill.presentation.theme.WarningAmber
import java.math.BigDecimal

data class ChartIntervalBar(
    val dateLabel: String,
    val units: Float,
    val isMultiDay: Boolean,
    val tooltip: String
)

@Composable
fun ConsumptionTrendChart(
    readings: List<MeterReading>,
    modifier: Modifier = Modifier,
    isBangla: Boolean = false
) {
    val bars = remember(readings, isBangla) {
        val sorted = readings.sortedBy { it.readingDateTime }
        val result = mutableListOf<ChartIntervalBar>()

        for (i in 1 until sorted.size) {
            val prev = sorted[i - 1]
            val curr = sorted[i]
            val isRollover = curr.readingSource == ReadingSource.ROLLOVER || curr.normalizedReading < prev.normalizedReading
            val deltaRes = ConsumptionCalculator.calculateConsumption(
                previousReading = prev.normalizedReading,
                currentReading = curr.normalizedReading,
                isRollover = isRollover,
                blackDigitCapacity = 5
            )
            val delta = deltaRes.getOrDefault(BigDecimal.ZERO)
            val days = DateUtils.daysBetween(prev.readingDateTime, curr.readingDateTime)
            val rawDateLabel = curr.readingDateTime.dayOfMonth.toString()
            val dateLabel = if (isBangla) BanglaDigitNormalizer.toBangla(rawDateLabel) else rawDateLabel
            val formattedDate = DateUtils.formatDate(curr.readingDateTime)
            val deltaStr = if (isBangla) BanglaDigitNormalizer.toBangla(delta.toPlainString()) else delta.toPlainString()
            val daysStr = if (isBangla) BanglaDigitNormalizer.toBangla(days.toString()) else days.toString()
            val dayWord = if (isBangla) "দিন" else if (days == 1L) "day" else "days"

            result.add(
                ChartIntervalBar(
                    dateLabel = dateLabel,
                    units = delta.toFloat(),
                    isMultiDay = days > 1,
                    tooltip = "$formattedDate: $deltaStr kWh ($daysStr $dayWord)"
                )
            )
        }
        result
    }

    var animateTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateTrigger = true
    }

    val animationProgress by animateFloatAsState(
        targetValue = if (animateTrigger) 1f else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "trendChartBars"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
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
                    text = if (isBangla) "ব্যবহারের গতিধারা (Trends)" else "Consumption Trends",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Optically balanced Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(7.dp).background(AccentCyan, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBangla) "১ দিন" else "1 Day",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.size(7.dp).background(WarningAmber, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBangla) "একাধিক দিন" else "Multi-Day",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (bars.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBangla) "গ্রাফ প্রদর্শন করতে ন্যূনতম ২টি রিডিং প্রয়োজন।" else "At least 2 readings required to display trend chart.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val maxUnits = (bars.maxOfOrNull { it.units } ?: 10f).coerceAtLeast(5f)
                val displayBars = bars.takeLast(12) // Show recent 12 intervals
                val textMeasurer = rememberTextMeasurer()
                val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                val labelStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = labelColor)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    val labelHeight = 20.dp.toPx()
                    val chartHeight = size.height - labelHeight
                    val width = size.width
                    val barWidth = (width / (displayBars.size * 1.8f)).coerceAtMost(32.dp.toPx())
                    val spacing = (width - (displayBars.size * barWidth)) / (displayBars.size + 1)

                    displayBars.forEachIndexed { index, bar ->
                        val targetHeight = ((bar.units / maxUnits) * chartHeight).coerceAtLeast(4.dp.toPx())
                        val animatedBarHeight = targetHeight * animationProgress
                        val x = spacing + index * (barWidth + spacing)
                        val y = chartHeight - animatedBarHeight

                        val barColor = if (bar.isMultiDay) WarningAmber else AccentCyan

                        // Draw bar with smooth rounded top corners
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, animatedBarHeight),
                            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                        )

                        // Optically aligned date label drawn precisely centered under the bar
                        val measuredText = textMeasurer.measure(
                            text = bar.dateLabel,
                            style = labelStyle
                        )
                        val textX = x + (barWidth - measuredText.size.width) / 2f
                        val textY = chartHeight + 4.dp.toPx()

                        drawText(
                            textLayoutResult = measuredText,
                            topLeft = Offset(textX, textY)
                        )
                    }
                }
            }
        }
    }
}
