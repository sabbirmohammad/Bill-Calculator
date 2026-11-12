package com.powerunit.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.WattWiseTheme

/**
 * TariffSlabRow (§5.7):
 * - Slab range, rate, units in slab, subtotal
 * - Right-aligned tabular numbers
 * - Thin 1dp divider
 * - Total row variant in titleMedium with primaryContainer background
 */
@Composable
fun TariffSlabRow(
    slabRange: String,
    rateText: String,
    unitsInSlab: String,
    subtotalText: String,
    modifier: Modifier = Modifier,
    isTotalRow: Boolean = false
) {
    if (isTotalRow) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = Dimens.cardPaddingStandard, vertical = Dimens.spacingSm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = slabRange,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = subtotalText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.End
            )
        }
    } else {
        Column(modifier = modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.cardPaddingStandard, vertical = Dimens.spacingSm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.2f)) {
                    Text(
                        text = slabRange,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "@ $rateText",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "$unitsInSlab units",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )

                Text(
                    text = subtotalText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }
            HorizontalDivider(
                thickness = Dimens.dividerThickness,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Preview(name = "TariffSlabRow - Light", showBackground = true)
@Composable
private fun PreviewTariffSlabRowLight() {
    WattWiseTheme(darkTheme = false) {
        Column {
            TariffSlabRow("0 - 75 units", "৳ 4.63/unit", "75", "৳ 347.25")
            TariffSlabRow("76 - 200 units", "৳ 6.31/unit", "42", "৳ 265.02")
            TariffSlabRow("Total Bill", "", "117", "৳ 612.27", isTotalRow = true)
        }
    }
}

@Preview(name = "TariffSlabRow - Dark", showBackground = true)
@Composable
private fun PreviewTariffSlabRowDark() {
    WattWiseTheme(darkTheme = true) {
        Column {
            TariffSlabRow("0 - 75 units", "৳ 4.63/unit", "75", "৳ 347.25")
            TariffSlabRow("76 - 200 units", "৳ 6.31/unit", "42", "৳ 265.02")
            TariffSlabRow("Total Bill", "", "117", "৳ 612.27", isTotalRow = true)
        }
    }
}

@Preview(name = "TariffSlabRow - 200% Font", fontScale = 2.0f, showBackground = true)
@Composable
private fun PreviewTariffSlabRowFont() {
    WattWiseTheme {
        TariffSlabRow("0-75", "4.63", "75", "৳ 347")
    }
}

@Preview(name = "TariffSlabRow - Bangla", showBackground = true)
@Composable
private fun PreviewTariffSlabRowBangla() {
    WattWiseTheme {
        TariffSlabRow("০ - ৭৫ ইউনিট", "৳ ৪.৬৩/ইউনিট", "৭৫", "৳ ৩৪৭.২৫")
    }
}
