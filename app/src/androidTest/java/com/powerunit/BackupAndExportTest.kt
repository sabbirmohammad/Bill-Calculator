package com.powerunit

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupAndExportTest {

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
    fun historyScreen_displaysExportButtons_andSettingsNavigatesToBackup() {
        // Tap History tab on bottom navigation bar
        composeTestRule.onNodeWithTag("nav_history").performClick()
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag("history_export_pdf_btn").fetchSemanticsNodes().isNotEmpty()
        }

        // Verify PDF export button exists
        composeTestRule.onNodeWithTag("history_export_pdf_btn").assertIsDisplayed()

        // Tap Settings tab
        composeTestRule.onNodeWithTag("nav_settings").performClick()
        composeTestRule.waitForIdle()

        // Swipe up on lazy column to reveal Data section
        composeTestRule.onNodeWithTag("settings_lazy_column").performTouchInput { swipeUp() }
        composeTestRule.waitForIdle()

        // Click Encrypted Backup & Restore
        composeTestRule.onNodeWithTag("settings_backup_row").performClick()
        composeTestRule.waitForIdle()

        // Verify Backup & Restore elements
        composeTestRule.onNodeWithTag("create_backup_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("restore_backup_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("backup_passphrase_input").assertIsDisplayed()

        // Enter passphrase and verify button enables
        composeTestRule.onNodeWithTag("backup_passphrase_input").performTextInput("securePass123")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("create_backup_btn").assertIsEnabled()

        // Navigate back to Settings
        composeTestRule.onNodeWithTag("backup_back_btn").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }
}
