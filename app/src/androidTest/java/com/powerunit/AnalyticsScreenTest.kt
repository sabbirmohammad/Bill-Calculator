package com.powerunit

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnalyticsScreenTest {

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
    fun analyticsTab_displaysChartsAndInteractions() {
        // Navigate to Analytics tab on bottom navigation bar
        composeTestRule.onNodeWithText("Analytics").performClick()
        composeTestRule.waitForIdle()

        // Verify period selector exists
        composeTestRule.onNodeWithTag("period_tab_day").assertIsDisplayed()
        composeTestRule.onNodeWithTag("period_tab_week").assertIsDisplayed()
        composeTestRule.onNodeWithTag("period_tab_month").assertIsDisplayed()

        // Switch to Week period
        composeTestRule.onNodeWithTag("period_tab_week").performClick()
        composeTestRule.waitForIdle()

        // Switch to Month period
        composeTestRule.onNodeWithTag("period_tab_month").performClick()
        composeTestRule.waitForIdle()

        // Switch back to Day
        composeTestRule.onNodeWithTag("period_tab_day").performClick()
        composeTestRule.waitForIdle()

        // Toggle table view
        composeTestRule.onNodeWithTag("analytics_toggle_table_btn").performClick()
        composeTestRule.waitForIdle()

        // Toggle back to chart
        composeTestRule.onNodeWithTag("analytics_toggle_table_btn").performClick()
        composeTestRule.waitForIdle()
    }
}
