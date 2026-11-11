package com.powerunit.core.designsystem.component

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.IconWellShape
import com.powerunit.core.designsystem.theme.NumberMedium
import com.powerunit.core.designsystem.theme.VoltTrackTheme

/**
 * StatCard (§5.2): Bento tile
 * - Radius 24dp, inner padding 16dp
 * - Icon well (40dp square, radius 12dp) top-left
 * - Label (labelMedium)
 * - Value (numberMedium, tabular)
 * - Delta (labelMedium with arrow icon)
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    unit: String? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trendPercent: Double? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(Dimens.iconContainerSize)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = IconWellShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Dimens.iconSizeSmall)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(Dimens.iconContainerSize))
                }

                if (trendPercent != null) {
                    TrendChip(percent = trendPercent)
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Dimens.cardValueToHintGap))

            Row(verticalAlignment = Alignment.Baseline) {
                Text(
                    text = value,
                    style = NumberMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (unit != null) {
                    Spacer(modifier = Modifier.width(Dimens.xxs))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (subtitle != null && trendPercent == null) {
                Spacer(modifier = Modifier.height(Dimens.cardValueToHintGap))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(name = "StatCard - Light", showBackground = true)
@Composable
private fun PreviewStatCardLight() {
    VoltTrackTheme(darkTheme = false) {
        StatCard(
            title = "Avg Daily Usage",
            value = "8.4",
            unit = "kWh",
            icon = Icons.Filled.ElectricBolt,
            trendPercent = -4.2
        )
    }
}

@Preview(name = "StatCard - Dark", showBackground = true)
@Composable
private fun PreviewStatCardDark() {
    VoltTrackTheme(darkTheme = true) {
        StatCard(
            title = "Avg Daily Usage",
            value = "8.4",
            unit = "kWh",
            icon = Icons.Filled.ElectricBolt,
            trendPercent = -4.2
        )
    }
}

@Preview(name = "StatCard - 200% Font", fontScale = 2.0f, showBackground = true)
@Composable
private fun PreviewStatCardFont() {
    VoltTrackTheme {
        StatCard(
            title = "Avg Daily Usage",
            value = "8.4",
            unit = "kWh",
            icon = Icons.Filled.ElectricBolt,
            trendPercent = -4.2
        )
    }
}

@Preview(name = "StatCard - Bangla", showBackground = true)
@Composable
private fun PreviewStatCardBangla() {
    VoltTrackTheme {
        StatCard(
            title = "দৈনিক গড় ব্যবহার",
            value = "৮.৪",
            unit = "ইউনিট",
            icon = Icons.Filled.ElectricBolt,
            trendPercent = -৪.২
        )
    }
}
