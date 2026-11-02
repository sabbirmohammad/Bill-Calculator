package com.bangla.electricitybill.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

import com.bangla.electricitybill.presentation.motion.MotionTokens

/**
 * Applies a subtle tactile spring scale response (0.97x) on press to make interactive
 * elements feel tangible and responsive without exaggerated motion.
 */
fun Modifier.tactileClick(
    targetScale: Float = MotionTokens.ScalePressed,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit
): Modifier = composed {
    val actualSource = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by actualSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) targetScale else 1f,
        animationSpec = MotionTokens.SpringSnappy,
        label = "tactileClickScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = actualSource,
            indication = androidx.compose.material3.ripple(),
            enabled = enabled,
            onClick = onClick
        )
}

/**
 * Modifier that applies tactile scaling linked to an existing interaction source.
 */
fun Modifier.tactileScale(
    interactionSource: MutableInteractionSource,
    targetScale: Float = MotionTokens.ScalePressed
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) targetScale else 1f,
        animationSpec = MotionTokens.SpringSnappy,
        label = "tactileScale"
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
