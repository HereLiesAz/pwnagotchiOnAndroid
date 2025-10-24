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
    host: String,
    apiKey: String,
    onSave: (String, String, String) -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val savedTheme = sharedPreferences.getString("theme", "System") ?: "System"
    val localAgentManager = remember { LocalAgentManager(context) }

    var hostState by remember { mutableStateOf(host) }
    var apiKeyState by remember { mutableStateOf(apiKey) }
    var theme by remember { mutableStateOf(savedTheme) }
    val isRooted by remember { mutableStateOf(localAgentManager.isDeviceRooted()) }
    val hasNexmon by remember { mutableStateOf(localAgentManager.hasNexmon()) }

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
                    Text(if (isRooted) "Yes" else "No")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Nexmon Detected:")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (hasNexmon) "Yes" else "No")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Theme Settings
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(id = R.string.theme), style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = theme == "System", onClick = { theme = "System" })
                    Text(stringResource(id = R.string.system))
                    RadioButton(selected = theme == "Light", onClick = { theme = "Light" })
                    Text(stringResource(id = R.string.light))
                    RadioButton(selected = theme == "Dark", onClick = { theme = "Dark" })
                    Text(stringResource(id = R.string.dark))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    // TODO: Implement custom theme loading
                }) {
                    Text(stringResource(id = R.string.select_custom_theme))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            sharedPreferences.edit().putString("theme", theme).apply()
            onSave(hostState, apiKeyState, theme)
        }) {
            Text(stringResource(id = R.string.save))
        }
    }
}
