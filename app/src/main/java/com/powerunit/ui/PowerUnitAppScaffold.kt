package com.powerunit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.powerunit.R
import com.powerunit.core.designsystem.component.WattWiseBottomBar
import com.powerunit.core.designsystem.gallery.DesignGalleryScreen
import com.powerunit.core.designsystem.theme.CenterAddButtonGradient
import com.powerunit.core.designsystem.theme.Dimens
import com.powerunit.core.designsystem.theme.Elevation
import com.powerunit.core.update.AppUpdateViewModel
import com.powerunit.core.update.model.UpdateState
import com.powerunit.core.update.ui.AppUpdateBottomSheet
import com.powerunit.feature.addreading.AddReadingScreen
import com.powerunit.feature.analytics.AnalyticsScreen
import com.powerunit.feature.backup.BackupScreen
import com.powerunit.feature.bill.BillScreen
import com.powerunit.feature.dashboard.DashboardScreen
import com.powerunit.feature.history.HistoryScreen
import com.powerunit.feature.onboarding.OnboardingScreen
import com.powerunit.feature.settings.SettingsScreen
import com.powerunit.feature.tariff.TariffEditorScreen
import com.powerunit.navigation.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private fun navigateToTopLevel(navController: NavHostController, route: String, currentRoute: String?) {
    if (currentRoute != route) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}

@Composable
fun PowerUnitAppScaffold(
    navController: NavHostController = rememberNavController(),
    updateViewModel: AppUpdateViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val updateState by updateViewModel.updateState.collectAsState()

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val isTopLevelDestination = currentRoute in Screen.bottomNavScreens.map { it.route }

    if (isTablet && isTopLevelDestination) {
        Row(modifier = Modifier.fillMaxSize()) {
            PowerUnitNavigationRail(
                currentRoute = currentRoute,
                onNavigate = { route -> navigateToTopLevel(navController, route, currentRoute) },
                onAddReading = { navController.navigate("add_reading") }
            )
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(modifier = Modifier.width(Dimens.maxContentWidth)) {
                        PowerUnitNavHost(
                            navController = navController,
                            scope = scope,
                            snackbarHostState = snackbarHostState
                        )
                    }
                }
            }
        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (isTopLevelDestination) {
                    WattWiseBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { route -> navigateToTopLevel(navController, route, currentRoute) },
                        onAddReading = { navController.navigate("add_reading") },
                        hasReadingToday = true
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                PowerUnitNavHost(
                    navController = navController,
                    scope = scope,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }

    AppUpdateBottomSheet(
        state = updateState,
        onStartDownload = { info -> updateViewModel.startDownload(info) },
        onCancelDownload = { updateViewModel.cancelDownload() },
        onInstall = { apkFile -> updateViewModel.installApk(apkFile) },
        onRetry = { failedInfo -> updateViewModel.retry(failedInfo) },
        onDismiss = { updateViewModel.dismiss() },
        installer = updateViewModel.updateManager.installer
    )
}

/**
 * NavigationRail for Medium (600–840dp) and Expanded (§7):
 * - 80dp width
 * - 56dp gradient circle Add button placed at top of rail above all 4 tabs
 */
@Composable
private fun PowerUnitNavigationRail(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onAddReading: () -> Unit
) {
    val addDescription = stringResource(R.string.add_reading)

    NavigationRail(
        modifier = Modifier.width(Dimens.navRailWidth),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        header = {
            // Top Add button: 56dp gradient circle above tabs
            Box(
                modifier = Modifier
                    .padding(vertical = Dimens.spacingMd)
                    .size(56.dp)
                    .shadow(
                        elevation = Elevation.shadowElevationFab,
                        shape = CircleShape,
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
                    )
                    .clip(CircleShape)
                    .background(CenterAddButtonGradient)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = onAddReading
                    )
                    .semantics { contentDescription = addDescription }
                    .testTag("fab_add_reading"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(Dimens.iconSizeLarge)
                )
            }
        }
    ) {
        Spacer(modifier = Modifier.height(Dimens.xs))
        Screen.bottomNavScreens.forEach { screen ->
            RailItem(
                screen = screen,
                selected = currentRoute == screen.route,
                onClick = { onNavigate(screen.route) }
            )
        }
    }
}

@Composable
private fun RailItem(
    screen: Screen,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationRailItem(
        modifier = Modifier.testTag("nav_${screen.route}"),
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                contentDescription = stringResource(screen.titleRes)
            )
        },
        label = { Text(stringResource(screen.titleRes), style = MaterialTheme.typography.labelSmall) },
        colors = NavigationRailItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun PowerUnitNavHost(
    navController: NavHostController,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable("onboarding") {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            DashboardScreen(
                onNavigateToAddReading = { navController.navigate("add_reading") },
                onNavigateToBill = { navController.navigate("bill") },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToEditReading = { id -> navController.navigate("edit_reading/$id") }
            )
        }
        composable("bill") {
            BillScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Analytics.route) {
            AnalyticsScreen(
                onNavigateToAddReading = { navController.navigate("add_reading") }
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateToAddReading = { navController.navigate("add_reading") },
                onNavigateToEditReading = { id -> navController.navigate("edit_reading/$id") }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToTariffEditor = { navController.navigate("tariff_editor") },
                onNavigateToBackup = { navController.navigate("backup") },
                onNavigateToDesignGallery = { navController.navigate("design_gallery") }
            )
        }
        composable("backup") {
            BackupScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("tariff_editor") {
            TariffEditorScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("design_gallery") {
            DesignGalleryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("add_reading") {
            AddReadingScreen(
                onNavigateBack = { navController.popBackStack() },
                onReadingSaved = { msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            )
        }
        composable(
            route = "edit_reading/{readingId}",
            arguments = listOf(navArgument("readingId") { type = NavType.LongType })
        ) {
            AddReadingScreen(
                onNavigateBack = { navController.popBackStack() },
                onReadingSaved = { msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            )
        }
    }
}
