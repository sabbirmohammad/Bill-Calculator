package com.powerunit.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.powerunit.core.designsystem.theme.ButtonShape
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.VoltTrackTheme

/**
 * EmptyState (§5.10):
 * - 160dp illustration/icon
 * - titleLarge
 * - bodyMedium muted
 * - Primary CTA
 * - 24dp gaps
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimens.screenHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier.size(160.dp)
        )

        Spacer(modifier = Modifier.height(Dimens.spacingXl))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimens.spacingSm))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(Dimens.spacingXl))
            Button(
                onClick = onActionClick,
                shape = ButtonShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.buttonHeightPrimary),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Preview(name = "EmptyState - Light", showBackground = true)
@Composable
private fun PreviewEmptyStateLight() {
    VoltTrackTheme(darkTheme = false) {
        EmptyState(
            icon = Icons.Filled.ElectricBolt,
            title = "No readings yet",
            description = "Add your first meter reading to start tracking your electricity usage and budget.",
            actionLabel = "Add first reading",
            onActionClick = {}
        )
    }
}

@Preview(name = "EmptyState - Dark", showBackground = true)
@Composable
private fun PreviewEmptyStateDark() {
    VoltTrackTheme(darkTheme = true) {
        EmptyState(
            icon = Icons.Filled.ElectricBolt,
            title = "No readings yet",
            description = "Add your first meter reading to start tracking your electricity usage and budget.",
            actionLabel = "Add first reading",
            onActionClick = {}
        )
    }
}

@Preview(name = "EmptyState - 200% Font", fontScale = 2.0f, showBackground = true)
@Composable
private fun PreviewEmptyStateFont() {
    VoltTrackTheme {
        EmptyState(
            icon = Icons.Filled.ElectricBolt,
            title = "No readings",
            description = "Add a reading to track usage.",
            actionLabel = "Add reading",
            onActionClick = {}
        )
    }
}

@Preview(name = "EmptyState - Bangla", showBackground = true)
@Composable
private fun PreviewEmptyStateBangla() {
    VoltTrackTheme {
        EmptyState(
            icon = Icons.Filled.ElectricBolt,
            title = "কোনো রিডিং পাওয়া যায়নি",
            description = "বিদ্যুৎ ব্যবহার ট্র্যাক করতে আপনার প্রথম রিডিং যোগ করুন।",
            actionLabel = "প্রথম রিডিং যোগ করুন",
            onActionClick = {}
        )
    }
}
