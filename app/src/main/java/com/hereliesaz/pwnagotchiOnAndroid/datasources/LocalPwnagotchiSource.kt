package com.hereliesaz.pwnagotchiOnAndroid.datasources

import android.content.Context
import com.hereliesaz.pwnagotchiOnAndroid.LocalAgentManager
import com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiService
import com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URI

class LocalPwnagotchiSource(
    context: Context,
    service: PwnagotchiService
) : BaseWebSocketDataSource(context, service) {

    private val localAgentManager = LocalAgentManager(context)

    companion object {
        private const val DEFAULT_INTERFACE = "wlan0"
        private const val MODE_SWITCH_DELAY_MS = 1000L
        private const val BETTERCAP_STARTUP_DELAY_MS = 5000L
    }

    private val selectedInterface: String
        get() {
            val sharedPreferences = context.getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
            return sharedPreferences.getString("selected_interface", DEFAULT_INTERFACE) ?: DEFAULT_INTERFACE
        }

    override fun connect(uri: URI) {
        currentUri = uri
        serviceScope.launch {
            _uiState.value = PwnagotchiUiState.Connecting("Setting up environment...")
            if (!localAgentManager.extractAssets()) {
                _uiState.value = PwnagotchiUiState.Error("Failed to extract assets")
                return@launch
            }

            _uiState.value = PwnagotchiUiState.Connecting("Checking dependencies...")
            val (hasBettercap, hasBusybox) = localAgentManager.areBinariesInstalled()
            if (!hasBettercap || !hasBusybox) {
                _uiState.value = PwnagotchiUiState.MissingDependencies(hasBettercap, hasBusybox)
                return@launch
            }

            _uiState.value = PwnagotchiUiState.Connecting("Starting Local Agent...")
            if (localAgentManager.enableMonitorMode(selectedInterface)) {
                // Give the system a moment to switch modes
                delay(MODE_SWITCH_DELAY_MS)
                if (localAgentManager.startBettercap(selectedInterface).isSuccess) {
                    _uiState.value = PwnagotchiUiState.Connecting("Starting AI...")
                    localAgentManager.startPythonAi()

                    // Give bettercap a moment to start its web UI
                    // TODO: Replace this with a more robust polling mechanism
                    delay(BETTERCAP_STARTUP_DELAY_MS)
                    webSocketClient = createWebSocketClient(uri)
                    webSocketClient?.connect()
                } else {
                    _uiState.value = PwnagotchiUiState.Error("Failed to start bettercap")
                    localAgentManager.disableMonitorMode(selectedInterface) // Clean up
                }
            } else {
                _uiState.value = PwnagotchiUiState.Error("Failed to enable monitor mode")
            }
        }
    }

    override fun disconnect() {
        super.disconnect()
        serviceScope.launch {
            val sharedPreferences = context.getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
            val selectedInterface = sharedPreferences.getString("selected_interface", "wlan0") ?: "wlan0"
            localAgentManager.stopBettercap()
            localAgentManager.disableMonitorMode(selectedInterface)
            _uiState.value = PwnagotchiUiState.Disconnected("Local Agent stopped")
        }
    }
}
