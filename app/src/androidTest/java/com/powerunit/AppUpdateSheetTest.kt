package com.powerunit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.powerunit.core.designsystem.theme.WattWiseTheme
import com.powerunit.core.update.AppUpdateInstaller
import com.powerunit.core.update.model.UpdateInfo
import com.powerunit.core.update.model.UpdateState
import com.powerunit.core.update.ui.AppUpdateBottomSheet
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppUpdateSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val installer = AppUpdateInstaller(ApplicationProvider.getApplicationContext())

    @Test
    fun upToDateState_rendersCustomWattWiseSheet_andHandlesDismiss() {
        var dismissed = false
        var state by mutableStateOf<UpdateState>(UpdateState.UpToDate)

        composeTestRule.setContent {
            WattWiseTheme {
                AppUpdateBottomSheet(
                    state = state,
                    onStartDownload = {},
                    onCancelDownload = {},
                    onInstall = {},
                    onDismiss = { dismissed = true },
                    installer = installer
                )
            }
        }

        // Verify custom sheet container exists
        composeTestRule.onNodeWithTag("app_update_bottom_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("update_up_to_date_content").assertIsDisplayed()
        composeTestRule.onNodeWithTag("update_up_to_date_btn").assertIsDisplayed()

        // Click "Got It" pill button
        composeTestRule.onNodeWithTag("update_up_to_date_btn").performClick()
        assertTrue("onDismiss should be invoked when tapping Got It", dismissed)
    }

    @Test
    fun availableState_rendersUpdateDetails_andCallsDownload() {
        var downloadStarted = false
        val sampleInfo = UpdateInfo(
            currentVersion = "1.0.5",
            newVersion = "1.0.6",
            releaseName = "v1.0.6 Release",
            releaseNotes = "Improved responsiveness and smooth update sheet animations.",
            downloadUrl = "https://example.com/app.apk",
            apkSize = 15_000_000L,
            publishedAt = "2026-11-12"
        )

        composeTestRule.setContent {
            WattWiseTheme {
                AppUpdateBottomSheet(
                    state = UpdateState.Available(sampleInfo),
                    onStartDownload = { downloadStarted = true },
                    onCancelDownload = {},
                    onInstall = {},
                    onDismiss = {},
                    installer = installer
                )
            }
        }

        composeTestRule.onNodeWithTag("update_available_content").assertIsDisplayed()
        composeTestRule.onNodeWithText("Improved responsiveness and smooth update sheet animations.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("update_download_btn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("update_download_btn").performClick()
        assertTrue("Download should be started", downloadStarted)
    }

    @Test
    fun downloadingState_smoothlyUpdatesProgressWithoutRecreation() {
        var progressState by mutableStateOf(
            UpdateState.Downloading(
                info = UpdateInfo("1.0.5", "1.0.6", "v1.0.6", "Notes", "https://example.com", 10_000_000L, ""),
                progress = 0.25f,
                downloadedBytes = 2_500_000L,
                totalBytes = 10_000_000L,
                speedBytesPerSec = 1_500_000L
            )
        )

        composeTestRule.setContent {
            WattWiseTheme {
                AppUpdateBottomSheet(
                    state = progressState,
                    onStartDownload = {},
                    onCancelDownload = {},
                    onInstall = {},
                    onDismiss = {},
                    installer = installer
                )
            }
        }

        composeTestRule.onNodeWithTag("update_downloading_content").assertIsDisplayed()
        composeTestRule.onNodeWithText("25%").assertIsDisplayed()

        // Emulate rapid progress emission: should update smoothly without flickering or restarting
        progressState = progressState.copy(
            progress = 0.60f,
            downloadedBytes = 6_000_000L
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("update_downloading_content").assertIsDisplayed()
        composeTestRule.onNodeWithText("60%").assertIsDisplayed()
    }
}
