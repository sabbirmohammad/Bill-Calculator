package com.powerunit.core.designsystem.component

import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.powerunit.R
import com.powerunit.core.designsystem.theme.CenterAddButtonGradient
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.Elevation
import com.powerunit.core.designsystem.theme.Motion
import com.powerunit.core.designsystem.theme.WattWiseTheme
import com.powerunit.navigation.Screen

/**
 * Custom Notched Bottom Bar Shape (§7):
 * - Smooth concave cutout (cubic curve, 76dp wide, 10dp deep) in the bar behind the center button
 * - Top corners 28dp rounded
 */
class NotchedBottomBarShape(
    private val notchWidth: Dp = Dimens.notchWidth,
    private val notchDepth: Dp = Dimens.notchDepth,
    private val cornerRadius: Dp = 28.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val r = with(density) { cornerRadius.toPx() }
        val nw = with(density) { notchWidth.toPx() }
        val nd = with(density) { notchDepth.toPx() }
        val w = size.width
        val h = size.height
        val cx = w / 2f

        val path = Path().apply {
            reset()
            // Top-left corner
            moveTo(0f, r)
            quadraticTo(0f, 0f, r, 0f)

            // Line to left start of notch cutout
            val notchStart = (cx - nw / 2f).coerceAtLeast(r)
            lineTo(notchStart, 0f)

            // Smooth concave cubic cutout:
            // First curve down from top surface to the bottom of the notch at center (cx, nd)
            cubicTo(
                notchStart + nw * 0.18f, 0f,
                cx - nw * 0.22f, nd,
                cx, nd
            )
            // Second curve up from center (cx, nd) back to top surface
            val notchEnd = (cx + nw / 2f).coerceAtMost(w - r)
            cubicTo(
                cx + nw * 0.22f, nd,
                notchEnd - nw * 0.18f, 0f,
                notchEnd, 0f
            )

            // Line to top-right corner
            lineTo(w - r, 0f)
            quadraticTo(w, 0f, w, r)

            // Right edge down
            lineTo(w, h)
            // Bottom edge across
            lineTo(0f, h)
            // Left edge up
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Draws the 1dp outlineVariant border along the notched top edge of the bottom bar.
 */
private fun Modifier.notchedTopBorder(
    color: Color,
    notchWidth: Dp = Dimens.notchWidth,
    notchDepth: Dp = Dimens.notchDepth,
    cornerRadius: Dp = 28.dp,
    strokeWidth: Dp = 1.dp
): Modifier = drawWithContent {
    drawContent()
    val r = cornerRadius.toPx()
    val nw = notchWidth.toPx()
    val nd = notchDepth.toPx()
    val w = size.width
    val cx = w / 2f

    val borderPath = Path().apply {
        reset()
        moveTo(0f, r)
        quadraticTo(0f, 0f, r, 0f)
        val notchStart = (cx - nw / 2f).coerceAtLeast(r)
        lineTo(notchStart, 0f)
        cubicTo(
            notchStart + nw * 0.18f, 0f,
            cx - nw * 0.22f, nd,
            cx, nd
        )
        val notchEnd = (cx + nw / 2f).coerceAtMost(w - r)
        cubicTo(
            cx + nw * 0.22f, nd,
            notchEnd - nw * 0.18f, 0f,
            notchEnd, 0f
        )
        lineTo(w - r, 0f)
        quadraticTo(w, 0f, w, r)
    }

    drawPath(
        path = borderPath,
        color = color,
        style = Stroke(width = strokeWidth.toPx())
    )
}

/**
 * WattWiseBottomBar (§7):
 * - 72dp tall + system gesture navigation inset
 * - 5 equal slots: Home · Analytics · [ + Add ] · History · Settings
 * - Custom concave notch cutout (76dp wide, 10dp deep, cubic curve)
 * - 1dp outlineVariant top border, 28dp top corners
 * - Center button: 60dp circle, raised 16dp above the bar, hero gradient (#4F46E5 → #7C3AED)
 * - Touch target 64dp, scale 0.94 spring + haptic tick on press
 * - Soft glow pulse when today's reading is missing
 * - Side tabs: filled icon + primaryContainer pill indicator (56x32dp) + labelSmall
 */
@Composable
fun WattWiseBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onAddReading: () -> Unit,
    modifier: Modifier = Modifier,
    hasReadingToday: Boolean = true
) {
    val navBarInsets = WindowInsets.navigationBars.asPaddingValues()
    val bottomInset = navBarInsets.calculateBottomPadding()
    val barHeight = Dimens.bottomNavBarHeight + bottomInset
    val density = LocalDensity.current

    // Glass surface: 80% surface alpha with solid fallback below API 31
    val surfaceColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MaterialTheme.colorScheme.surface.copy(alpha = Elevation.glassSurfaceAlpha)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val notchedShape = remember { NotchedBottomBarShape() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight + Dimens.centerAddRaised), // Raised 16dp allowance
        contentAlignment = Alignment.BottomCenter
    ) {
        // The notched bottom bar container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .shadow(elevation = Dimens.level2, shape = notchedShape)
                .clip(notchedShape)
                .background(surfaceColor)
                .notchedTopBorder(color = outlineColor)
                .padding(bottom = bottomInset)
        ) {
            // 5 equal slots layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.bottomNavBarHeight),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Slot 1: Home
                BottomNavTab(
                    screen = Screen.Home,
                    selected = currentRoute == Screen.Home.route,
                    onClick = { onNavigate(Screen.Home.route) },
                    modifier = Modifier.weight(1f)
                )

                // Slot 2: Analytics
                BottomNavTab(
                    screen = Screen.Analytics,
                    selected = currentRoute == Screen.Analytics.route,
                    onClick = { onNavigate(Screen.Analytics.route) },
                    modifier = Modifier.weight(1f)
                )

                // Slot 3: Center empty notch slot for Add button
                Spacer(modifier = Modifier.weight(1f))

                // Slot 4: History
                BottomNavTab(
                    screen = Screen.History,
                    selected = currentRoute == Screen.History.route,
                    onClick = { onNavigate(Screen.History.route) },
                    modifier = Modifier.weight(1f)
                )

                // Slot 5: Settings
                BottomNavTab(
                    screen = Screen.Settings,
                    selected = currentRoute == Screen.Settings.route,
                    onClick = { onNavigate(Screen.Settings.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Center Raised Add Button (§7)
        // Positioned 16dp above the 72dp bar top edge
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 0.dp),
            contentAlignment = Alignment.Center
        ) {
            CenterAddButton(
                onClick = onAddReading,
                pulseGlow = !hasReadingToday
            )
        }
    }
}

@Composable
private fun BottomNavTab(
    screen: Screen,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = stringResource(screen.titleRes)

    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("nav_${screen.route}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon container with pill indicator when selected (56 x 32 dp)
        Box(
            modifier = Modifier
                .size(width = Dimens.navIndicatorWidth, height = Dimens.navIndicatorHeight)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                contentDescription = title,
                tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Dimens.iconSizeStandard)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Label: labelSmall, always visible
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

/**
 * Center Add Button (§7):
 * - 60dp circle
 * - Raised 16dp above the bar
 * - Hero gradient (#4F46E5 → #7C3AED)
 * - White "add" icon 28dp
 * - Shadow: 0 8dp 20dp primary@30%
 * - No label; contentDescription = "Add reading"; touch target 64dp
 * - Press: scale 0.94 with spring + haptic tick
 * - Soft glow pulse when today's reading is missing
 */
@Composable
private fun CenterAddButton(
    onClick: () -> Unit,
    pulseGlow: Boolean = false,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Haptic tick on press
    LaunchedEffect(isPressed) {
        if (isPressed) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    // Scale animation with spring (damping 0.8)
    val scale by animateFloatAsState(
        targetValue = if (isPressed) Motion.buttonPressScale else 1.0f,
        animationSpec = spring(
            dampingRatio = Motion.cardSpringDamping,
            stiffness = 400f
        ),
        label = "AddButtonScale"
    )

    // Optional soft glow pulse
    val glowAlpha = if (pulseGlow) {
        val transition = rememberInfiniteTransition(label = "GlowTransition")
        val alpha by transition.animateFloat(
            initialValue = 0.15f,
            targetValue = 0.40f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "GlowAlpha"
        )
        alpha
    } else 0f

    val buttonDescription = stringResource(R.string.add_reading)

    // Touch target: 64dp
    Box(
        modifier = modifier
            .size(Dimens.centerAddTouchTarget)
            .semantics { contentDescription = buttonDescription }
            .testTag("fab_add_reading"),
        contentAlignment = Alignment.Center
    ) {
        // Soft glow ring
        if (pulseGlow) {
            Box(
                modifier = Modifier
                    .size(Dimens.centerAddButtonSize + 12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha))
            )
        }

        // 60dp Gradient Circle Button with Shadow
        Box(
            modifier = Modifier
                .scale(scale)
                .size(Dimens.centerAddButtonSize)
                .shadow(
                    elevation = Elevation.shadowElevationFab,
                    shape = CircleShape,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
                )
                .clip(CircleShape)
                .background(CenterAddButtonGradient)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(Dimens.iconSizeLarge)
            )
        }
    }
}

@Preview(name = "BottomBar - Light", showBackground = true)
@Composable
private fun PreviewWattWiseBottomBarLight() {
    WattWiseTheme(darkTheme = false) {
        WattWiseBottomBar(
            currentRoute = Screen.Home.route,
            onNavigate = {},
            onAddReading = {},
            hasReadingToday = false
        )
    }
}

@Preview(name = "BottomBar - Dark", showBackground = true)
@Composable
private fun PreviewWattWiseBottomBarDark() {
    WattWiseTheme(darkTheme = true) {
        WattWiseBottomBar(
            currentRoute = Screen.Analytics.route,
            onNavigate = {},
            onAddReading = {},
            hasReadingToday = true
        )
    }
}
