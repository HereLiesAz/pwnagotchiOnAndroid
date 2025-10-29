package com.hereliesaz.pwnagotchiOnAndroid.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiViewModel
import com.hereliesaz.pwnagotchiOnAndroid.PluginsScreen

@Composable
fun PluginsScreenNav(
    pwnagotchiViewModel: PwnagotchiViewModel,
    onTogglePlugin: (String, Boolean) -> Unit,
    onInstallPlugin: (String) -> Unit
) {
    val plugins by pwnagotchiViewModel.plugins.collectAsState()
    val communityPlugins by pwnagotchiViewModel.communityPlugins.collectAsState()

    PluginsScreen(
        plugins = plugins,
        communityPlugins = communityPlugins,
        onTogglePlugin = onTogglePlugin,
        onInstallPlugin = onInstallPlugin
    )
}