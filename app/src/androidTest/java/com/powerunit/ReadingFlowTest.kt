package com.powerunit

import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.powerunit.core.designsystem.theme.VoltTrackTheme
import com.powerunit.feature.history.MonthSummaryUiModel
import com.powerunit.feature.history.component.MonthSummaryCard
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal

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
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag("nav_history").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("nav_history").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag("history_year_selector").fetchSemanticsNodes().isNotEmpty()
        }

        // Verify history year selector and month chips row are displayed
        composeTestRule.onNodeWithTag("history_year_selector").assertIsDisplayed()
        composeTestRule.onNodeWithTag("history_month_chips_row").assertIsDisplayed()

        // Month chip 11 is displayed and clickable
        composeTestRule.onNodeWithTag("history_month_chip_11").assertIsDisplayed()
        composeTestRule.onNodeWithTag("history_month_chip_11").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun readingFlow_openAddReading_andNavigateBack() {
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag("fab_add_reading").fetchSemanticsNodes().isNotEmpty()
        }

        // Open Add Reading via FAB
        composeTestRule.onNodeWithTag("fab_add_reading").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag("add_reading_title").fetchSemanticsNodes().isNotEmpty()
        }

        // Verify Add Reading screen components
        composeTestRule.onNodeWithTag("add_reading_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("reading_input_field").assertIsDisplayed()

        // Navigate back
        composeTestRule.onNodeWithTag("add_reading_back_btn").performClick()
        composeTestRule.waitForIdle()

        // Verify back on home
        composeTestRule.onNodeWithTag("nav_home").assertIsDisplayed()
    }

    @Test
    fun readingFlow_monthSummaryCard_displaysResponsively() {
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.setContent {
                VoltTrackTheme {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        MonthSummaryCard(
                            summary = MonthSummaryUiModel(
                                totalUnits = BigDecimal("47.2"),
                                totalCost = BigDecimal("245.50"),
                                readingsCount = 3,
                                avgPerDay = BigDecimal("1.5")
                            ),
                            useBengaliDigits = false,
                            isBangla = false,
                            hideAmounts = false
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()

        // Verify month summary card is displayed
        composeTestRule.onNodeWithTag("history_month_summary").assertIsDisplayed()

        // Verify all 4 responsive metrics and values
        composeTestRule.onNodeWithText("Total Units").assertIsDisplayed()
        composeTestRule.onNodeWithText("47.2 kWh").assertIsDisplayed()

        composeTestRule.onNodeWithText("Est. Bill").assertIsDisplayed()
        composeTestRule.onNodeWithText("৳245.50").assertIsDisplayed()

        composeTestRule.onNodeWithText("Readings").assertIsDisplayed()
        composeTestRule.onNodeWithText("3 total").assertIsDisplayed()

        composeTestRule.onNodeWithText("Daily Avg").assertIsDisplayed()
        composeTestRule.onNodeWithText("1.5 kWh/d").assertIsDisplayed()

        // Capture screenshot of the responsive card
        try {
            val bitmap = composeTestRule.onNodeWithTag("history_month_summary").captureToImage().asAndroidBitmap()
            val file = File("/sdcard/month_summary_fixed.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Test
    fun readingFlow_monthSummaryCard_displaysResponsively_bangla() {
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.setContent {
                VoltTrackTheme {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        MonthSummaryCard(
                            summary = MonthSummaryUiModel(
                                totalUnits = BigDecimal("47.2"),
                                totalCost = BigDecimal("245.50"),
                                readingsCount = 3,
                                avgPerDay = BigDecimal("1.5")
                            ),
                            useBengaliDigits = true,
                            isBangla = true,
                            hideAmounts = false
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("history_month_summary").assertIsDisplayed()
        composeTestRule.onNodeWithText("মোট বিদ্যুৎ").assertIsDisplayed()
        composeTestRule.onNodeWithText("আনুমানিক বিল").assertIsDisplayed()
        composeTestRule.onNodeWithText("রিডিং").assertIsDisplayed()
        composeTestRule.onNodeWithText("দৈনিক গড়").assertIsDisplayed()

        // Capture screenshot of the Bangla responsive card
        try {
            val bitmap = composeTestRule.onNodeWithTag("history_month_summary").captureToImage().asAndroidBitmap()
            val file = File("/sdcard/month_summary_bangla.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
