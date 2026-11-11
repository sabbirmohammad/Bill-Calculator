package com.powerunit.feature.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.powerunit.core.common.Formatters
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.ChipShape
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.Elevation
import com.powerunit.feature.analytics.WeekdayIntensity
import kotlinx.datetime.DayOfWeek

@Composable
fun WeekdayHeatmap(
    pattern: List<WeekdayIntensity>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("analytics_heatmap_card"),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.level1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard)
        ) {
            Text(
                text = "Day-of-Week Pattern",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(Dimens.xxs))
            Text(
                text = "Average daily consumption across days of the week",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Dimens.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                pattern.forEach { item ->
                    WeekdayCell(item = item, modifier = Modifier.weight(1f).padding(horizontal = 2.dp))
                }
            }
        }
    }
}

@Composable
private fun WeekdayCell(
    item: WeekdayIntensity,
    modifier: Modifier = Modifier
) {
    val dayShort = when (item.dayOfWeek) {
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        DayOfWeek.SUNDAY -> "Sun"
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    // Interpolate color between surfaceVariant and primary
    val cellColor = rememberInterpolatedColor(surfaceVariant, primaryColor, item.intensity)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.keypadKeyHeight)
                .clip(ChipShape)
                .background(cellColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = Formatters.formatUnits(item.averageUnits),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (item.intensity > 0.6f) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(Dimens.xs - 2.dp))

        Text(
            text = dayShort,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun rememberInterpolatedColor(lowColor: Color, highColor: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = lowColor.red + (highColor.red - lowColor.red) * f,
        green = lowColor.green + (highColor.green - lowColor.green) * f,
        blue = lowColor.blue + (highColor.blue - lowColor.blue) * f,
        alpha = 1f
    )
}
