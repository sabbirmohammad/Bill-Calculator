package com.bangla.electricitybill.presentation.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard motion tokens and physics specifications for the Electricity Bill Calculator.
 * Follows the core motion design principles: consistent durations, natural spring physics,
 * accessible reduced motion support, and predictable enter/exit curves.
 */
object MotionTokens {

    // --- Durations (ms) ---
    const val DurationInstant = 50
    const val DurationFast = 180
    const val DurationNormal = 320
    const val DurationSlow = 480
    const val DurationLong = 650

    // --- Stagger Interval (ms) ---
    // Rule: between 50ms and 100ms for natural perception without feeling sluggish
    const val StaggerIntervalMs = 60

    // --- Distance / Offsets (dp) ---
    val DistanceSubtle: Dp = 6.dp
    val DistanceSm: Dp = 14.dp
    val DistanceMd: Dp = 28.dp
    val DistanceLg: Dp = 56.dp

    // --- Scale Ratios ---
    const val ScalePressed = 0.96f
    const val ScaleSubtle = 0.98f
    const val ScalePop = 1.04f

    // --- Easings ---
    val EmphasizedEasing: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val EmphasizedDecelerateEasing: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EmphasizedAccelerateEasing: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    val StandardEasing: Easing = FastOutSlowInEasing
    val DecelerateEasing: Easing = LinearOutSlowInEasing

    // --- Spring Specifications ---
    // Snappy spring for tactile interactive elements like buttons and chips
    val SpringSnappy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    // Gentle spring for card expansions, bottom sheets, and layouts
    val SpringGentle = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )

    // Bouncy spring for celebratory elements, badges, and counters
    val SpringBouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    // Spring for Dp animations (layouts, card elevation)
    val SpringDpGentle = spring<Dp>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )

    // --- Pre-configured Tween Specs ---
    fun <T> normalTween(delayMillis: Int = 0) = tween<T>(
        durationMillis = DurationNormal,
        delayMillis = delayMillis,
        easing = EmphasizedDecelerateEasing
    )

    fun <T> fastTween(delayMillis: Int = 0) = tween<T>(
        durationMillis = DurationFast,
        delayMillis = delayMillis,
        easing = FastOutSlowInEasing
    )

    fun <T> slowTween(delayMillis: Int = 0) = tween<T>(
        durationMillis = DurationSlow,
        delayMillis = delayMillis,
        easing = EmphasizedEasing
    )
}
