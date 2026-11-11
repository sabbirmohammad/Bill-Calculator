package com.powerunit

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsAndTariffTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            try {
                System.loadLibrary("sqlcipher")
            } catch (_: UnsatisfiedLinkError) {
                // Ignore if already loaded
            }
        }
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun settingsTab_opensSettings_andNavigatesToTariffEditor() {
        // Tap Settings tab on bottom navigation bar
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.waitForIdle()

        // Verify Settings cards are displayed
        composeTestRule.onNodeWithTag("settings_meter_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_billing_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_reminders_card").assertIsDisplayed()

        // Toggle reminder switch
        composeTestRule.onNodeWithTag("reminder_switch").performClick()
        composeTestRule.waitForIdle()

        // Scroll to and toggle hide amounts switch
        composeTestRule.onNodeWithTag("hide_amounts_switch").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Navigate to Tariff Editor
        composeTestRule.onNodeWithTag("settings_tariff_editor_row").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Verify Tariff Editor elements
        composeTestRule.onNodeWithTag("tariff_name_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tariff_slabs_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("add_slab_btn").assertIsDisplayed()

        // Tap Back to return to Settings
        composeTestRule.onNodeWithTag("tariff_back_btn").performClick()
        composeTestRule.waitForIdle()

        // Confirm back on Settings
        composeTestRule.onNodeWithTag("settings_meter_card").assertIsDisplayed()
    }
}
