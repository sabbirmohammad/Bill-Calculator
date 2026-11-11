package com.powerunit.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.Motion
import com.powerunit.core.designsystem.theme.NumberMedium
import com.powerunit.core.designsystem.theme.UsageHigh
import com.powerunit.core.designsystem.theme.UsageLow
import com.powerunit.core.designsystem.theme.UsageMedium
import com.powerunit.core.designsystem.theme.VoltTrackTheme

/**
 * UsageRing (§5.3):
 * - 120dp default, 12dp stroke, rounded caps
 * - Track: surfaceVariant
 * - Color from usage meter rules: 0–70% teal, 70–90% amber, 90%+ red
 * - Center: percent (numberMedium) + "of budget" (labelMedium)
 * - Animated sweep 800ms
 */
@Composable
fun UsageRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = Dimens.usageRingDefaultSize,
    strokeWidth: Dp = Dimens.usageRingStrokeWidth,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    centerText: String? = null,
    subText: String? = "of budget"
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = Motion.usageRingDurationMs),
        label = "UsageRingProgress"
    )

    val progressColor = when {
        progress >= 0.9f -> UsageHigh
        progress >= 0.7f -> UsageMedium
        else -> UsageLow
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val radius = (this.size.minDimension - stroke) / 2
            val center = this.center

            // Background Track
            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = stroke)
            )

            // Progress Arc
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val displayText = centerText ?: "${(progress * 100).toInt()}%"
            Text(
                text = displayText,
                style = if (size >= 100.dp) NumberMedium else MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subText != null && size >= 100.dp) {
                Text(
                    text = subText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(name = "UsageRing - Light", showBackground = true)
@Composable
private fun PreviewUsageRingLight() {
    VoltTrackTheme(darkTheme = false) {
        UsageRing(progress = 0.65f)
    }
}

@Preview(name = "UsageRing - Dark", showBackground = true)
@Composable
private fun PreviewUsageRingDark() {
    VoltTrackTheme(darkTheme = true) {
        UsageRing(progress = 0.82f)
    }
}

@Preview(name = "UsageRing - 200% Font", fontScale = 2.0f, showBackground = true)
@Composable
private fun PreviewUsageRingFont() {
    VoltTrackTheme {
        UsageRing(progress = 0.94f)
    }
}

@Preview(name = "UsageRing - Bangla", showBackground = true)
@Composable
private fun PreviewUsageRingBangla() {
    VoltTrackTheme {
        UsageRing(progress = 0.65f, centerText = "৬৫%", subText = "বাজেটের")
    }
}
