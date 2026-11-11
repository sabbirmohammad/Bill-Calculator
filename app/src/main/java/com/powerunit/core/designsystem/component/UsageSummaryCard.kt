package com.powerunit.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.NumberMedium
import com.powerunit.core.designsystem.theme.VoltTrackTheme

/**
 * Cohesive Usage Summary card displaying Today, Yesterday, and Daily Average
 * in a unified 3-column layout (or stacked 3-row layout when narrow or at large font scales).
 */
@Composable
fun UsageSummaryCard(
    todayUnits: String,
    yesterdayUnits: String,
    dailyAverageUnits: String,
    modifier: Modifier = Modifier,
    unitSymbol: String = "kWh",
    todayTrendPercent: Double? = null,
    useBengaliDigits: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard)
        ) {
            val configuration = LocalConfiguration.current
            val isStacked = maxWidth < 280.dp || configuration.fontScale > 1.3f

            if (isStacked) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
                ) {
                    UsageSummaryStackedRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Today",
                        value = todayUnits,
                        unit = unitSymbol,
                        trendPercent = todayTrendPercent,
                        useBengaliDigits = useBengaliDigits
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 1.dp
                    )
                    UsageSummaryStackedRow(
                        icon = Icons.Default.History,
                        label = "Yesterday",
                        value = yesterdayUnits,
                        unit = unitSymbol,
                        trendPercent = null,
                        useBengaliDigits = useBengaliDigits
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 1.dp
                    )
                    UsageSummaryStackedRow(
                        icon = Icons.Default.Speed,
                        label = "Daily Avg",
                        value = dailyAverageUnits,
                        unit = unitSymbol,
                        trendPercent = null,
                        useBengaliDigits = useBengaliDigits
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UsageSummaryColumn(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CalendarToday,
                        label = "Today",
                        value = todayUnits,
                        unit = unitSymbol,
                        trendPercent = todayTrendPercent,
                        useBengaliDigits = useBengaliDigits
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(52.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    )

                    UsageSummaryColumn(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.History,
                        label = "Yesterday",
                        value = yesterdayUnits,
                        unit = unitSymbol,
                        trendPercent = null,
                        useBengaliDigits = useBengaliDigits
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(52.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    )

                    UsageSummaryColumn(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Speed,
                        label = "Daily Avg",
                        value = dailyAverageUnits,
                        unit = unitSymbol,
                        trendPercent = null,
                        useBengaliDigits = useBengaliDigits
                    )
                }
            }
        }
    }
}

@Composable
private fun UsageSummaryColumn(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    unit: String,
    trendPercent: Double? = null,
    useBengaliDigits: Boolean = false
) {
    Column(
        modifier = modifier.padding(horizontal = Dimens.spacingXs),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(Dimens.xxs))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(Dimens.spacingXs))

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = NumberMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier.alignByBaseline()
            )
            Spacer(modifier = Modifier.width(Dimens.xxs))
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier.alignByBaseline()
            )
        }

        if (trendPercent != null) {
            Spacer(modifier = Modifier.height(Dimens.spacingXs))
            TrendChip(
                percent = trendPercent,
                useBengaliDigits = useBengaliDigits
            )
        }
    }
}

@Composable
private fun UsageSummaryStackedRow(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String,
    trendPercent: Double? = null,
    useBengaliDigits: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.spacingXs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(Dimens.spacingSm))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = NumberMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(Dimens.xxs))
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (trendPercent != null) {
                Spacer(modifier = Modifier.width(Dimens.spacingSm))
                TrendChip(
                    percent = trendPercent,
                    useBengaliDigits = useBengaliDigits
                )
            }
        }
    }
}

@Preview(name = "UsageSummary - Light", showBackground = true)
@Composable
private fun PreviewUsageSummaryLight() {
    VoltTrackTheme(darkTheme = false) {
        UsageSummaryCard(
            todayUnits = "14.2",
            yesterdayUnits = "12.8",
            dailyAverageUnits = "11.5",
            todayTrendPercent = 10.9
        )
    }
}

@Preview(name = "UsageSummary - Dark", showBackground = true)
@Composable
private fun PreviewUsageSummaryDark() {
    VoltTrackTheme(darkTheme = true) {
        UsageSummaryCard(
            todayUnits = "14.2",
            yesterdayUnits = "12.8",
            dailyAverageUnits = "11.5",
            todayTrendPercent = -4.2
        )
    }
}
