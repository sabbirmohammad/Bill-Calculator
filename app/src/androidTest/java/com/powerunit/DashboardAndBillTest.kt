package com.powerunit

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardAndBillTest {

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
    fun dashboard_displaysHeroCard_andNavigatesToBillBreakdown() {
        // Verify hero card on Home
        composeTestRule.onNodeWithTag("dashboard_hero_card").assertIsDisplayed()

        // Tap hero card to navigate to Bill Breakdown
        composeTestRule.onNodeWithTag("dashboard_hero_card").performClick()
        composeTestRule.waitForIdle()

        // Verify Bill screen components
        composeTestRule.onNodeWithTag("bill_summary_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bill_itemized_receipt_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("what_if_simulator_card").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("what_if_slider").performScrollTo().assertIsDisplayed()

        // Tap Back button
        composeTestRule.onNodeWithTag("bill_back_btn").performClick()
        composeTestRule.waitForIdle()

        // Back on Home
        composeTestRule.onNodeWithTag("dashboard_hero_card").assertIsDisplayed()
    }
}
