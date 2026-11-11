package com.powerunit.core.designsystem.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.HeroGradientDark
import com.powerunit.core.designsystem.theme.HeroGradientLight
import com.powerunit.core.designsystem.theme.HeroShape
import com.powerunit.core.designsystem.theme.NumberHero
import com.powerunit.core.designsystem.theme.VoltTrackTheme

/**
 * GradientHeroCard (§5.1):
 * - Radius 28dp, padding 20dp, min height 200dp
 * - Top row: label (labelMedium, white 78%) + TrendChip
 * - Center: numberHero units + unit suffix (labelMedium, 4dp gap)
 * - Below: cost line (titleMedium)
 * - Bottom: cycle progress bar (6dp tall, white 24% track, white fill) + cycle status (labelMedium)
 * - Optional sparkline in background at 20% opacity.
 */
@Composable
fun GradientHeroCard(
    monthLabel: String,
    unitsUsed: String,
    unitSymbol: String,
    estimatedCost: String,
    modifier: Modifier = Modifier,
    trendPercent: Double? = null,
    cycleDayText: String = "Day 11 of 30",
    cycleProgress: Float = 0.36f,
    projectedBill: String? = null,
    projectedUnits: String? = null,
    forecastStatus: String? = null,
    isOverBudget: Boolean? = null,
    sparklinePoints: List<Float>? = null,
    darkTheme: Boolean = isSystemInDarkTheme()
) {
    val gradient = if (darkTheme) HeroGradientDark else HeroGradientLight
    val whitePrimary = Color.White
    val whiteSecondary = Color.White.copy(alpha = 0.78f)
    val trackWhite = Color.White.copy(alpha = 0.24f)

    val animatedProgress by animateFloatAsState(
        targetValue = cycleProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "heroCycleProgress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 200.dp)
            .clip(HeroShape),
        shape = HeroShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level3),
        border = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(HeroShape)
                .background(gradient)
        ) {
            // Optional background sparkline at 20% opacity (only drawn when there is meaningful variation across at least 2 points)
            if (!sparklinePoints.isNullOrEmpty() && sparklinePoints.size >= 2) {
                val maxVal = sparklinePoints.maxOrNull() ?: 1f
                val minVal = sparklinePoints.minOrNull() ?: 0f
                val range = maxVal - minVal
                if (range > 0.5f) {
                    Canvas(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(bottom = Dimens.spacingXxl * 2)
                    ) {
                        val stepX = size.width / (sparklinePoints.size - 1)

                        val path = Path()
                        sparklinePoints.forEachIndexed { i, p ->
                            val x = i * stepX
                            val normalized = (p - minVal) / range
                            val y = size.height - (normalized * (size.height * 0.40f))
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }

                        drawPath(
                            path = path,
                            color = Color.White.copy(alpha = 0.20f),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.cardPaddingLarge)
            ) {
                // Top row: label + TrendChip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = monthLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = whiteSecondary
                    )
                    if (trendPercent != null) {
                        TrendChip(percent = trendPercent)
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacingSm))

                // Center: numberHero units + unit suffix
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = unitsUsed,
                        style = NumberHero,
                        color = whitePrimary,
                        modifier = Modifier.alignByBaseline()
                    )
                    Spacer(modifier = Modifier.width(Dimens.xxs))
                    Text(
                        text = unitSymbol,
                        style = MaterialTheme.typography.labelMedium,
                        color = whiteSecondary,
                        modifier = Modifier.alignByBaseline()
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.xs))

                // Below: cost line
                Text(
                    text = estimatedCost,
                    style = MaterialTheme.typography.titleMedium,
                    color = whitePrimary
                )

                Spacer(modifier = Modifier.height(Dimens.spacingMd))

                // Bottom: Cycle progress bar (6dp tall PillShape) + cycle text and percentage
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cycleDayText,
                            style = MaterialTheme.typography.labelMedium,
                            color = whiteSecondary
                        )
                        val percentInt = (animatedProgress * 100).toInt()
                        Text(
                            text = "$percentInt%",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = whitePrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(Dimens.spacingXs))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    ) {
                        val cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                        // Background track
                        drawRoundRect(
                            color = trackWhite,
                            size = size,
                            cornerRadius = cornerRadius
                        )
                        // Fill track
                        if (animatedProgress > 0f) {
                            val fillWidth = size.width * animatedProgress
                            drawRoundRect(
                                color = whitePrimary,
                                size = Size(width = fillWidth, height = size.height),
                                cornerRadius = cornerRadius
                            )
                        }
                    }
                }
            }
        }
    }
}

// Backwards-compatible overload
@Composable
fun GradientHeroCard(
    monthLabel: String,
    unitsUsed: String,
    unitSymbol: String,
    estimatedBill: String,
    budgetPercent: Float,
    projectionText: String,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isSystemInDarkTheme()
) {
    GradientHeroCard(
        monthLabel = monthLabel,
        unitsUsed = unitsUsed,
        unitSymbol = unitSymbol,
        estimatedCost = estimatedBill,
        modifier = modifier,
        trendPercent = -4.2,
        cycleDayText = projectionText,
        cycleProgress = budgetPercent,
        sparklinePoints = listOf(12f, 15f, 13f, 18f, 14f, 16f, 11f),
        darkTheme = darkTheme
    )
}

@Preview(name = "HeroCard - Light", showBackground = true)
@Composable
private fun PreviewGradientHeroCardLight() {
    VoltTrackTheme(darkTheme = false) {
        GradientHeroCard(
            monthLabel = "Current Billing Cycle",
            unitsUsed = "142.8",
            unitSymbol = "kWh",
            estimatedCost = "৳ 1,280.50 (Projected)",
            trendPercent = -5.4,
            cycleDayText = "Day 14 of 30 · 48% through cycle",
            cycleProgress = 0.48f,
            sparklinePoints = listOf(10f, 12f, 15f, 13f, 17f, 14f, 16f)
        )
    }
}

@Preview(name = "HeroCard - Dark", showBackground = true)
@Composable
private fun PreviewGradientHeroCardDark() {
    VoltTrackTheme(darkTheme = true) {
        GradientHeroCard(
            monthLabel = "Current Billing Cycle",
            unitsUsed = "142.8",
            unitSymbol = "kWh",
            estimatedCost = "৳ 1,280.50 (Projected)",
            trendPercent = -5.4,
            cycleDayText = "Day 14 of 30 · 48% through cycle",
            cycleProgress = 0.48f,
            sparklinePoints = listOf(10f, 12f, 15f, 13f, 17f, 14f, 16f)
        )
    }
}

@Preview(name = "HeroCard - 200% Font", fontScale = 2.0f, showBackground = true)
@Composable
private fun PreviewGradientHeroCardFontScale() {
    VoltTrackTheme {
        GradientHeroCard(
            monthLabel = "Billing Cycle",
            unitsUsed = "142.8",
            unitSymbol = "kWh",
            estimatedCost = "৳ 1,280.50",
            trendPercent = -5.4,
            cycleDayText = "Day 14 of 30",
            cycleProgress = 0.48f
        )
    }
}

@Preview(name = "HeroCard - Bangla", showBackground = true)
@Composable
private fun PreviewGradientHeroCardBangla() {
    VoltTrackTheme {
        GradientHeroCard(
            monthLabel = "বর্তমান বিলিং সাইকেল",
            unitsUsed = "১৪২.৮",
            unitSymbol = "ইউনিট",
            estimatedCost = "৳ ১,২৮০.৫০ (সম্ভাব্য)",
            trendPercent = -5.4,
            cycleDayText = "৩০ দিনের মধ্যে ১৪তম দিন",
            cycleProgress = 0.48f
        )
    }
}
