package com.hereliesaz.pwnagotchiOnAndroid.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiUiState
import com.hereliesaz.pwnagotchiOnAndroid.R

@Composable
fun ConnectedScreen(
    uiState: PwnagotchiUiState.Connected,
    onDisconnect: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
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
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text(uiState.channel, style = MaterialTheme.typography.bodyLarge)
                        Text(uiState.aps, style = MaterialTheme.typography.bodyLarge)
                        Text(uiState.uptime, style = MaterialTheme.typography.bodyLarge)
                    }
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text(uiState.shakes, style = MaterialTheme.typography.bodyLarge)
                        Text("MODE: ${uiState.mode}", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDisconnect) {
                    Text("Disconnect")
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(id = R.string.captured_handshakes),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                LazyColumn {
                    items(uiState.handshakes, key = { it.filename }) { handshake ->
                        ListItem(
                            headlineContent = { Text(handshake.ap) },
                            supportingContent = { Text(handshake.sta) }
                        )
                    }
                }
            }
        }
    }
}
