package com.powerunit.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.HeroShape
import com.powerunit.core.designsystem.theme.Motion
import com.powerunit.core.designsystem.theme.PillShape
import com.powerunit.core.designsystem.theme.WattWiseTheme

/**
 * Skeleton shimmer (§5.12):
 * - 1.2s shimmer loop
 * - Matches component shapes (hero, stat, list items)
 * - No spinners on main screens
 */
fun Modifier.shimmerEffect(shape: Shape = CardShape): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "ShimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = Motion.skeletonShimmerDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerTranslate"
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    this
        .clip(shape)
        .background(brush)
}

@Composable
fun HeroCardSkeleton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .shimmerEffect(HeroShape)
    )
}

@Composable
fun StatCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard)
        ) {
            Box(modifier = Modifier.size(Dimens.iconContainerSize).shimmerEffect(CardShape))
            Spacer(modifier = Modifier.height(Dimens.spacingSm))
            Box(modifier = Modifier.size(width = 80.dp, height = 16.dp).shimmerEffect(CardShape))
            Spacer(modifier = Modifier.height(Dimens.cardValueToHintGap))
            Box(modifier = Modifier.size(width = 120.dp, height = 28.dp).shimmerEffect(CardShape))
        }
    }
}

@Composable
fun UsageSummarySkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(3) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(28.dp).shimmerEffect(CardShape))
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.size(width = 60.dp, height = 14.dp).shimmerEffect(CardShape))
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.size(width = 72.dp, height = 20.dp).shimmerEffect(CardShape))
                }
            }
        }
    }
}

@Composable
fun BudgetOutlookSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(Dimens.cardPaddingStandard)) {
            Box(modifier = Modifier.size(width = 140.dp, height = 18.dp).shimmerEffect(CardShape))
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.size(110.dp).shimmerEffect(PillShape))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(3) {
                        Box(modifier = Modifier.fillMaxWidth().height(24.dp).shimmerEffect(CardShape))
                    }
                }
            }
        }
    }
}

@Composable
fun ReadingItemSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPaddingStandard),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(width = 100.dp, height = 16.dp).shimmerEffect(CardShape))
                Box(modifier = Modifier.size(width = 60.dp, height = 12.dp).shimmerEffect(CardShape))
            }
            Box(modifier = Modifier.size(width = 70.dp, height = 24.dp).shimmerEffect(CardShape))
        }
    }
}

@Composable
fun DashboardSkeleton(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Dimens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.cardGap),
        contentPadding = PaddingValues(top = Dimens.contentTopPadding, bottom = 88.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.size(width = 140.dp, height = 24.dp).shimmerEffect(CardShape))
                Box(modifier = Modifier.size(width = 90.dp, height = 28.dp).shimmerEffect(PillShape))
            }
        }
        item { HeroCardSkeleton() }
        item { UsageSummarySkeleton() }
        item { BudgetOutlookSkeleton() }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .shimmerEffect(CardShape)
            )
        }
        item { ReadingItemSkeleton() }
    }
}

@Composable
fun HistorySkeleton(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Dimens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.cardGap),
        contentPadding = PaddingValues(top = Dimens.contentTopPadding, bottom = 88.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(width = 100.dp, height = 36.dp).shimmerEffect(PillShape))
                Box(modifier = Modifier.weight(1f).height(36.dp).shimmerEffect(PillShape))
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(3) {
                        Box(modifier = Modifier.size(width = 80.dp, height = 36.dp).shimmerEffect(CardShape))
                    }
                }
            }
        }
        items(4) {
            ReadingItemSkeleton()
        }
    }
}

@Composable
fun AnalyticsSkeleton(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Dimens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.cardGap),
        contentPadding = PaddingValues(top = Dimens.contentTopPadding, bottom = 88.dp)
    ) {
        item {
            Box(modifier = Modifier.fillMaxWidth().height(40.dp).shimmerEffect(PillShape))
        }
        item {
            Box(modifier = Modifier.fillMaxWidth().height(220.dp).shimmerEffect(CardShape))
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f).height(90.dp).shimmerEffect(CardShape))
                Box(modifier = Modifier.weight(1f).height(90.dp).shimmerEffect(CardShape))
            }
        }
        item {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp).shimmerEffect(CardShape))
        }
    }
}

@Composable
fun BillSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Dimens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.cardGap)
    ) {
        Spacer(modifier = Modifier.height(Dimens.contentTopPadding))
        Box(modifier = Modifier.fillMaxWidth().height(180.dp).shimmerEffect(HeroShape))
        Box(modifier = Modifier.fillMaxWidth().height(240.dp).shimmerEffect(CardShape))
        Box(modifier = Modifier.fillMaxWidth().height(120.dp).shimmerEffect(CardShape))
    }
}

@Preview(name = "ShimmerSkeleton - Light", showBackground = true)
@Composable
private fun PreviewSkeletonLight() {
    WattWiseTheme(darkTheme = false) {
        DashboardSkeleton()
    }
}
