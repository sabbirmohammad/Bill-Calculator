package com.powerunit.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.powerunit.R
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.NumberMedium
import com.powerunit.core.designsystem.theme.WattWiseTheme

/**
 * Cohesive Usage Summary card displaying Today, Yesterday, and Daily Average
 * in a unified 3-column layout (or stacked 3-row layout when narrow or at large font scales).
 *
 * Responsiveness features:
 * - Fluid sizing adapting to narrow widths (< 350dp) without layout overflow or text clipping.
 * - Perfectly aligned header baselines, value baselines, and bottom slots across all columns.
 * - Auto-compact TrendChip and scalable vertical divider.
 * - Graceful transition to stacked rows when screen is very narrow (< 270dp) or font scale > 1.25x.
 */
@Composable
fun UsageSummaryCard(
    todayUnits: String,
    yesterdayUnits: String,
    dailyAverageUnits: String,
    modifier: Modifier = Modifier,
    unitSymbol: String = "kWh",
    todayTrendPercent: Double? = null,
    useBengaliDigits: Boolean = false,
    todayLabel: String = stringResource(R.string.today_usage),
    yesterdayLabel: String = stringResource(R.string.yesterday_usage),
    dailyAverageLabel: String = stringResource(R.string.daily_avg)
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
                .padding(
                    horizontal = Dimens.cardPaddingStandard,
                    vertical = Dimens.sm
                )
        ) {
            val configuration = LocalConfiguration.current
            // Responsive layout thresholds:
            // - Stacked mode on very narrow widths (<270dp) or large accessibility font scale (>1.25x)
            // - Compact 3-column mode on standard mobile widths (<350dp) or moderate font scale (>1.05x)
            val isStacked = maxWidth < 270.dp || configuration.fontScale > 1.25f
            val isCompact = maxWidth < 350.dp || configuration.fontScale > 1.05f
            val hasAnyTrend = todayTrendPercent != null

            if (isStacked) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
                ) {
                    UsageSummaryStackedRow(
                        icon = Icons.Default.CalendarToday,
                        label = todayLabel,
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
                        label = yesterdayLabel,
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
                        label = dailyAverageLabel,
                        value = dailyAverageUnits,
                        unit = unitSymbol,
                        trendPercent = null,
                        useBengaliDigits = useBengaliDigits
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.Top
                ) {
                    UsageSummaryColumn(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CalendarToday,
                        label = todayLabel,
                        value = todayUnits,
                        unit = unitSymbol,
                        trendPercent = todayTrendPercent,
                        hasAnyTrend = hasAnyTrend,
                        useBengaliDigits = useBengaliDigits,
                        isCompact = isCompact
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .padding(vertical = 4.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    )

                    UsageSummaryColumn(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.History,
                        label = yesterdayLabel,
                        value = yesterdayUnits,
                        unit = unitSymbol,
                        trendPercent = null,
                        hasAnyTrend = hasAnyTrend,
                        useBengaliDigits = useBengaliDigits,
                        isCompact = isCompact
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .padding(vertical = 4.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    )

                    UsageSummaryColumn(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Speed,
                        label = dailyAverageLabel,
                        value = dailyAverageUnits,
                        unit = unitSymbol,
                        trendPercent = null,
                        hasAnyTrend = hasAnyTrend,
                        useBengaliDigits = useBengaliDigits,
                        isCompact = isCompact
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
    hasAnyTrend: Boolean = false,
    useBengaliDigits: Boolean = false,
    isCompact: Boolean = false
) {
    Column(
        modifier = modifier.padding(horizontal = if (isCompact) 2.dp else Dimens.spacingXs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // 1. Icon + Label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(if (isCompact) 18.dp else 20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(if (isCompact) 10.dp else 12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(if (isCompact) 2.dp else Dimens.xxs))
            Text(
                text = label,
                style = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(Dimens.spacingXs))

        // 2. Value + Unit
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            val valueStyle = if (isCompact) {
                MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum"
                )
            } else {
                NumberMedium
            }

            Text(
                text = value,
                style = valueStyle,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.alignByBaseline()
            )
            Spacer(modifier = Modifier.width(if (isCompact) 2.dp else Dimens.xxs))
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier.alignByBaseline()
            )
        }

        // 3. Trend slot - preserves uniform baseline across all 3 columns
        if (hasAnyTrend) {
            Spacer(modifier = Modifier.height(Dimens.spacingXs))
            if (trendPercent != null) {
                TrendChip(
                    percent = trendPercent,
                    useBengaliDigits = useBengaliDigits,
                    compact = isCompact
                )
            } else {
                // Reserve matching height so Yesterday and Daily Avg maintain identical vertical alignment
                Spacer(modifier = Modifier.height(if (isCompact) 20.dp else 22.dp))
            }
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = NumberMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(Dimens.xxs))
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
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
    WattWiseTheme(darkTheme = false) {
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
    WattWiseTheme(darkTheme = true) {
        UsageSummaryCard(
            todayUnits = "14.2",
            yesterdayUnits = "12.8",
            dailyAverageUnits = "11.5",
            todayTrendPercent = -4.2
        )
    }
}

@Preview(name = "UsageSummary - Narrow (320dp)", widthDp = 320, showBackground = true)
@Composable
private fun PreviewUsageSummaryNarrow() {
    WattWiseTheme(darkTheme = false) {
        UsageSummaryCard(
            todayUnits = "0.0",
            yesterdayUnits = "15.0",
            dailyAverageUnits = "10.0",
            todayTrendPercent = -100.0
        )
    }
}

@Preview(name = "UsageSummary - Large Font", fontScale = 1.3f, showBackground = true)
@Composable
private fun PreviewUsageSummaryLargeFont() {
    WattWiseTheme(darkTheme = false) {
        UsageSummaryCard(
            todayUnits = "14.2",
            yesterdayUnits = "12.8",
            dailyAverageUnits = "11.5",
            todayTrendPercent = -12.5
        )
    }
}

@Preview(name = "UsageSummary - Bangla", showBackground = true)
@Composable
private fun PreviewUsageSummaryBangla() {
    WattWiseTheme {
        UsageSummaryCard(
            todayUnits = "১৪.২",
            yesterdayUnits = "১২.৮",
            dailyAverageUnits = "১১.৫",
            todayTrendPercent = -100.0,
            useBengaliDigits = true,
            todayLabel = "আজ",
            yesterdayLabel = "গতকাল",
            dailyAverageLabel = "দৈনিক গড়"
        )
    }
}
