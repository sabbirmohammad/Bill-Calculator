package com.bangla.electricitybill.presentation.dashboard

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bangla.electricitybill.presentation.components.AddReadingActionButton
import com.bangla.electricitybill.presentation.components.HomeHeader
import com.bangla.electricitybill.presentation.components.MonthPickerDialog
import com.bangla.electricitybill.presentation.components.MonthlyConsumptionCard
import com.bangla.electricitybill.presentation.components.QuickStatisticsRow
import com.bangla.electricitybill.presentation.components.RecentReadingRow
import com.bangla.electricitybill.presentation.motion.staggeredEntrance
import com.bangla.electricitybill.presentation.theme.CardOutline
import com.bangla.electricitybill.presentation.theme.PrimaryPurple
import com.bangla.electricitybill.presentation.theme.PrimaryText
import com.bangla.electricitybill.presentation.theme.SecondaryText
import java.math.BigDecimal

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onAddReadingClick: () -> Unit,
    onViewAllReadingsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showMonthPicker by remember { mutableStateOf(false) }

    if (showMonthPicker) {
        MonthPickerDialog(
            selectedMonth = state.selectedMonth,
            onDismiss = { showMonthPicker = false },
            onSelectMonth = { viewModel.selectMonth(it) }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
            return@Scaffold
        }

        val hasReadings = state.dashboardData?.latestReading != null

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen 1: Modern Clean Home Header with integrated Date & Notification
            item {
                HomeHeader(
                    title = if (state.isBangla) "বিদ্যুৎ বিল ক্যালকুলেটর" else "Electricity Bill Calculator",
                    selectedMonth = state.selectedMonth,
                    onDateClick = { showMonthPicker = true },
                    onNotificationClick = onSettingsClick,
                    isBangla = state.isBangla
                )
            }

            if (!hasReadings) {
                // Screen 1 Section G: Dashboard Empty State
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .staggeredEntrance(1),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, CardOutline),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryPurple.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = PrimaryPurple,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = if (state.isBangla) "কোনো মিটার রিডিং নেই" else "No Meter Readings Yet",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = if (state.isBangla)
                                        "আপনার মিটারের বর্তমান রিডিং দিয়ে ট্র্যাকিং শুরু করুন। প্রাথমিক রিডিংটি বেসলাইন হিসেবে গণ্য হবে।"
                                    else
                                        "Add your first meter reading to start tracking daily electricity usage and calculating bills. Initial reading is treated as a baseline.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SecondaryText,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = onAddReadingClick,
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (state.isBangla) "প্রথম রিডিং যোগ করুন" else "+ Add Initial Reading",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Screen 1 Section C: Monthly Consumption Hero Card
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        MonthlyConsumptionCard(
                            consumptionKwh = state.monthConsumption,
                            estimatedBill = state.monthEstimatedBill,
                            subtitle = if (state.isBangla) "চলতি মাসের ব্যবহার (এ পর্যন্ত)" else "This Month (so far)",
                            isBangla = state.isBangla,
                            modifier = Modifier.staggeredEntrance(1)
                        )
                    }
                }

                // Screen 1 Section D: Quick Statistics Row
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        QuickStatisticsRow(
                            daysTracked = state.daysTracked,
                            unitsPerDay = state.unitsPerDay,
                            costPerDay = state.costPerDay,
                            isBangla = state.isBangla,
                            modifier = Modifier.staggeredEntrance(2)
                        )
                    }
                }

                // Screen 1 Section E: Add Today's Reading Primary Action Button
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        AddReadingActionButton(
                            onClick = onAddReadingClick,
                            text = if (state.isBangla) "আজকের রিডিং যোগ করুন" else "Add Today's Reading",
                            modifier = Modifier.staggeredEntrance(3)
                        )
                    }
                }

                // Screen 1 Section F: Recent Readings Section
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .staggeredEntrance(4),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (state.isBangla) "সাম্প্রতিক রিডিং" else "Recent Readings",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )

                            TextButton(onClick = onViewAllReadingsClick) {
                                Text(
                                    text = if (state.isBangla) "সব দেখুন" else "View All",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryPurple
                                )
                            }
                        }
                    }
                }

                // Recent Readings List Card
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .staggeredEntrance(5),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                            border = BorderStroke(1.dp, CardOutline)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                state.recentReadingsList.forEachIndexed { index, item ->
                                    RecentReadingRow(
                                        dateLabel = item.dateLabel,
                                        readingValue = item.readingValue,
                                        unitsDelta = item.unitsDelta,
                                        isBangla = state.isBangla,
                                        showDivider = index < state.recentReadingsList.lastIndex
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
