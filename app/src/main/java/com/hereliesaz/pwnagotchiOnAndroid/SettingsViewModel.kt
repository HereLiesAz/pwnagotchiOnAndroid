package com.hereliesaz.pwnagotchiOnAndroid

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(context: Context, private val localAgentManager: LocalAgentManager = LocalAgentManager(context)) : ViewModel() {
    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        loadSettings(context)
    }

    private fun loadSettings(context: Context) {
        viewModelScope.launch {
            val sharedPreferences = context.getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
            val host = sharedPreferences.getString("host", "") ?: ""
            val apiKey = sharedPreferences.getString("api_key", "") ?: ""
            val city = sharedPreferences.getString("city", "") ?: ""
            val isRooted = localAgentManager.isDeviceRooted()
            val hasNexmon = localAgentManager.hasNexmon()

            val modeString = sharedPreferences.getString("mode", PwnagotchiMode.REMOTE.name) ?: PwnagotchiMode.REMOTE.name
            val mode = PwnagotchiMode.valueOf(modeString)
            val themeString = sharedPreferences.getString("theme", AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
            val theme = AppTheme.valueOf(themeString)
            val wirelessInterfaces = localAgentManager.getWirelessInterfaces()
            val selectedInterface = sharedPreferences.getString("selected_interface", "") ?: ""


            _uiState.value = SettingsUiState.Loaded(
                isRooted = isRooted,
                hasNexmon = hasNexmon,
                host = host,
                apiKey = apiKey,
                city = city,
                mode = mode,
                theme = theme,
                wirelessInterfaces = wirelessInterfaces,
                selectedInterface = selectedInterface
            )
        }
    }

    fun saveSettings(context: Context, host: String, apiKey: String, city: String, mode: PwnagotchiMode, theme: AppTheme, selectedInterface: String) {
        viewModelScope.launch {
            val sharedPreferences = context.getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString("host", host)
                putString("api_key", apiKey)
                putString("city", city)
                putString("mode", mode.name)
                putString("theme", theme.name)
                putString("selected_interface", selectedInterface)
                apply()
            }
        }
    }
}
