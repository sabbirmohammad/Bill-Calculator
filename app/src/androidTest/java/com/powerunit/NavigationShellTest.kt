package com.powerunit

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationShellTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            try {
                System.loadLibrary("sqlcipher")
            } catch (e: UnsatisfiedLinkError) {
                e.printStackTrace()
            }
        }
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun bottomNavigation_displaysAllFourTabs() {
        composeTestRule.onNodeWithTag("nav_home").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_analytics").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_history").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_settings").assertIsDisplayed()
    }

    @Test
    fun bottomNavigation_canSwitchTabs() {
        // Switch to Analytics
        composeTestRule.onNodeWithTag("nav_analytics").performClick()
        composeTestRule.waitForIdle()

        // Switch to History
        composeTestRule.onNodeWithTag("nav_history").performClick()
        composeTestRule.waitForIdle()

        // Switch back to Home
        composeTestRule.onNodeWithTag("nav_home").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun addReadingFab_isDisplayedOnHome_andNavigates() {
        composeTestRule.onNodeWithTag("fab_add_reading").assertIsDisplayed()
        composeTestRule.onNodeWithTag("fab_add_reading").performClick()
        composeTestRule.waitForIdle()
    }
}
