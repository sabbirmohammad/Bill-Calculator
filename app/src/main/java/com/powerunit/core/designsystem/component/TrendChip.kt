package com.powerunit.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.powerunit.core.common.Formatters
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.VoltTrackTheme
import kotlin.math.abs

/**
 * TrendChip (§5.4):
 * - Height 24dp, radius 12dp
 * - Arrow icon 14dp + percent (labelMedium)
 * - Green down arrow = less usage (good); red up arrow = more usage (bad).
 * - Always icon + text.
 */
@Composable
fun TrendChip(
    percent: Double,
    modifier: Modifier = Modifier,
    useBengaliDigits: Boolean = false
) {
    val isDecrease = percent < 0
    val isZero = percent == 0.0

    // Green (success) for decrease (good in energy consumption), Red (error) for increase
    val chipContainerColor: Color = when {
        isZero -> MaterialTheme.colorScheme.surfaceVariant
        isDecrease -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
    }

    val contentColor: Color = when {
        isZero -> MaterialTheme.colorScheme.onSurfaceVariant
        isDecrease -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.error
    }

    Box(
        modifier = modifier
            .height(24.dp)
            .defaultMinSize(minWidth = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(chipContainerColor)
            .padding(horizontal = Dimens.xs),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isDecrease || isZero) {
                    Icons.AutoMirrored.Filled.TrendingDown
                } else {
                    Icons.AutoMirrored.Filled.TrendingUp
                },
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(Dimens.iconSizeMini)
            )

            Spacer(modifier = Modifier.width(Dimens.xxs))

            val formatted = Formatters.formatPercentage(abs(percent), useBengaliDigits)
            val prefix = if (isDecrease) "-" else if (isZero) "" else "+"
            Text(
                text = "$prefix$formatted",
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
        }
    }
}

@Preview(name = "TrendChip - Light", showBackground = true)
@Composable
private fun PreviewTrendChipLight() {
    VoltTrackTheme(darkTheme = false) {
        Row {
            TrendChip(percent = -8.4)
            Spacer(modifier = Modifier.width(8.dp))
            TrendChip(percent = 12.1)
        }
    }
}

@Preview(name = "TrendChip - Dark", showBackground = true)
@Composable
private fun PreviewTrendChipDark() {
    VoltTrackTheme(darkTheme = true) {
        Row {
            TrendChip(percent = -8.4)
            Spacer(modifier = Modifier.width(8.dp))
            TrendChip(percent = 12.1)
        }
    }
}

@Preview(name = "TrendChip - 200% Font", fontScale = 2.0f, showBackground = true)
@Composable
private fun PreviewTrendChipFont() {
    VoltTrackTheme {
        TrendChip(percent = -8.4)
    }
}

@Preview(name = "TrendChip - Bangla", showBackground = true)
@Composable
private fun PreviewTrendChipBangla() {
    VoltTrackTheme {
        TrendChip(percent = -8.4, useBengaliDigits = true)
    }
}
