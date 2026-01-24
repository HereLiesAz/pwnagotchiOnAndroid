package com.hereliesaz.pwnagotchi.desktop.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import com.hereliesaz.pwnagotchi.desktop.DiscoveryManager
import com.hereliesaz.pwnagotchi.desktop.ProcessManager
import com.hereliesaz.pwnagotchi.desktop.PwnagotchiClient
import io.github.g0dkar.qrcode.QRCode

@Composable
fun SettingsScreen(client: PwnagotchiClient, processManager: ProcessManager) {
    val localIp = remember { DiscoveryManager.getLocalIpAddress()?.hostAddress ?: "Unknown" }
    var qrCodeBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(localIp) {
        if (localIp != "Unknown") {
            // Android app expects URI format
            // Assuming Android app can parse this or just the IP
            val content = "ws://$localIp:8765"
            val javaImage = QRCode(content).render().nativeImage() as java.awt.image.BufferedImage
            qrCodeBitmap = javaImage.toComposeImageBitmap()
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = processManager.targetInterface,
            onValueChange = { processManager.targetInterface = it },
            label = { Text("Wireless Interface") },
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))

        Text("Service Control", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row {
            Button(onClick = { processManager.start() }) { Text("Start") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { processManager.stop() }) { Text("Stop") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { client.connect() }) { Text("Reconnect UI") }
        }

        Spacer(Modifier.height(24.dp))

        Text("Remote Pairing", style = MaterialTheme.typography.titleMedium)
        Text("Connect your Android app using the scanner.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))

        Text("Local IP: $localIp")

        qrCodeBitmap?.let {
            Spacer(Modifier.height(8.dp))
            Image(bitmap = it, contentDescription = "QR Code", modifier = Modifier.size(200.dp))
        }

        Spacer(Modifier.height(24.dp))
        Button(onClick = { processManager.install() }) {
            Text("Re-install Resources")
        }
    }
}
