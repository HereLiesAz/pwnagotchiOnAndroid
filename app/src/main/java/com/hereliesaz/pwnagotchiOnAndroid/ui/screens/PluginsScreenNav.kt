package com.hereliesaz.pwnagotchiOnAndroid.ui.screens

import androidx.compose.runtime.Composable
import com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiUiState
import com.hereliesaz.pwnagotchiOnAndroid.PluginsScreen

@Composable
fun PluginsScreenNav(
    pwnagotchiUiState: PwnagotchiUiState,
    onTogglePlugin: (String, Boolean) -> Unit,
    onInstallPlugin: (String) -> Unit
) {
    if (pwnagotchiUiState is PwnagotchiUiState.Connected) {
        PluginsScreen(
            pwnagotchiUiState.plugins,
            pwnagotchiUiState.communityPlugins,
            onTogglePlugin,
            onInstallPlugin
        )
    }
}