package com.powerunit.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.WattWiseTheme

/**
 * ChartCard (§5.14):
 * - Title
 * - SegmentedTabs (Day / Week / Month / Year)
 * - Chart composable slot
 * - "View as table" text button
 * - Semantic text summary for TalkBack
 */
@Composable
fun ChartCard(
    title: String,
    semanticSummary: String,
    modifier: Modifier = Modifier,
    tabs: List<String> = listOf("Day", "Week", "Month", "Year"),
    selectedTabIndex: Int = 1,
    onTabSelected: (Int) -> Unit = {},
    onViewAsTableClick: (() -> Unit)? = null,
    chartContent: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = semanticSummary },
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (onViewAsTableClick != null) {
                    TextButton(onClick = onViewAsTableClick) {
                        Text(
                            text = "View as table",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            SegmentedTabs(
                tabs = tabs,
                selectedIndex = selectedTabIndex,
                onTabSelected = onTabSelected
            )

            Spacer(modifier = Modifier.height(Dimens.spacingMd))

            Box(modifier = Modifier.fillMaxWidth()) {
                chartContent()
            }
        }
    }
}

@Preview(name = "ChartCard - Light", showBackground = true)
@Composable
private fun PreviewChartCardLight() {
    WattWiseTheme(darkTheme = false) {
        ChartCard(
            title = "Usage Trend",
            semanticSummary = "Average 8.4 units daily over the past week.",
            onViewAsTableClick = {}
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Chart Placeholder (160dp)", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Preview(name = "ChartCard - Dark", showBackground = true)
@Composable
private fun PreviewChartCardDark() {
    WattWiseTheme(darkTheme = true) {
        ChartCard(
            title = "Usage Trend",
            semanticSummary = "Average 8.4 units daily over the past week.",
            onViewAsTableClick = {}
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Chart Placeholder (160dp)", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
