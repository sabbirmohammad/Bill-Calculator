package com.bangla.electricitybill.presentation.reports

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bangla.electricitybill.core.util.BigDecimalMath
import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.presentation.components.SlabBreakdownCard
import com.bangla.electricitybill.presentation.theme.EnergyEmeraldGreen
import com.bangla.electricitybill.presentation.theme.WarningAmber
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyReportScreen(
    viewModel: MonthlyReportViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("মাসিক বিদ্যুৎ বিল স্টেটমেন্ট", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val reportText = buildShareReportText(state)
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, reportText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "বিল স্টেটমেন্ট শেয়ার করুন"))
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val period = state.period ?: return@Scaffold
        val yearMonth = YearMonth.from(period.periodStart)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period Header Card (Concentric 20dp outer / 10dp inner)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = DateUtils.formatMonthYear(yearMonth),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        val statusLabel = when (period.periodStatus.name) {
                            "COMPLETED" -> "সম্পন্ন"
                            "PARTIAL_TRACKING" -> "আংশিক মাস"
                            "INCOMPLETE_BOUNDARY_DATA" -> "সীমানা রিডিং অনুপস্থিত"
                            "ESTIMATED" -> "আনুমানিক"
                            else -> "চলমান"
                        }
                        val statusColor = when (period.periodStatus.name) {
                            "COMPLETED" -> EnergyEmeraldGreen
                            "PARTIAL_TRACKING", "INCOMPLETE_BOUNDARY_DATA" -> WarningAmber
                            else -> MaterialTheme.colorScheme.primary
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusColor.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(text = "শুরুর রিডিং", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = period.openingReading?.let { "$it kWh" } ?: "অজানা",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(text = "শেষ রিডিং", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = period.closingReading?.let { "$it kWh" } ?: "অজানা",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(text = "মোট এন্ট্রি", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${state.readingsInMonth.size} টি",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Detailed Bill Breakdown Card
            if (state.itemizedBill != null) {
                SlabBreakdownCard(itemizedBill = state.itemizedBill!!)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun buildShareReportText(state: MonthlyReportUiState): String {
    val period = state.period ?: return ""
    val bill = state.itemizedBill ?: return ""
    val month = DateUtils.formatMonthYear(YearMonth.from(period.periodStart))
    return """
        ⚡ বিদ্যুৎ বিল স্টেটমেন্ট — $month
        -------------------------------------------
        মোট বিদ্যুৎ ব্যবহার: ${BigDecimalMath.formatKwh(bill.totalUnits)}
        এনার্জি খরচ: ${BigDecimalMath.formatCurrency(bill.energyCost)}
        ডিমান্ড চার্জ: ${BigDecimalMath.formatCurrency(bill.demandCharge)}
        মিটার ভাড়া: ${BigDecimalMath.formatCurrency(bill.meterRent)}
        ভ্যাট (VAT): ${BigDecimalMath.formatCurrency(bill.vatAmount)}
        -------------------------------------------
        সর্বমোট আনুমানিক বিল: ${BigDecimalMath.formatCurrency(bill.totalBill)}
        
        (জেনারেটেড ফ্রম: বিদ্যুৎ মিটার ও বিল ক্যালকুলেটর)
    """.trimIndent()
}
