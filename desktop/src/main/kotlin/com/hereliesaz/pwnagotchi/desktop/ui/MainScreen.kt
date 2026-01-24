package com.hereliesaz.pwnagotchi.desktop.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Settings
import com.hereliesaz.pwnagotchi.desktop.DiscoveryManager
import com.hereliesaz.pwnagotchi.desktop.ProcessManager
import com.hereliesaz.pwnagotchi.desktop.PwnagotchiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class Screen { Home, Plugins, Settings }

@Composable
fun MainScreen() {
    val processManager = remember { ProcessManager }
    val pwnagotchiClient = remember { PwnagotchiClient() }

    // Auto-connect and ensure installation
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            if (!processManager.isInstalled()) {
                 processManager.install()
            }
            processManager.start()
            DiscoveryManager.start()
            pwnagotchiClient.connect()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            DiscoveryManager.stop()
            processManager.stop()
        }
    }

    var currentScreen by remember { mutableStateOf(Screen.Home) }

    MaterialTheme {
        Scaffold { padding ->
            Row(Modifier.fillMaxSize()) {
                NavigationRail {
                    NavigationRailItem(
                        selected = currentScreen == Screen.Home,
                        onClick = { currentScreen = Screen.Home },
                        icon = { Icon(Icons.Default.Home, "Home") },
                        label = { Text("Home") }
                    )
                    NavigationRailItem(
                        selected = currentScreen == Screen.Plugins,
                        onClick = { currentScreen = Screen.Plugins },
                        icon = { Icon(Icons.Default.Extension, "Plugins") },
                        label = { Text("Plugins") }
                    )
                    NavigationRailItem(
                        selected = currentScreen == Screen.Settings,
                        onClick = { currentScreen = Screen.Settings },
                        icon = { Icon(Icons.Default.Settings, "Settings") },
                        label = { Text("Settings") }
                    )
                }

                Surface(Modifier.fillMaxSize()) {
                    when(currentScreen) {
                        Screen.Home -> HomeScreen(pwnagotchiClient, processManager)
                        Screen.Plugins -> PluginsScreen(pwnagotchiClient)
                        Screen.Settings -> SettingsScreen(pwnagotchiClient, processManager)
                    }
                }
            }
        }
    }
}
