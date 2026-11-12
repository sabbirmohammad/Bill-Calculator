package com.powerunit.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.powerunit.core.designsystem.theme.WattWiseTheme

/**
 * InsightCard (§5.13):
 * - Icon well (40dp)
 * - One friendly sentence (bodyLarge)
 * - Optional dismiss action
 * - Tinted with tertiaryContainer or secondaryContainer
 */
@Composable
fun InsightCard(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Lightbulb,
    isSecondary: Boolean = false,
    onDismiss: (() -> Unit)? = null
) {
    val containerColor = if (isSecondary) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.tertiaryContainer
    }

    val contentColor = if (isSecondary) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onTertiaryContainer
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.level0)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.iconContainerSize)
                    .background(contentColor.copy(alpha = 0.12f), shape = IconWellShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(Dimens.iconSizeStandard)
                )
            }

            Spacer(modifier = Modifier.width(Dimens.iconToTextGap))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )

            if (onDismiss != null) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dismiss",
                        tint = contentColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(Dimens.iconSizeSmall)
                    )
                }
            }
        }
    }
}

@Preview(name = "InsightCard - Light", showBackground = true)
@Composable
private fun PreviewInsightCardLight() {
    WattWiseTheme(darkTheme = false) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InsightCard(
                message = "You used 12% less electricity this week than last week!",
                onDismiss = {}
            )
            InsightCard(
                message = "High usage on Friday evening accounted for 30% of weekly cost.",
                isSecondary = true,
                onDismiss = {}
            )
        }
    }
}

@Preview(name = "InsightCard - Dark", showBackground = true)
@Composable
private fun PreviewInsightCardDark() {
    WattWiseTheme(darkTheme = true) {
        InsightCard(
            message = "You are currently within your monthly budget of 300 units.",
            onDismiss = {}
        )
    }
}

@Preview(name = "InsightCard - 200% Font", fontScale = 2.0f, showBackground = true)
@Composable
private fun PreviewInsightCardFont() {
    WattWiseTheme {
        InsightCard(
            message = "You saved 12% units this week.",
            onDismiss = {}
        )
    }
}

@Preview(name = "InsightCard - Bangla", showBackground = true)
@Composable
private fun PreviewInsightCardBangla() {
    WattWiseTheme {
        InsightCard(
            message = "গত সপ্তাহের তুলনায় আপনি এই সপ্তাহে ১২% কম বিদ্যুৎ ব্যবহার করেছেন!",
            onDismiss = {}
        )
    }
}
