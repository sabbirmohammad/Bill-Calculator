package com.powerunit.core.designsystem.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.SegmentedTabsContainerShape
import com.powerunit.core.designsystem.theme.WattWiseTheme

/**
 * SegmentedTabs (§5.9):
 * - Height 44dp
 * - Radius 14dp container (surfaceContainer)
 * - Selected pill with primary background and spring slide animation
 */
@Composable
fun SegmentedTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    tabTestTags: List<String>? = null
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(SegmentedTabsContainerShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(4.dp)
    ) {
        val tabWidth = (maxWidth) / tabs.size
        val indicatorOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
            label = "TabIndicatorOffset"
        )

        // Animated pill background for selected tab
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary)
        )

        // Tab items
        Row(modifier = Modifier.fillMaxSize()) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = index == selectedIndex
                val testTagModifier = if (tabTestTags != null && index in tabTestTags.indices) {
                    Modifier.testTag(tabTestTags[index])
                } else {
                    Modifier
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .then(testTagModifier)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onTabSelected(index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(name = "SegmentedTabs - Light", showBackground = true)
@Composable
private fun PreviewSegmentedTabsLight() {
    WattWiseTheme(darkTheme = false) {
        SegmentedTabs(
            tabs = listOf("Day", "Week", "Month", "Year"),
            selectedIndex = 1,
            onTabSelected = {}
        )
    }
}

@Preview(name = "SegmentedTabs - Dark", showBackground = true)
@Composable
private fun PreviewSegmentedTabsDark() {
    WattWiseTheme(darkTheme = true) {
        SegmentedTabs(
            tabs = listOf("Day", "Week", "Month", "Year"),
            selectedIndex = 2,
            onTabSelected = {}
        )
    }
}

@Preview(name = "SegmentedTabs - 200% Font", fontScale = 2.0f, showBackground = true)
@Composable
private fun PreviewSegmentedTabsFont() {
    WattWiseTheme {
        SegmentedTabs(
            tabs = listOf("D", "W", "M", "Y"),
            selectedIndex = 0,
            onTabSelected = {}
        )
    }
}

@Preview(name = "SegmentedTabs - Bangla", showBackground = true)
@Composable
private fun PreviewSegmentedTabsBangla() {
    WattWiseTheme {
        SegmentedTabs(
            tabs = listOf("দিন", "সপ্তাহ", "মাস", "বছর"),
            selectedIndex = 2,
            onTabSelected = {}
        )
    }
}
