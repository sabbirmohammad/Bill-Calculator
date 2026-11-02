package com.bangla.electricitybill.presentation.motion

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * Animated rolling digit odometer effect.
 * When digit values update (e.g. typing or reading updates), the digit rolls vertically
 * mimicking a mechanical counter cylinder.
 */
@Composable
fun AnimatedRollingDigit(
    digit: Char,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    isIncreasing: Boolean = true
) {
    AnimatedContent(
        targetState = digit,
        transitionSpec = {
            val enterTransition = slideInVertically(
                animationSpec = MotionTokens.fastTween(),
                initialOffsetY = { fullHeight -> if (isIncreasing) fullHeight else -fullHeight }
            ) + fadeIn(animationSpec = MotionTokens.fastTween())

            val exitTransition = slideOutVertically(
                animationSpec = MotionTokens.fastTween(),
                targetOffsetY = { fullHeight -> if (isIncreasing) -fullHeight else fullHeight }
            ) + fadeOut(animationSpec = MotionTokens.fastTween())

            enterTransition.togetherWith(exitTransition)
        },
        label = "RollingDigit"
    ) { targetChar ->
        Text(
            text = targetChar.toString(),
            style = style,
            color = color,
            modifier = modifier
        )
    }
}

/**
 * Smooth ticker for numbers / text where individual digits slide vertically when changed.
 */
@Composable
fun AnimatedRollingText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        text.forEachIndexed { index, char ->
            if (char.isDigit()) {
                AnimatedRollingDigit(
                    digit = char,
                    style = style,
                    color = color
                )
            } else {
                Text(
                    text = char.toString(),
                    style = style,
                    color = color,
                    fontWeight = fontWeight
                )
            }
        }
    }
}
