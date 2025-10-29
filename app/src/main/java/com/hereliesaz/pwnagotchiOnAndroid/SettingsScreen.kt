package com.hereliesaz.pwnagotchiOnAndroid

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val uiState by settingsViewModel.uiState.collectAsState()

    when (val state = uiState) {
        is SettingsUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is SettingsUiState.Loaded -> {
            var hostState by remember { mutableStateOf(state.host) }
            var apiKeyState by remember { mutableStateOf(state.apiKey) }
            var cityState by remember { mutableStateOf(state.city) }
            var modeState by remember { mutableStateOf(state.mode) }
            var themeState by remember { mutableStateOf(state.theme) }
            var selectedInterfaceState by remember { mutableStateOf(state.selectedInterface) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Remote Host Settings
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Remote Host Settings", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = hostState,
                    onValueChange = { hostState = it },
                    label = { Text(stringResource(id = R.string.websocket_host)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = apiKeyState,
                    onValueChange = { apiKeyState = it },
                    label = { Text(stringResource(id = R.string.opwngrid_api_key)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Device Status Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Device Status (for Standalone Mode)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Rooted:")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (state.isRooted) "Yes" else "No")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Nexmon Detected:")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (state.hasNexmon) "Yes" else "No")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Theme Settings
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(id = R.string.theme), style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = themeState == AppTheme.SYSTEM, onClick = { themeState = AppTheme.SYSTEM })
                    Text(stringResource(id = R.string.system))
                    RadioButton(selected = themeState == AppTheme.LIGHT, onClick = { themeState = AppTheme.LIGHT })
                    Text(stringResource(id = R.string.light))
                    RadioButton(selected = themeState == AppTheme.DARK, onClick = { themeState = AppTheme.DARK })
                    Text(stringResource(id = R.string.dark))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            settingsViewModel.saveSettings(context, hostState, apiKeyState, cityState, modeState, themeState, selectedInterfaceState)
        }) {
            Text(stringResource(id = R.string.save))
        }
    }
    }
    }
}
