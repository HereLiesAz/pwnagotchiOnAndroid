package com.hereliesaz.pwnagotchiOnAndroid.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hereliesaz.pwnagotchiOnAndroid.AppTheme
import com.hereliesaz.pwnagotchiOnAndroid.SettingsViewModel
import com.hereliesaz.pwnagotchiOnAndroid.SettingsViewModelFactory

import com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiMode

@Composable
fun SettingsScreenNav(
) {
    val context = LocalContext.current
    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(context))
    val uiState by settingsViewModel.uiState.collectAsState()

    SettingsScreen(uiState) { host, apiKey, city, mode, theme, selectedInterface ->
        settingsViewModel.saveSettings(context, host, apiKey, city, mode, theme, selectedInterface)
    }
}
