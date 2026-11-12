package com.powerunit

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.powerunit.core.designsystem.theme.VoltTrackTheme
import com.powerunit.feature.history.MonthSummaryUiModel
import com.powerunit.feature.history.component.MonthSummaryCard
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
class MonthSummaryCardTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun monthSummaryCard_displaysResponsively() {
        composeTestRule.setContent {
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
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("history_month_summary").assertIsDisplayed()
        composeTestRule.onNodeWithText("Total Units").assertIsDisplayed()
        composeTestRule.onNodeWithText("47.2 kWh").assertIsDisplayed()
        composeTestRule.onNodeWithText("Est. Bill").assertIsDisplayed()
        composeTestRule.onNodeWithText("৳245.50").assertIsDisplayed()
        composeTestRule.onNodeWithText("Readings").assertIsDisplayed()
        composeTestRule.onNodeWithText("3 total").assertIsDisplayed()
        composeTestRule.onNodeWithText("Daily Avg").assertIsDisplayed()
        composeTestRule.onNodeWithText("1.5 kWh/d").assertIsDisplayed()

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
    fun monthSummaryCard_displaysResponsively_bangla() {
        composeTestRule.setContent {
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
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("history_month_summary").assertIsDisplayed()
        composeTestRule.onNodeWithText("মোট বিদ্যুৎ").assertIsDisplayed()
        composeTestRule.onNodeWithText("আনুমানিক বিল").assertIsDisplayed()
        composeTestRule.onNodeWithText("রিডিং").assertIsDisplayed()
        composeTestRule.onNodeWithText("দৈনিক গড়").assertIsDisplayed()

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
