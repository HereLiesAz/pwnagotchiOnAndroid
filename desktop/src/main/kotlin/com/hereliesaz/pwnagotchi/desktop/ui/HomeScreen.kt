package com.hereliesaz.pwnagotchi.desktop.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hereliesaz.pwnagotchi.desktop.ProcessManager
import com.hereliesaz.pwnagotchi.desktop.PwnagotchiClient

@Composable
fun HomeScreen(client: PwnagotchiClient, processManager: ProcessManager) {
    val state by client.state.collectAsState()
    val logs by processManager.logs.collectAsState()
    val status by processManager.status.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Face
        Text(
            text = state.face,
            fontSize = 64.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(Modifier.height(16.dp))

        // Stats
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Status: ${if (state.connected) "Connected" else "Disconnected (${state.error ?: "Unknown"})"}")
                Text("Process: $status")
                Spacer(Modifier.height(8.dp))
                if (state.connected) {
                    Text("Mode: ${state.mode}")
                    Text("Channel: ${state.channel}")
                    Text("APS: ${state.aps}")
                    Text("PWND: ${state.shakes}")
                    Text("Uptime: ${state.uptime}")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Logs
        Text("Logs:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
        Card(Modifier.fillMaxWidth().weight(1f)) {
            val scrollState = rememberScrollState()
            Text(
                text = logs,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .verticalScroll(scrollState),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            // Auto-scroll
            LaunchedEffect(logs) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }
    }
}
