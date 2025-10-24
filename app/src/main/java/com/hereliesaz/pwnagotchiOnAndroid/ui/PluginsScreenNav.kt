package com.hereliesaz.pwnagotchiOnAndroid.ui

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
            plugins = pwnagotchiUiState.plugins,
            communityPlugins = pwnagotchiUiState.communityPlugins,
            onTogglePlugin = onTogglePlugin,
            onInstallPlugin = onInstallPlugin
        )
    }
}
