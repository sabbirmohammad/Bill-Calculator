package com.powerunit.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.powerunit.core.common.Formatters
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.VoltTrackTheme
import kotlin.math.abs

/**
 * TrendChip (§5.4):
 * - Height 24dp (scalable), radius 12dp
 * - Arrow icon + percent
 * - Green down arrow = less usage (good); red up arrow = more usage (bad).
 * - Always icon + text.
 * - Supports compact mode for space-constrained column layouts.
 */
@Composable
fun TrendChip(
    percent: Double,
    modifier: Modifier = Modifier,
    useBengaliDigits: Boolean = false,
    compact: Boolean = false
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

    // Fix double sign bug: Formatters.formatPercentage returns e.g. "+10.0%" or "-10.0%"
    // Strip leading signs before prepending prefix to avoid "-+100.0%" or "++12.5%"
    val displayText = formatTrendText(percent, useBengaliDigits, compact)

    val horizontalPadding = if (compact) 5.dp else Dimens.xs
    val verticalPadding = 2.dp
    val iconSize = if (compact) 11.dp else Dimens.iconSizeMini
    val textStyle = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium
    val minWidth = if (compact) 36.dp else 44.dp

    Box(
        modifier = modifier
            .heightIn(min = if (compact) 20.dp else 22.dp)
            .defaultMinSize(minWidth = minWidth)
            .clip(RoundedCornerShape(11.dp))
            .background(chipContainerColor)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
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
                modifier = Modifier.size(iconSize)
            )

            Spacer(modifier = Modifier.width(if (compact) 2.dp else Dimens.xxs))

            Text(
                text = displayText,
                style = textStyle,
                color = contentColor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

internal fun formatTrendText(
    percent: Double,
    useBengaliDigits: Boolean = false,
    compact: Boolean = false
): String {
    val isDecrease = percent < 0
    val isZero = percent == 0.0

    val rawFormatted = Formatters.formatPercentage(abs(percent), useBengaliDigits)
        .removePrefix("+")
        .removePrefix("-")
        .trim()
    val prefix = when {
        isZero -> ""
        isDecrease -> "-"
        else -> "+"
    }
    val cleanFormatted = if (compact && (abs(percent) >= 100.0 || abs(percent) % 1.0 == 0.0)) {
        rawFormatted.replace(".0%", "%").replace(".০%", "%")
    } else {
        rawFormatted
    }
    return "$prefix$cleanFormatted"
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

@Preview(name = "TrendChip - Compact", showBackground = true)
@Composable
private fun PreviewTrendChipCompact() {
    VoltTrackTheme {
        Row {
            TrendChip(percent = -100.0, compact = true)
            Spacer(modifier = Modifier.width(8.dp))
            TrendChip(percent = 12.5, compact = true)
        }
    }
}

@Preview(name = "TrendChip - Bangla", showBackground = true)
@Composable
private fun PreviewTrendChipBangla() {
    VoltTrackTheme {
        TrendChip(percent = -8.4, useBengaliDigits = true)
    }
}
