package com.bangla.electricitybill.presentation.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bangla.electricitybill.presentation.analytics.AnalyticsScreen
import com.bangla.electricitybill.presentation.analytics.AnalyticsViewModel
import com.bangla.electricitybill.presentation.bill.MonthlyBillScreen
import com.bangla.electricitybill.presentation.bill.MonthlyBillViewModel
import com.bangla.electricitybill.presentation.components.tactileScale
import com.bangla.electricitybill.presentation.dashboard.DashboardScreen
import com.bangla.electricitybill.presentation.dashboard.DashboardViewModel
import com.bangla.electricitybill.presentation.history.HistoryScreen
import com.bangla.electricitybill.presentation.history.HistoryViewModel
import com.bangla.electricitybill.presentation.motion.MotionTokens
import com.bangla.electricitybill.presentation.onboarding.OnboardingScreen
import com.bangla.electricitybill.presentation.onboarding.OnboardingViewModel
import com.bangla.electricitybill.presentation.reading.AddReadingScreen
import com.bangla.electricitybill.presentation.reading.AddReadingViewModel
import com.bangla.electricitybill.presentation.reports.MonthlyReportScreen
import com.bangla.electricitybill.presentation.reports.MonthlyReportViewModel
import com.bangla.electricitybill.presentation.settings.SettingsScreen
import com.bangla.electricitybill.presentation.settings.SettingsViewModel
import com.bangla.electricitybill.presentation.theme.AppDivider
import com.bangla.electricitybill.presentation.theme.LightPurple
import com.bangla.electricitybill.presentation.theme.PrimaryPurple
import com.bangla.electricitybill.presentation.theme.SecondaryText
import com.bangla.electricitybill.presentation.update.AppUpdateViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Onboarding : Screen("onboarding", "Onboarding")
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    object Readings : Screen("readings", "Readings", Icons.Default.ReceiptLong)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.BarChart)
    object Bill : Screen("bill", "Bill", Icons.Default.Description)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object AddReading : Screen("add_reading", "Add Reading", Icons.Default.Add)
    object MonthlyReport : Screen("report/{periodId}", "Report") {
        fun createRoute(periodId: String) = "report/$periodId"
    }

    // Compatibility alias
    val History = Readings
}

private val bottomNavRoutes = setOf(
    Screen.Dashboard.route,
    Screen.Readings.route,
    Screen.Analytics.route,
    Screen.Bill.route,
    Screen.Settings.route
)

@Composable
fun AppNavigation(
    startDestination: String,
    navController: NavHostController = rememberNavController(),
    updateViewModel: AppUpdateViewModel? = null,
    isBangla: Boolean = false
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isBottomBarVisible = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) {
                FintechBottomBar(
                    currentRoute = currentRoute,
                    isBangla = isBangla,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> (fullWidth * 0.25f).toInt() },
                    animationSpec = MotionTokens.normalTween()
                ) + fadeIn(animationSpec = MotionTokens.normalTween())
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> (-fullWidth * 0.25f).toInt() },
                    animationSpec = MotionTokens.normalTween()
                ) + fadeOut(animationSpec = MotionTokens.normalTween())
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> (-fullWidth * 0.25f).toInt() },
                    animationSpec = MotionTokens.normalTween()
                ) + fadeIn(animationSpec = MotionTokens.normalTween())
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> (fullWidth * 0.25f).toInt() },
                    animationSpec = MotionTokens.normalTween()
                ) + fadeOut(animationSpec = MotionTokens.normalTween())
            }
        ) {
            composable(Screen.Onboarding.route) {
                val viewModel: OnboardingViewModel = hiltViewModel()
                OnboardingScreen(
                    viewModel = viewModel,
                    onComplete = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // Screen 1: Home Dashboard
            composable(Screen.Dashboard.route) {
                val viewModel: DashboardViewModel = hiltViewModel()
                DashboardScreen(
                    viewModel = viewModel,
                    onAddReadingClick = { navController.navigate(Screen.AddReading.route) },
                    onViewAllReadingsClick = { navController.navigate(Screen.Readings.route) },
                    onSettingsClick = { navController.navigate(Screen.Settings.route) }
                )
            }

            // Tab 2: Readings List (History)
            composable(Screen.Readings.route) {
                val viewModel: HistoryViewModel = hiltViewModel()
                HistoryScreen(
                    viewModel = viewModel,
                    onAddReadingClick = { navController.navigate(Screen.AddReading.route) }
                )
            }

            // Screen 3: Analytics
            composable(Screen.Analytics.route) {
                val viewModel: AnalyticsViewModel = hiltViewModel()
                AnalyticsScreen(
                    viewModel = viewModel,
                    onNavigateToReadings = { navController.navigate(Screen.Readings.route) }
                )
            }

            // Screen 4: Monthly Bill
            composable(Screen.Bill.route) {
                val viewModel: MonthlyBillViewModel = hiltViewModel()
                MonthlyBillScreen(
                    viewModel = viewModel
                )
            }

            // Screen 5: Settings
            composable(Screen.Settings.route) {
                val viewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    viewModel = viewModel,
                    updateViewModel = updateViewModel
                )
            }

            // Screen 2: Add Meter Reading (Focused modal screen with back button)
            composable(Screen.AddReading.route) {
                val viewModel: AddReadingViewModel = hiltViewModel()
                AddReadingScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Direct Monthly Report route (retained for deep linking)
            composable(
                route = Screen.MonthlyReport.route,
                arguments = listOf(navArgument("periodId") { type = NavType.StringType })
            ) {
                val viewModel: MonthlyReportViewModel = hiltViewModel()
                MonthlyReportScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * Modern 5-Item FinTech Bottom Navigation Bar matching the reference design:
 * 1. Home
 * 2. Readings
 * 3. Analytics
 * 4. Bill
 * 5. Settings
 */
@Composable
fun FintechBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    isBangla: Boolean = false
) {
    val navItems = listOf(
        Screen.Dashboard,
        Screen.Readings,
        Screen.Analytics,
        Screen.Bill,
        Screen.Settings
    )

    val dividerColor = AppDivider

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = dividerColor,
                    start = Offset.Zero,
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            },
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(68.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { screen ->
                val selected = currentRoute == screen.route
                val localizedTitle = if (isBangla) {
                    when (screen.route) {
                        Screen.Dashboard.route -> "হোম"
                        Screen.Readings.route -> "রিডিং"
                        Screen.Analytics.route -> "অ্যানালিটিক্স"
                        Screen.Bill.route -> "বিল"
                        Screen.Settings.route -> "সেটিংস"
                        else -> screen.title
                    }
                } else {
                    screen.title
                }
                FintechNavItem(
                    title = localizedTitle,
                    icon = screen.icon ?: Icons.Default.Home,
                    selected = selected,
                    onClick = { onNavigate(screen.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FintechNavItem(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val animatedIconColor by animateColorAsState(
        targetValue = if (selected) PrimaryPurple else SecondaryText,
        animationSpec = MotionTokens.fastTween(),
        label = "navColor"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1.0f,
        animationSpec = MotionTokens.SpringSnappy,
        label = "navScale"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .tactileScale(interactionSource),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Soft rounded active indicator container as shown in Screen 3 & Screen 4
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected) LightPurple.copy(alpha = 0.85f) else Color.Transparent)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = animatedIconColor,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    }
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.sp,
            color = animatedIconColor,
            maxLines = 1
        )
    }
}
