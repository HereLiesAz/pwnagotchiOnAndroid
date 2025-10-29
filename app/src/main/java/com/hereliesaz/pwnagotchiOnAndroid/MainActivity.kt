package com.hereliesaz.pwnagotchiOnAndroid

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import com.hereliesaz.pwnagotchiOnAndroid.ui.MainScreen
import com.hereliesaz.pwnagotchiOnAndroid.ui.screens.OnboardingScreen
import com.hereliesaz.pwnagotchiOnAndroid.ui.theme.PwnagotchiOnAndroidTheme
import com.hereliesaz.pwnagotchiOnAndroid.LocalAgentManager

class MainActivity : ComponentActivity() {
    private var pwnagotchiService: PwnagotchiService? = null
    private var isBound = false
    private val pwnagotchiViewModel: PwnagotchiViewModel by viewModels()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as PwnagotchiService.LocalBinder
            pwnagotchiService = binder.getService()
            pwnagotchiService?.let {
                pwnagotchiViewModel.setService(it)
            }
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Intent(this, PwnagotchiService::class.java).also { intent ->
            startService(intent)
            bindService(intent, connection, BIND_AUTO_CREATE)
        }

        setContent {
            val sharedPreferences = getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
            var showOnboarding by remember { mutableStateOf(!sharedPreferences.getBoolean("onboarding_complete", false)) }

            val localAgentManager = LocalAgentManager(this)
            var isRooted by remember { mutableStateOf(localAgentManager.isDeviceRooted()) }
            val modeString = sharedPreferences.getString("mode", PwnagotchiMode.REMOTE.name) ?: PwnagotchiMode.REMOTE.name
            val currentMode = PwnagotchiMode.valueOf(modeString)

            if (!isRooted && (currentMode == PwnagotchiMode.LOCAL || currentMode == PwnagotchiMode.HYBRID)) {
                pwnagotchiViewModel.setUiState(PwnagotchiUiState.NotRooted("Standalone and Hybrid modes require root access. Please switch to Remote mode."))
                sharedPreferences.edit { putString("mode", PwnagotchiMode.REMOTE.name) }
                localAgentManager.requestRootAccess { granted ->
                    if (granted) {
                        isRooted = true
                        pwnagotchiViewModel.setUiState(PwnagotchiUiState.Connected())
                    }
                }
            }

            val themeString = sharedPreferences.getString("theme", AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
            var currentTheme by remember { mutableStateOf(AppTheme.valueOf(themeString)) }

            val pwnagotchiUiState by pwnagotchiViewModel.uiState.collectAsState()
            val errorMessage by pwnagotchiViewModel.errorFlow.collectAsState()

            val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
                if (key == "theme") {
                    val newThemeString = prefs.getString("theme", AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
                    currentTheme = AppTheme.valueOf(newThemeString)
                }
            }
            DisposableEffect(Unit) {
                sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
                onDispose {
                    sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }

            PwnagotchiOnAndroidTheme(
                darkTheme = when (currentTheme) {
                    AppTheme.SYSTEM -> isSystemInDarkTheme()
                    AppTheme.LIGHT -> false
                    AppTheme.DARK -> true
                }
            ) {
                if (showOnboarding) {
                    val completeOnboarding = {
                        sharedPreferences.edit { putBoolean("onboarding_complete", true) }
                        showOnboarding = false
                    }
                    OnboardingScreen(
                        onOnboardingComplete = completeOnboarding,
                        onNavigateToSettings = {
                            completeOnboarding()
                            // This will be handled by the NavController in MainScreen
                            // We will navigate to settings after the onboarding is complete
                            pwnagotchiViewModel.navigateToSettings()
                        }
                    )
                } else {
                    MainScreen(
                        pwnagotchiUiState = pwnagotchiUiState,
                        errorMessage = errorMessage,
                        onDisconnect = { pwnagotchiService?.disconnect() },
                        onTogglePlugin = { plugin, enabled -> pwnagotchiService?.togglePlugin(plugin, enabled) },
                        onInstallPlugin = { plugin -> pwnagotchiService?.installCommunityPlugin(plugin) },
                        onReconnect = { pwnagotchiService?.reconnect() },
                        onFetchLeaderboard = { pwnagotchiViewModel.fetchLeaderboard() },
                        onErrorDismiss = { pwnagotchiViewModel.clearError() },
                        navigateToSettings = {
                            pwnagotchiViewModel.onSettingsNavigated()
                        },
                        pwnagotchiViewModel = pwnagotchiViewModel
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }
}
