package com.hereliesaz.pwnagotchi.desktop.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hereliesaz.pwnagotchi.desktop.ProcessManager
import com.hereliesaz.pwnagotchi.desktop.PwnagotchiClient

@Composable
fun SettingsScreen(client: PwnagotchiClient, processManager: ProcessManager) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        Button(onClick = { processManager.start() }) {
            Text("Start Pwnagotchi Service")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { processManager.stop() }) {
            Text("Stop Pwnagotchi Service")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { processManager.install() }) {
            Text("Re-install / Update Resources")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { client.connect() }) {
            Text("Reconnect WebSocket")
        }

        Spacer(Modifier.height(16.dp))
        Text("Note: 'bettercap' must be installed on your system.")
    }
}
