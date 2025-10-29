package com.hereliesaz.pwnagotchiOnAndroid.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiUiState
import androidx.compose.runtime.collectAsState
import com.hereliesaz.pwnagotchiOnAndroid.SettingsViewModel
import com.hereliesaz.pwnagotchiOnAndroid.ui.navigation.Screen
import com.hereliesaz.pwnagotchiOnAndroid.ui.screens.HomeScreen
import com.hereliesaz.pwnagotchiOnAndroid.ui.screens.OpwngridScreenNav
import com.hereliesaz.pwnagotchiOnAndroid.ui.screens.PluginsScreenNav
import com.hereliesaz.pwnagotchiOnAndroid.ui.screens.MissingDependenciesScreen
import com.hereliesaz.pwnagotchiOnAndroid.ui.screens.NotRootedScreen
import com.hereliesaz.aznavrail.AzNavRail
import com.hereliesaz.aznavrail.model.AzButtonShape
import com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiViewModel
import com.hereliesaz.pwnagotchiOnAndroid.ui.screens.SettingsScreen

@Composable
fun MainScreen(
    pwnagotchiUiState: PwnagotchiUiState,
    errorMessage: String?,
    onDisconnect: () -> Unit,
    onTogglePlugin: (String, Boolean) -> Unit,
    onInstallPlugin: (String) -> Unit,
    onReconnect: () -> Unit,
    onFetchLeaderboard: () -> Unit,
    onErrorDismiss: () -> Unit,
    navigateToSettings: () -> Unit,
    pwnagotchiViewModel: PwnagotchiViewModel,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Home,
        Screen.Plugins,
        Screen.Opwngrid,
        Screen.Settings
    )
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorDismiss()
        }
    }

    val navigateToSettingsState by pwnagotchiViewModel.navigateToSettings.collectAsState()

    LaunchedEffect(navigateToSettingsState) {
        if (navigateToSettingsState) {
            navController.navigate(Screen.Settings.route)
            navigateToSettings()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Row(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            val selectedColor = MaterialTheme.colorScheme.primary
            val unselectedColor = Color.Transparent
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            AzNavRail {
                azSettings(
                    displayAppNameInHeader = false,
                    packRailButtons = false,
                    isLoading = false,
                    defaultShape = AzButtonShape.RECTANGLE
                )
                items.forEach { screen ->
                    azRailItem(
                        id = screen.route,
                        text = screen.route,
                        color = if (currentRoute == screen.route) selectedColor else unselectedColor,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
            AppNavHost(
                navController = navController,
                pwnagotchiUiState = pwnagotchiUiState,
                onDisconnect = onDisconnect,
                onTogglePlugin = onTogglePlugin,
                onInstallPlugin = onInstallPlugin,
                onReconnect = onReconnect,
                onNavigateToPlugins = { navController.navigate(Screen.Plugins.route) },
                onNavigateToOpwngrid = { navController.navigate(Screen.Opwngrid.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onFetchLeaderboard = onFetchLeaderboard,
                pwnagotchiViewModel = pwnagotchiViewModel,
                settingsViewModel = settingsViewModel
            )
        }
    }
}


@Composable
fun AppNavHost(
    navController: NavController,
    pwnagotchiUiState: PwnagotchiUiState,
    onDisconnect: () -> Unit,
    onTogglePlugin: (String, Boolean) -> Unit,
    onInstallPlugin: (String) -> Unit,
    onReconnect: () -> Unit,
    onNavigateToPlugins: () -> Unit,
    onNavigateToOpwngrid: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onFetchLeaderboard: () -> Unit,
    pwnagotchiViewModel: PwnagotchiViewModel,
    settingsViewModel: SettingsViewModel
) {
    NavHost(
        navController = navController as androidx.navigation.NavHostController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            when (pwnagotchiUiState) {
                is PwnagotchiUiState.MissingDependencies ->
                    MissingDependenciesScreen(
                        hasBettercap = pwnagotchiUiState.hasBettercap,
                        hasBusybox = pwnagotchiUiState.hasBusybox,
                        onRetry = onReconnect
                    )
                is PwnagotchiUiState.NotRooted ->
                    NotRootedScreen(
                        message = pwnagotchiUiState.message,
                        onSwitchToRemote = onReconnect
                    )
                else ->
                    HomeScreen(
                        pwnagotchiUiState = pwnagotchiUiState,
                        onReconnect = onReconnect,
                        onDisconnect = onDisconnect
                    )
            }
        }
        composable(Screen.Plugins.route) {
            PluginsScreenNav(pwnagotchiViewModel, onTogglePlugin, onInstallPlugin)
        }
        composable(Screen.Opwngrid.route) {
            OpwngridScreenNav(pwnagotchiUiState, onFetchLeaderboard)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(settingsViewModel = settingsViewModel)
        }
    }
}
