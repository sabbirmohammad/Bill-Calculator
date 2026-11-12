package com.powerunit

import android.view.WindowManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecurityAndPrivacyTest {

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
    fun securitySection_displaysSecurityControls_andHandlesToggles() {
        // 1. Navigate to Settings using tag
        composeTestRule.onNodeWithTag("nav_settings").performClick()
        composeTestRule.waitForIdle()

        // 2. Scroll to Security Card & verify controls exist
        composeTestRule.onNodeWithTag("settings_lazy_column")
            .performScrollToNode(hasTestTag("settings_security_card"))
        composeTestRule.onNodeWithTag("settings_security_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("app_lock_switch").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hide_amounts_switch").assertIsDisplayed()
        composeTestRule.onNodeWithTag("block_screenshots_switch").assertIsDisplayed()
        composeTestRule.onNodeWithTag("db_encryption_row").assertIsDisplayed()

        // 3. Test FLAG_SECURE toggle
        composeTestRule.onNodeWithTag("block_screenshots_switch").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(5000) {
            (composeTestRule.activity.window.attributes.flags and WindowManager.LayoutParams.FLAG_SECURE) != 0
        }

        var flags = composeTestRule.activity.window.attributes.flags
        val hasSecureFlag = (flags and WindowManager.LayoutParams.FLAG_SECURE) != 0
        assertTrue("Window should have FLAG_SECURE set", hasSecureFlag)

        // Toggle it back off
        composeTestRule.onNodeWithTag("block_screenshots_switch").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(5000) {
            (composeTestRule.activity.window.attributes.flags and WindowManager.LayoutParams.FLAG_SECURE) == 0
        }
        flags = composeTestRule.activity.window.attributes.flags
        assertEquals("Window should not have FLAG_SECURE set", 0, flags and WindowManager.LayoutParams.FLAG_SECURE)

        // 4. Test Privacy Mode toggle
        composeTestRule.onNodeWithTag("hide_amounts_switch").performClick()
        composeTestRule.waitForIdle()

        // Navigate to Home/Dashboard and verify masked currency
        composeTestRule.onNodeWithTag("nav_home").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dashboard_hero_card").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("৳ ••••").onFirst().assertIsDisplayed()

        // Turn Privacy Mode back off for clean state
        composeTestRule.onNodeWithTag("nav_settings").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_lazy_column")
            .performScrollToNode(hasTestTag("hide_amounts_switch"))
        composeTestRule.onNodeWithTag("hide_amounts_switch").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun clearData_triggersDoubleConfirmationDialogs() {
        // Navigate to Settings
        composeTestRule.onNodeWithTag("nav_settings").performClick()
        composeTestRule.waitForIdle()

        // Scroll to Clear All Data row
        composeTestRule.onNodeWithTag("settings_lazy_column")
            .performScrollToNode(hasTestTag("settings_clear_data_row"))
        composeTestRule.onNodeWithTag("settings_clear_data_row").performClick()
        composeTestRule.waitForIdle()

        // Verify Step 1 Warning Dialog is shown
        composeTestRule.onNodeWithTag("clear_data_continue_btn").assertIsDisplayed()

        // Click Continue
        composeTestRule.onNodeWithTag("clear_data_continue_btn").performClick()
        composeTestRule.waitForIdle()

        // Verify Step 2 Final Confirmation Dialog is shown
        composeTestRule.onNodeWithTag("clear_data_final_btn").assertIsDisplayed()

        // Dismiss safely
        composeTestRule.onNodeWithTag("clear_data_final_btn").assertIsDisplayed()
    }
}
