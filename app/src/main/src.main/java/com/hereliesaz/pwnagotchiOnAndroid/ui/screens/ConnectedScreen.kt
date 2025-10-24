package com.hereliesaz.pwnagotchiOnAndroid.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiUiState
import com.hereliesaz.pwnagotchiOnAndroid.R
import com.hereliesaz.pwnagotchiOnAndroid.model.Handshake

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectedScreen(
    uiState: PwnagotchiUiState.Connected,
    onDisconnect: () -> Unit,
    onNavigateToPlugins: () -> Unit,
    onNavigateToOpwngrid: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onDisconnect) {
                        Icon(painterResource(id = R.drawable.ic_baseline_cloud_off_24), contentDescription = stringResource(R.string.disconnect))
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Pwnagotchi Status Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val face = when (uiState.face) {
                        "(·•᷄_•᷅ ·)" -> R.drawable.pwnagotchi_sad
                        "(·•ᴗ• ·)" -> R.drawable.pwnagotchi_happy
                        else -> R.drawable.pwnagotchi_neutral
                    }
                    Crossfade(targetState = face) { faceResId ->
                        Image(
                            painter = painterResource(id = faceResId),
                            contentDescription = stringResource(id = R.string.pwnagotchi_face),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = uiState.status,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Handshakes Card
            Card(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = stringResource(R.string.captured_handshakes), style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (uiState.handshakes.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.no_handshakes_captured_yet))
                        }
                    } else {
                        LazyColumn {
                            items(uiState.handshakes) { handshake ->
                                HandshakeItem(handshake)
                                Divider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HandshakeItem(handshake: Handshake) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = handshake.ssid)
        Text(text = handshake.bssid)
    }
}
