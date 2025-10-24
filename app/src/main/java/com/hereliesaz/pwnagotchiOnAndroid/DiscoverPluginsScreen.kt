package com.hereliesaz.pwnagotchiOnAndroid

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DiscoverPluginsScreen(
    plugins: List<CommunityPlugin>,
    onInstallPlugin: (String) -> Unit
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
                CommunityPluginItem(plugin = plugin, onInstall = { onInstallPlugin(plugin.name) })
            }
        }
    }
}

@Composable
fun CommunityPluginItem(
    plugin: CommunityPlugin,
    onInstall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = plugin.name, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            Text(text = plugin.description, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
            Button(onClick = onInstall) {
                Text("Install")
            }
        }
    }
}
