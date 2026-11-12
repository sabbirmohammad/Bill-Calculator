package com.powerunit

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToString
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TariffPersistenceTest {

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
    fun tariffEditor_editAndSave_persistsAcrossActivityRelaunch() {
        // Dismiss initial reading dialog if visible
        val laterNodes = composeTestRule.onAllNodesWithText("Later")
        if (laterNodes.fetchSemanticsNodes().isNotEmpty()) {
            laterNodes.onFirst().performClick()
            composeTestRule.waitForIdle()
        }

        // Navigate to Settings
        composeTestRule.onNodeWithTag("nav_settings").performClick()
        composeTestRule.waitForIdle()

        // Navigate to Tariff Editor
        composeTestRule.onNodeWithTag("settings_tariff_editor_row").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Wait for tariff editor to finish loading
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag("edit_charges_btn").fetchSemanticsNodes().isNotEmpty()
        }

        // Open edit charges sheet
        composeTestRule.onNodeWithTag("edit_charges_btn").performClick()
        composeTestRule.waitForIdle()

        // Change plan name to "My Custom Plan 2026"
        composeTestRule.onNodeWithTag("tariff_plan_name_input").performTextClearance()
        composeTestRule.onNodeWithTag("tariff_plan_name_input").performTextInput("My Custom Plan 2026")
        composeTestRule.waitForIdle()

        // Save charges sheet
        composeTestRule.onNodeWithTag("charges_sheet_save_btn").performClick()
        composeTestRule.waitForIdle()

        // Scroll to and tap Save Tariff Rates
        composeTestRule.onNodeWithTag("tariff_editor_lazy_column")
            .performScrollToNode(androidx.compose.ui.test.hasTestTag("save_tariff_btn"))
        composeTestRule.onNodeWithTag("save_tariff_btn").performClick()
        composeTestRule.waitForIdle()

        // Month picker: Tap Next
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithTag("apply_month_confirm_btn").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("apply_month_confirm_btn").performClick()
        composeTestRule.waitForIdle()

        // Confirmation dialog: Tap Confirm & Save
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithTag("confirm_save_tariff_btn").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("confirm_save_tariff_btn").performClick()
        composeTestRule.waitForIdle()

        // Wait for recalculation progress to dismiss
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithTag("tariff_editor_lazy_column").fetchSemanticsNodes().isNotEmpty()
        }

        // Scroll back to top summary card
        composeTestRule.onNodeWithTag("tariff_editor_lazy_column")
            .performScrollToNode(androidx.compose.ui.test.hasTestTag("tariff_summary_card"))
        composeTestRule.waitForIdle()

        // Verify plan name is displayed on screen
        composeTestRule.onNodeWithText("My Custom Plan 2026").assertIsDisplayed()

        // Recreate activity to simulate kill and relaunch
        composeTestRule.activityRule.scenario.recreate()
        composeTestRule.waitForIdle()

        // Dismiss initial reading dialog if visible after relaunch
        val laterNodesAfter = composeTestRule.onAllNodesWithText("Later")
        if (laterNodesAfter.fetchSemanticsNodes().isNotEmpty()) {
            laterNodesAfter.onFirst().performClick()
            composeTestRule.waitForIdle()
        }

        // If not already on Tariff Editor, navigate there
        val navSettingsNodes = composeTestRule.onAllNodesWithTag("nav_settings")
        if (navSettingsNodes.fetchSemanticsNodes().isNotEmpty()) {
            navSettingsNodes.onFirst().performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("settings_tariff_editor_row").performScrollTo().performClick()
            composeTestRule.waitForIdle()
        }

        // Wait for tariff editor to finish loading after relaunch
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithTag("edit_charges_btn").fetchSemanticsNodes().isNotEmpty()
        }

        // Verify the newly saved plan name persisted and did NOT revert to default!
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText("My Custom Plan 2026").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("My Custom Plan 2026").assertIsDisplayed()
    }
}
