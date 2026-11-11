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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.powerunit.core.designsystem.theme.CardShape
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.HeroShape
import com.powerunit.core.designsystem.theme.Motion
import com.powerunit.core.designsystem.theme.VoltTrackTheme

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

@Preview(name = "ShimmerSkeleton - Light", showBackground = true)
@Composable
private fun PreviewSkeletonLight() {
    VoltTrackTheme(darkTheme = false) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            HeroCardSkeleton()
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCardSkeleton(modifier = Modifier.weight(1f))
                StatCardSkeleton(modifier = Modifier.weight(1f))
            }
        }
    }
}
