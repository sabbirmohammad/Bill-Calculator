package com.powerunit.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.NumberMedium
import com.powerunit.core.designsystem.theme.WattWiseTheme

/**
 * ReadingListItem (§5.6):
 * - Leading date block (48dp rounded square: day numberMedium, month labelSmall)
 * - Title: meter value (titleMedium)
 * - Subtitle: "+8.4 units" (bodyMedium, muted)
 * - Trailing: cost (numberMedium) + badges (Estimated / Note / Photo as 20dp icons)
 * - Min height 64dp, vertical padding 12dp
 */
@Composable
fun ReadingListItem(
    dayString: String,
    monthString: String,
    meterValue: String,
    deltaUnitsText: String,
    costText: String,
    modifier: Modifier = Modifier,
    hasPhoto: Boolean = false,
    hasNote: Boolean = false,
    isEstimated: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level1)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = Dimens.listItemMinHeight)
                .padding(horizontal = Dimens.cardPaddingStandard, vertical = Dimens.listItemVerticalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading date block (48dp rounded square)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(Dimens.xs))
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = dayString,
                        style = NumberMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = monthString.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(Dimens.iconToTextGap))

            // Center: title and delta units subtitle
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = meterValue,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = deltaUnitsText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(Dimens.xs))

            // Trailing: cost and icon badges
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = costText,
                    style = NumberMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.xxs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEstimated) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Estimated",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(Dimens.iconSizeSmall)
                        )
                    }
                    if (hasNote) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Notes,
                            contentDescription = "Note",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(Dimens.iconSizeSmall)
                        )
                    }
                    if (hasPhoto) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = "Photo",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(Dimens.iconSizeSmall)
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "ReadingListItem - Light", showBackground = true)
@Composable
private fun PreviewReadingListItemLight() {
    WattWiseTheme(darkTheme = false) {
        ReadingListItem(
            dayString = "14",
            monthString = "Nov",
            meterValue = "14,820 kWh",
            deltaUnitsText = "+8.4 units",
            costText = "৳ 78.50",
            hasPhoto = true,
            hasNote = true,
            isEstimated = false
        )
    }
}

@Preview(name = "ReadingListItem - Dark", showBackground = true)
@Composable
private fun PreviewReadingListItemDark() {
    WattWiseTheme(darkTheme = true) {
        ReadingListItem(
            dayString = "14",
            monthString = "Nov",
            meterValue = "14,820 kWh",
            deltaUnitsText = "+8.4 units",
            costText = "৳ 78.50",
            hasPhoto = true,
            hasNote = true,
            isEstimated = false
        )
    }
}

@Preview(name = "ReadingListItem - 200% Font", fontScale = 2.0f, showBackground = true)
@Composable
private fun PreviewReadingListItemFont() {
    WattWiseTheme {
        ReadingListItem(
            dayString = "14",
            monthString = "Nov",
            meterValue = "14,820",
            deltaUnitsText = "+8.4 units",
            costText = "৳ 78",
            hasNote = true
        )
    }
}

@Preview(name = "ReadingListItem - Bangla", showBackground = true)
@Composable
private fun PreviewReadingListItemBangla() {
    WattWiseTheme {
        ReadingListItem(
            dayString = "১৪",
            monthString = "নভে",
            meterValue = "১৪,৮২০ ইউনিট",
            deltaUnitsText = "+৮.৪ ইউনিট",
            costText = "৳ ৭৮.৫০",
            hasPhoto = true,
            hasNote = true
        )
    }
}
