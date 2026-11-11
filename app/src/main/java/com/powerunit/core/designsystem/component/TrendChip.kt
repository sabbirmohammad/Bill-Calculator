package com.powerunit.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import com.powerunit.core.common.Formatters
import com.powerunit.core.designsystem.theme.PillShape
import kotlin.math.abs

@Composable
fun TrendChip(
    percent: Double,
    modifier: Modifier = Modifier,
    useBengaliDigits: Boolean = false
) {
    val isDecrease = percent < 0
    val isZero = percent == 0.0

    val chipColor: Color = when {
        isZero -> MaterialTheme.colorScheme.surfaceVariant
        isDecrease -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
    }

    val contentColor: Color = when {
        isZero -> MaterialTheme.colorScheme.onSurfaceVariant
        isDecrease -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.tertiary
    }

    Box(
        modifier = modifier
            .clip(PillShape)
            .background(chipColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isZero) {
                Icon(
                    imageVector = if (isDecrease) {
                        Icons.AutoMirrored.Filled.TrendingDown
                    } else {
                        Icons.AutoMirrored.Filled.TrendingUp
                    },
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            val formatted = Formatters.formatPercentage(abs(percent), useBengaliDigits)
            val prefix = if (isDecrease) "-" else if (isZero) "" else "+"
            Text(
                text = "$prefix$formatted",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}
