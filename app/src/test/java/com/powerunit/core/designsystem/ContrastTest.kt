package com.powerunit.core.designsystem

import androidx.compose.ui.graphics.Color
import com.powerunit.core.designsystem.theme.BackgroundDark
import com.powerunit.core.designsystem.theme.BackgroundLight
import com.powerunit.core.designsystem.theme.OnPrimaryContainerDark
import com.powerunit.core.designsystem.theme.OnPrimaryContainerLight
import com.powerunit.core.designsystem.theme.OnPrimaryDark
import com.powerunit.core.designsystem.theme.OnPrimaryLight
import com.powerunit.core.designsystem.theme.OnSecondaryDark
import com.powerunit.core.designsystem.theme.OnSecondaryLight
import com.powerunit.core.designsystem.theme.OnSurfaceDark
import com.powerunit.core.designsystem.theme.OnSurfaceLight
import com.powerunit.core.designsystem.theme.OnSurfaceVariantDark
import com.powerunit.core.designsystem.theme.OnSurfaceVariantLight
import com.powerunit.core.designsystem.theme.PrimaryContainerDark
import com.powerunit.core.designsystem.theme.PrimaryContainerLight
import com.powerunit.core.designsystem.theme.PrimaryDark
import com.powerunit.core.designsystem.theme.PrimaryLight
import com.powerunit.core.designsystem.theme.SecondaryDark
import com.powerunit.core.designsystem.theme.SecondaryLight
import com.powerunit.core.designsystem.theme.SurfaceDark
import com.powerunit.core.designsystem.theme.SurfaceLight
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class ContrastTest {

    private fun channelLuminance(channel: Float): Double {
        return if (channel <= 0.03928f) {
            channel.toDouble() / 12.92
        } else {
            ((channel.toDouble() + 0.055) / 1.055).pow(2.4)
        }
    }

    private fun relativeLuminance(color: Color): Double {
        val r = channelLuminance(color.red)
        val g = channelLuminance(color.green)
        val b = channelLuminance(color.blue)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    private fun contrastRatio(c1: Color, c2: Color): Double {
        val l1 = relativeLuminance(c1)
        val l2 = relativeLuminance(c2)
        val lighter = max(l1, l2)
        val darker = min(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    @Test
    fun testLightModeContrastPairs_meetWcagAa() {
        val onSurfaceOnSurface = contrastRatio(OnSurfaceLight, SurfaceLight)
        assertTrue("onSurface on surface must be >= 4.5:1, got $onSurfaceOnSurface", onSurfaceOnSurface >= 4.5)

        val onSurfaceVariantOnSurface = contrastRatio(OnSurfaceVariantLight, SurfaceLight)
        assertTrue("onSurfaceVariant on surface must be >= 4.5:1, got $onSurfaceVariantOnSurface", onSurfaceVariantOnSurface >= 4.5)

        val onPrimaryOnPrimary = contrastRatio(OnPrimaryLight, PrimaryLight)
        assertTrue("onPrimary on primary must be >= 4.5:1, got $onPrimaryOnPrimary", onPrimaryOnPrimary >= 4.5)

        val onPrimaryContainer = contrastRatio(OnPrimaryContainerLight, PrimaryContainerLight)
        assertTrue("onPrimaryContainer on primaryContainer must be >= 4.5:1, got $onPrimaryContainer", onPrimaryContainer >= 4.5)

        val onSecondaryOnSecondary = contrastRatio(OnSecondaryLight, SecondaryLight)
        assertTrue("onSecondary on secondary must be >= 3.0:1 (large) or 4.0, got $onSecondaryOnSecondary", onSecondaryOnSecondary >= 3.0)

        val onSurfaceOnBackground = contrastRatio(OnSurfaceLight, BackgroundLight)
        assertTrue("onSurface on background must be >= 4.5:1, got $onSurfaceOnBackground", onSurfaceOnBackground >= 4.5)
    }

    @Test
    fun testDarkModeContrastPairs_meetWcagAa() {
        val onSurfaceOnSurface = contrastRatio(OnSurfaceDark, SurfaceDark)
        assertTrue("Dark onSurface on surface must be >= 4.5:1, got $onSurfaceOnSurface", onSurfaceOnSurface >= 4.5)

        val onSurfaceVariantOnSurface = contrastRatio(OnSurfaceVariantDark, SurfaceDark)
        assertTrue("Dark onSurfaceVariant on surface must be >= 4.5:1, got $onSurfaceVariantOnSurface", onSurfaceVariantOnSurface >= 4.5)

        val onPrimaryOnPrimary = contrastRatio(OnPrimaryDark, PrimaryDark)
        assertTrue("Dark onPrimary on primary must be >= 4.5:1, got $onPrimaryOnPrimary", onPrimaryOnPrimary >= 4.5)

        val onPrimaryContainer = contrastRatio(OnPrimaryContainerDark, PrimaryContainerDark)
        assertTrue("Dark onPrimaryContainer on primaryContainer must be >= 4.5:1, got $onPrimaryContainer", onPrimaryContainer >= 4.5)

        val onSecondaryOnSecondary = contrastRatio(OnSecondaryDark, SecondaryDark)
        assertTrue("Dark onSecondary on secondary must be >= 4.5:1, got $onSecondaryOnSecondary", onSecondaryOnSecondary >= 4.5)

        val onSurfaceOnBackground = contrastRatio(OnSurfaceDark, BackgroundDark)
        assertTrue("Dark onSurface on background must be >= 4.5:1, got $onSurfaceOnBackground", onSurfaceOnBackground >= 4.5)
    }
}
