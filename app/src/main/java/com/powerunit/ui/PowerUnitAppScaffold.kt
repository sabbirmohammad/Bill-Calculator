package com.powerunit.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.powerunit.feature.history.HistoryScreen
import com.powerunit.feature.onboarding.OnboardingScreen
import com.powerunit.navigation.Screen
import kotlinx.coroutines.launch

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
    val showFab = currentRoute == Screen.Home.route || currentRoute == Screen.History.route

    Row(modifier = Modifier.fillMaxSize()) {
        if (isTablet && isTopLevelDestination) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Screen.bottomNavScreens.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationRailItem(
                        modifier = Modifier.testTag("nav_${screen.route}"),
                        selected = selected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = stringResource(screen.titleRes)
                            )
                        },
                        label = { Text(stringResource(screen.titleRes)) }
                    )
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = showFab,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                ) {
                    ExtendedFloatingActionButton(
                        modifier = Modifier.testTag("fab_add_reading"),
                        onClick = {
                            navController.navigate("add_reading")
                        },
                        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                        text = { Text(stringResource(R.string.add_reading)) },
                        shape = ButtonShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            bottomBar = {
                if (!isTablet && isTopLevelDestination) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        tonalElevation = 4.dp
                    ) {
                        Screen.bottomNavScreens.forEach { screen ->
                            val selected = currentRoute == screen.route
                            NavigationBarItem(
                                modifier = Modifier.testTag("nav_${screen.route}"),
                                selected = selected,
                                onClick = {
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
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
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
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
                        PlaceholderTabScreen(title = stringResource(Screen.Home.titleRes))
                    }
                    composable(Screen.Analytics.route) {
                        PlaceholderTabScreen(title = stringResource(Screen.Analytics.titleRes))
                    }
                    composable(Screen.History.route) {
                        HistoryScreen(
                            onNavigateToAddReading = { navController.navigate("add_reading") },
                            onNavigateToEditReading = { id -> navController.navigate("edit_reading/$id") }
                        )
                    }
                    composable(Screen.Settings.route) {
                        PlaceholderTabScreen(title = stringResource(Screen.Settings.titleRes))
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
        }
    }
}

@Composable
fun PlaceholderTabScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
