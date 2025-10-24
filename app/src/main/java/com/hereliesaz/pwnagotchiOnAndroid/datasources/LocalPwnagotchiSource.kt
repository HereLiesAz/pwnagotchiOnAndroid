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

    override fun connect(uri: URI) {
        currentUri = uri
        serviceScope.launch {
            _uiState.value = PwnagotchiUiState.Connecting("Starting Local Agent...")
            if (localAgentManager.enableMonitorMode()) {
                // Give the system a moment to switch modes
                delay(1000)
                if (localAgentManager.startBettercap().isSuccess) {
                    // Give bettercap a moment to start its web UI
                    delay(5000)
                    webSocketClient = createWebSocketClient(uri)
                    webSocketClient?.connect()
                } else {
                    _uiState.value = PwnagotchiUiState.Error("Failed to start bettercap")
                    localAgentManager.disableMonitorMode() // Clean up
                }
            } else {
                _uiState.value = PwnagotchiUiState.Error("Failed to enable monitor mode")
            }
        }
    }

    override fun disconnect() {
        super.disconnect()
        serviceScope.launch {
            localAgentManager.stopBettercap()
            localAgentManager.disableMonitorMode()
            _uiState.value = PwnagotchiUiState.Disconnected("Local Agent stopped")
        }
    }
}
