package com.hereliesaz.pwnagotchiOnAndroid.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.hereliesaz.pwnagotchiOnAndroid.SettingsScreen
import com.hereliesaz.pwnagotchiOnAndroid.core.Constants

@Composable
fun SettingsScreenNav(
    onSave: (String, String, String) -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val host = sharedPreferences.getString("host", Constants.DEFAULT_PWNAGOTCHI_IP) ?: Constants.DEFAULT_PWNAGOTCHI_IP
    val apiKey = sharedPreferences.getString("opwngrid_api_key", "") ?: ""

    SettingsScreen(
        host = host,
        apiKey = apiKey,
        onSave = { newHost, newApiKey, newTheme ->
            sharedPreferences.edit()
                .putString("host", newHost)
                .putString("opwngrid_api_key", newApiKey)
                .putString("theme", newTheme)
                .apply()
            onSave(newHost, newApiKey, newTheme)
        }
    )
}
