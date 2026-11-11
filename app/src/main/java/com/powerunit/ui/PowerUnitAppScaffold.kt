package com.powerunit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.powerunit.R
import com.powerunit.core.designsystem.theme.ButtonShape
import com.powerunit.feature.addreading.AddReadingScreen
import com.powerunit.feature.analytics.AnalyticsScreen
import com.powerunit.feature.backup.BackupScreen
import com.powerunit.feature.settings.SettingsScreen
import com.powerunit.feature.tariff.TariffEditorScreen
import com.powerunit.feature.bill.BillScreen
import com.powerunit.feature.dashboard.DashboardScreen
import com.powerunit.feature.history.HistoryScreen
import com.powerunit.feature.onboarding.OnboardingScreen
import com.powerunit.navigation.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val leftScreens = listOf(Screen.Home, Screen.Analytics)
private val rightScreens = listOf(Screen.History, Screen.Settings)

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
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (isTopLevelDestination) {
                    PowerUnitBottomNavigationBar(
                        currentRoute = currentRoute,
                        onNavigate = { route -> navigateToTopLevel(navController, route, currentRoute) },
                        onAddReading = { navController.navigate("add_reading") }
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
}

@Composable
private fun PowerUnitNavigationRail(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onAddReading: () -> Unit
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        leftScreens.forEach { screen ->
            RailItem(screen = screen, selected = currentRoute == screen.route, onClick = { onNavigate(screen.route) })
        }

        FloatingActionButton(
            onClick = onAddReading,
            modifier = Modifier
                .padding(vertical = 12.dp)
                .size(48.dp)
                .testTag("rail_add_reading"),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_reading))
        }

        rightScreens.forEach { screen ->
            RailItem(screen = screen, selected = currentRoute == screen.route, onClick = { onNavigate(screen.route) })
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
        label = { Text(stringResource(screen.titleRes)) }
    )
}

@Composable
private fun PowerUnitBottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onAddReading: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 4.dp
    ) {
        leftScreens.forEach { screen ->
            AppNavigationBarItem(
                screen = screen,
                selected = currentRoute == screen.route,
                onNavigate = { onNavigate(screen.route) }
            )
        }

        CenterAddReadingButton(onClick = onAddReading)

        rightScreens.forEach { screen ->
            AppNavigationBarItem(
                screen = screen,
                selected = currentRoute == screen.route,
                onNavigate = { onNavigate(screen.route) }
            )
        }
    }
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
                }
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
                onNavigateToBackup = { navController.navigate("backup") }
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

@Composable
private fun RowScope.AppNavigationBarItem(
    screen: Screen,
    selected: Boolean,
    onNavigate: () -> Unit
) {
    NavigationBarItem(
        modifier = Modifier.testTag("nav_${screen.route}"),
        selected = selected,
        onClick = onNavigate,
        icon = {
            Icon(
                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                contentDescription = stringResource(screen.titleRes)
            )
        },
        label = { Text(stringResource(screen.titleRes)) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun RowScope.CenterAddReadingButton(
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = false,
        onClick = onClick,
        icon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(44.dp)
                    .offset(y = (-4).dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.add_reading),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        label = {
            Text(
                text = stringResource(R.string.add_reading),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = androidx.compose.ui.graphics.Color.Transparent
        ),
        modifier = Modifier.testTag("fab_add_reading")
    )
}

@Composable
fun PlaceholderTabScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

