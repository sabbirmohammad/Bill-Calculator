package com.powerunit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.powerunit.core.designsystem.theme.WattWiseTheme
import com.powerunit.feature.addreading.ModernMeterResetCard
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MeterResetRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun meterResetRow_hasOnlySwitch_noCheckbox_andIsClickable() {
        var isReset by mutableStateOf(false)

        composeTestRule.setContent {
            WattWiseTheme {
                ModernMeterResetCard(
                    isMeterReset = isReset,
                    onMeterResetChange = { isReset = it }
                )
            }
        }

        // 1. Verify checkbox does NOT exist anywhere in the hierarchy
        val checkboxNodes = composeTestRule.onAllNodesWithTag("meter_reset_checkbox").fetchSemanticsNodes()
        assertTrue("Checkbox must be completely removed", checkboxNodes.isEmpty())

        // 2. Verify switch is displayed
        composeTestRule.onNodeWithTag("meter_reset_switch").assertIsDisplayed()

        // 3. Verify clicking the row toggles the state
        composeTestRule.onNodeWithTag("meter_reset_row").performClick()
        composeTestRule.waitForIdle()
        assertTrue("Expected meter reset to be true after clicking row", isReset)

        // 4. Verify clicking the switch toggles it back
        composeTestRule.onNodeWithTag("meter_reset_switch").performClick()
        composeTestRule.waitForIdle()
        assertTrue("Expected meter reset to be false after clicking switch", !isReset)
    }
}
