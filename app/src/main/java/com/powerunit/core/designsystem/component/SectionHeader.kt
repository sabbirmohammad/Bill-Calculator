package com.powerunit.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.VoltTrackTheme

/**
 * SectionHeader (§5.5):
 * - titleLarge on the left
 * - Optional text button on the right (labelLarge, primary)
 * - 48dp touch height
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = Dimens.minTouchTarget)
            .padding(vertical = Dimens.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (actionLabel != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                modifier = Modifier.defaultMinSize(minHeight = Dimens.minTouchTarget)
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(name = "SectionHeader - Light", showBackground = true)
@Composable
private fun PreviewSectionHeaderLight() {
    VoltTrackTheme(darkTheme = false) {
        SectionHeader(
            title = "Last 7 days",
            actionLabel = "View details",
            onActionClick = {}
        )
    }
}

@Preview(name = "SectionHeader - Dark", showBackground = true)
@Composable
private fun PreviewSectionHeaderDark() {
    VoltTrackTheme(darkTheme = true) {
        SectionHeader(
            title = "Last 7 days",
            actionLabel = "View details",
            onActionClick = {}
        )
    }
}

@Preview(name = "SectionHeader - 200% Font", fontScale = 2.0f, showBackground = true)
@Composable
private fun PreviewSectionHeaderFont() {
    VoltTrackTheme {
        SectionHeader(
            title = "Last 7 days",
            actionLabel = "Details",
            onActionClick = {}
        )
    }
}

@Preview(name = "SectionHeader - Bangla", showBackground = true)
@Composable
private fun PreviewSectionHeaderBangla() {
    VoltTrackTheme {
        SectionHeader(
            title = "গত ৭ দিন",
            actionLabel = "বিস্তারিত",
            onActionClick = {}
        )
    }
}
