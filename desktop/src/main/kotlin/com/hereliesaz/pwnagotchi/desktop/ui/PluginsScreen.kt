package com.hereliesaz.pwnagotchi.desktop.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hereliesaz.pwnagotchi.desktop.PwnagotchiClient

@Composable
fun PluginsScreen(client: PwnagotchiClient) {
    val state by client.state.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Plugins", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        LazyColumn {
            items(state.plugins) { plugin ->
                Row(
                    Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(plugin.name)
                    Switch(
                        checked = plugin.enabled,
                        onCheckedChange = { client.togglePlugin(plugin.name, it) }
                    )
                }
                HorizontalDivider()
            }
        }
    }
}
