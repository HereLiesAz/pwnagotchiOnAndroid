package com.hereliesaz.pwnagotchiOnAndroid.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hereliesaz.pwnagotchiOnAndroid.AppTheme
import com.hereliesaz.pwnagotchiOnAndroid.SettingsUiState

import com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiMode
import androidx.compose.material3.RadioButton
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onSaveSettings: (String, String, String, PwnagotchiMode, AppTheme, String) -> Unit
) {
    val context = LocalContext.current
    when (uiState) {
        is SettingsUiState.Loading -> {
            CircularProgressIndicator()
        }
        is SettingsUiState.Loaded -> {
            var host by remember { mutableStateOf(uiState.host) }
            var apiKey by remember { mutableStateOf(uiState.apiKey) }
            var city by remember { mutableStateOf(uiState.city) }
            var mode by remember { mutableStateOf(uiState.mode) }
            var theme by remember { mutableStateOf(uiState.theme) }
            var selectedInterface by remember { mutableStateOf(uiState.selectedInterface) }
            var expanded by remember { mutableStateOf(false) }


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Theme", style = MaterialTheme.typography.headlineSmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = theme == AppTheme.SYSTEM,
                                onClick = { theme = AppTheme.SYSTEM }
                            )
                            Text("System")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = theme == AppTheme.LIGHT,
                                onClick = { theme = AppTheme.LIGHT }
                            )
                            Text("Light")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = theme == AppTheme.DARK,
                                onClick = { theme = AppTheme.DARK }
                            )
                            Text("Dark")
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Mode", style = MaterialTheme.typography.headlineSmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = mode == PwnagotchiMode.REMOTE,
                                onClick = { mode = PwnagotchiMode.REMOTE }
                            )
                            Text("Remote")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = mode == PwnagotchiMode.LOCAL,
                                onClick = { mode = PwnagotchiMode.LOCAL }
                            )
                            Text("Local")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = mode == PwnagotchiMode.HYBRID,
                                onClick = { mode = PwnagotchiMode.HYBRID }
                            )
                            Text("Hybrid")
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Remote Client Settings", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = host,
                            onValueChange = { host = it },
                            label = { Text("Pwnagotchi Host") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            label = { Text("oPwngrid API Key") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City for oPwngrid") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Standalone Mode", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        StatusRow("Root Access", uiState.isRooted)
                        StatusRow("Nexmon Found", uiState.hasNexmon)
                        if (!uiState.hasNexmon) {
                            TextButton(onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/seemoo-lab/nexmon"))
                                context.startActivity(intent)
                            }) {
                                Text("Learn how to install Nexmon")
                            }
                        }

                        if(uiState.wirelessInterfaces.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Wireless Interface:")
                                Spacer(modifier = Modifier.weight(1f))
                                Box {
                                    Button(onClick = { expanded = true }) {
                                        Text(selectedInterface.ifEmpty { "Select Interface" })
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        uiState.wirelessInterfaces.forEach { iface ->
                                            DropdownMenuItem(
                                                text = { Text(iface) },
                                                onClick = {
                                                    selectedInterface = iface
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                                Button(onClick = { expanded = true }) {
                                    Text(selectedInterface.ifEmpty { "Select Interface" })
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = { onSaveSettings(host, apiKey, city, mode, theme, selectedInterface) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
fun StatusRow(label: String, isSuccess: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isSuccess) Color.Green else Color.Red,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
