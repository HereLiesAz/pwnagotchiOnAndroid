package com.hereliesaz.pwnagotchiOnAndroid.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MissingDependenciesScreen(
    hasBettercap: Boolean,
    hasBusybox: Boolean,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Missing Dependencies",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (!hasBusybox) {
            DependencyInstallationGuide(
                title = "Busybox Not Found",
                instructions = """
                    This app requires Busybox to be installed on your rooted device.
                    The recommended way to install it is through a Magisk module.

                    1. Open the Magisk Manager app.
                    2. Go to the 'Modules' section.
                    3. Search for 'Busybox' and install the one by 'osm0sis'.
                    4. Reboot your device.
                """.trimIndent()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (!hasBettercap) {
            DependencyInstallationGuide(
                title = "Bettercap Not Found",
                instructions = """
                    This app requires bettercap to be installed on your rooted device.

                    1. Install Termux from F-Droid.
                    2. Open Termux and run the following commands:

                    apt update
                    termux-setup-storage
                    pkg install root-repo
                    pkg install golang git libpcap libusb
                    pkg install pkg-config
                    pkg install tsu
                    go install github.com/bettercap/bettercap@latest && cd ${'$'}HOME/go/bin
                    sudo ./bettercap

                    3. Grant Termux superuser rights when prompted.
                """.trimIndent()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun DependencyInstallationGuide(title: String, instructions: String) {
    Column {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = instructions,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
    }
}
