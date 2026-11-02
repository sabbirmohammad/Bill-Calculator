package com.bangla.electricitybill.presentation.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch

/**
 * Reusable Compose Motion Modifiers based on motion-patterns principles.
 */

/**
 * Adds a tactile spring press bounce effect to any interactive component.
 * Upon press, smoothly scales down to 0.96; releases with a snappy spring.
 */
fun Modifier.bounceClick(
    enabled: Boolean = true,
    scaleDown: Float = MotionTokens.ScalePressed,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    if (!enabled) return@composed this

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = MotionTokens.SpringSnappy,
        label = "bounceClickScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
            } else {
                Modifier
            }
        )
}

/**
 * Staggered entrance animation for list items and dashboard sections.
 * Automatically computes entrance delay based on the index (rule: 50-100ms per item).
 * Animates opacity (0 -> 1) and vertical offset (28dp -> 0dp).
 */
fun Modifier.staggeredEntrance(
    index: Int,
    baseDelayMs: Int = 40,
    intervalMs: Int = MotionTokens.StaggerIntervalMs
): Modifier = composed {
    val totalDelay = baseDelayMs + (index * intervalMs)

    val alphaAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(MotionTokens.DistanceMd.value) }

    LaunchedEffect(Unit) {
        launch {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = MotionTokens.normalTween(delayMillis = totalDelay)
            )
        }
        launch {
            offsetYAnim.animateTo(
                targetValue = 0f,
                animationSpec = MotionTokens.normalTween(delayMillis = totalDelay)
            )
        }
    }

    this.graphicsLayer {
        alpha = alphaAnim.value
        translationY = offsetYAnim.value * density
    }
}

/**
 * Subtle ambient pulsing alpha for active or warning indicators.
 */
@Composable
fun Modifier.pulsingGlow(
    minAlpha: Float = 0.55f,
    maxAlpha: Float = 1.0f,
    durationMs: Int = 1200
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsingGlow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = minAlpha,
        targetValue = maxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    return this.graphicsLayer { this.alpha = alpha }
}
