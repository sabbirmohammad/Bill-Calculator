package com.bangla.electricitybill.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bangla.electricitybill.core.util.BanglaDigitNormalizer
import com.bangla.electricitybill.presentation.motion.MotionTokens
import com.bangla.electricitybill.presentation.motion.pulsingGlow
import com.bangla.electricitybill.presentation.theme.MeterBezel
import com.bangla.electricitybill.presentation.theme.MeterBlackWheelBg
import com.bangla.electricitybill.presentation.theme.MeterBlackWheelText
import com.bangla.electricitybill.presentation.theme.MeterRedWheelBg
import com.bangla.electricitybill.presentation.theme.MeterRedWheelText

@Composable
fun MechanicalMeterDisplay(
    blackDigits: String,
    redDigit: String,
    modifier: Modifier = Modifier,
    capacity: Int = 5,
    isBangla: Boolean = false,
    subtitle: String? = null
) {
    val paddedBlack = remember(blackDigits, capacity) {
        blackDigits.padStart(capacity, '0').takeLast(capacity)
    }
    val safeRed = remember(redDigit) {
        if (redDigit.isNotEmpty()) redDigit.take(1) else "0"
    }

    // Adaptively scale digit wheel dimensions according to capacity so it never overflows narrow screens
    val totalDigits = capacity + 1
    val (digitWidth, digitHeight, fontSize) = remember(capacity) {
        when {
            totalDigits >= 8 -> Triple(28.dp, 44.dp, 20.sp)
            totalDigits == 7 -> Triple(32.dp, 48.dp, 23.sp)
            totalDigits == 6 -> Triple(36.dp, 50.dp, 26.sp)
            else -> Triple(38.dp, 52.dp, 28.sp)
        }
    }
    val digitSpacing = if (totalDigits >= 8) 2.dp else 3.dp

    // Concentric radius rule: outer radius = inner radius + padding
    // Inner window is 12dp + 12dp inner margin ~ 24dp outer card radius
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MeterBezel
        ),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Meter type & unit badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KILOWATT-HOUR (kWh)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF00E676).copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E676))
                            .pulsingGlow(durationMs = 900)
                    )
                    Text(
                        text = if (isBangla) "অ্যানালগ মিটার" else "ANALOG COUNTER",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF00E676),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mechanical Counter Bezel Window (Obsidian recessed cavity with layered border)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF030712))
                    .border(1.5.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Black whole kWh digits
                    paddedBlack.forEachIndexed { index, char ->
                        MeterWheelDigit(
                            digit = BanglaDigitNormalizer.formatDigits(char.toString(), isBangla),
                            baseColor = MeterBlackWheelBg,
                            textColor = MeterBlackWheelText,
                            width = digitWidth,
                            height = digitHeight,
                            fontSize = fontSize
                        )
                        if (index < paddedBlack.length - 1) {
                            Spacer(modifier = Modifier.width(digitSpacing))
                        }
                    }

                    // Optically aligned decimal separator dot (replaces drifting bullet char)
                    Box(
                        modifier = Modifier
                            .padding(horizontal = if (totalDigits >= 8) 4.dp else 6.dp)
                            .size(if (totalDigits >= 8) 4.dp else 5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF94A3B8))
                    )

                    // Single red decimal digit (0.1 kWh)
                    MeterWheelDigit(
                        digit = BanglaDigitNormalizer.formatDigits(safeRed, isBangla),
                        baseColor = MeterRedWheelBg,
                        textColor = MeterRedWheelText,
                        isRedDigit = true,
                        width = digitWidth,
                        height = digitHeight,
                        fontSize = fontSize
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Realistic Induction Rotor Disc (spinning aluminum disc with passing reference mark)
            InductionDiscMotion()

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle or Reconstructed numerical readout
            val completeText = "$paddedBlack.$safeRed kWh"
            Text(
                text = if (isBangla) BanglaDigitNormalizer.toBangla(completeText) else completeText,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Animated aluminum induction disc found in all mechanical induction electricity meters.
 * Rotates continuously with a passing red reference notch.
 */
@Composable
fun InductionDiscMotion(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "InductionDisc")
    val discOffset by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "discMark"
    )

    Box(
        modifier = modifier
            .width(160.dp)
            .height(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF334155),
                        Color(0xFF0F172A)
                    )
                )
            )
            .border(1.dp, Color(0xFF475569), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Metallic reflection hairline
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFFCBD5E1).copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    )
                )
        )
        // Red notch on spinning aluminum disc
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
        ) {
            val markWidth = 12.dp.toPx()
            val xPos = size.width * discOffset
            if (xPos >= -markWidth && xPos <= size.width + markWidth) {
                drawRect(
                    color = Color(0xFFEF4444),
                    topLeft = Offset(xPos, 1.dp.toPx()),
                    size = Size(markWidth, size.height - 2.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun MeterWheelDigit(
    digit: String,
    baseColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    isRedDigit: Boolean = false,
    width: Dp = 38.dp,
    height: Dp = 52.dp,
    fontSize: TextUnit = 28.sp
) {
    // Subtle cylinder gradient giving tactile rotary wheel illusion
    val cylinderGradient = if (isRedDigit) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF991B1B), // dark top bevel
                MeterRedWheelBg,   // vibrant center
                Color(0xFF7F1D1D)  // shadowed bottom
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1E2430), // top highlight
                MeterBlackWheelBg, // center
                Color(0xFF030712)  // bottom shadow
            )
        )
    }

    // Digit wheel: radius 5.dp concentric with 12.dp window (5dp + 7dp padding = 12dp)
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(5.dp))
            .background(cylinderGradient)
            .border(
                1.dp,
                if (isRedDigit) Color(0xFFB91C1C) else Color(0xFF334155).copy(alpha = 0.8f),
                RoundedCornerShape(5.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Physical rotary wheel center slit line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.12f))
        )
        // Rolling wheel digit animation
        AnimatedContent(
            targetState = digit,
            transitionSpec = {
                (slideInVertically(
                    animationSpec = MotionTokens.fastTween(),
                    initialOffsetY = { fullHeight -> fullHeight }
                ) + fadeIn(animationSpec = MotionTokens.fastTween())).togetherWith(
                    slideOutVertically(
                        animationSpec = MotionTokens.fastTween(),
                        targetOffsetY = { fullHeight -> -fullHeight }
                    ) + fadeOut(animationSpec = MotionTokens.fastTween())
                )
            },
            label = "WheelDigitRoll"
        ) { targetDigit ->
            Text(
                text = targetDigit,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
