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
class ReadingFlowTest {

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
    fun readingFlow_navigateToHistory_andSwitchFilters() {
        // Navigate to History
        composeTestRule.onNodeWithTag("nav_history").performClick()
        composeTestRule.waitForIdle()

        // Verify history search field is displayed
        composeTestRule.onNodeWithTag("history_search_field").assertIsDisplayed()

        // Filter chips exist
        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("Resets").assertIsDisplayed()
        composeTestRule.onNodeWithText("Estimated").assertIsDisplayed()
        composeTestRule.onNodeWithText("High Usage").assertIsDisplayed()
    }

    @Test
    fun readingFlow_openAddReading_andNavigateBack() {
        // Open Add Reading via FAB
        composeTestRule.onNodeWithTag("fab_add_reading").performClick()
        composeTestRule.waitForIdle()

        // Verify Add Reading screen components
        composeTestRule.onNodeWithText("Add Reading").assertIsDisplayed()
        composeTestRule.onNodeWithTag("reading_input_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("save_reading_btn").assertIsDisplayed()

        // Navigate back
        composeTestRule.onNodeWithTag("add_reading_back_btn").performClick()
        composeTestRule.waitForIdle()

        // Verify back on home
        composeTestRule.onNodeWithTag("nav_home").assertIsDisplayed()
    }
}
