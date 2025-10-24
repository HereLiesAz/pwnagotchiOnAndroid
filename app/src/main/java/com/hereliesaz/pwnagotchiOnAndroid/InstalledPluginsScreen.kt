package com.hereliesaz.pwnagotchiOnAndroid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun InstalledPluginsScreen(
    plugins: List<Plugin>,
    onTogglePlugin: (String, Boolean) -> Unit
) {
    if (plugins.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No plugins found")
        }
    } else {
        LazyColumn {
            items(plugins) { plugin ->
                PluginItem(plugin = plugin, onToggle = { enabled -> onTogglePlugin(plugin.name, enabled) })
            }
        }
    }
}
