package com.hereliesaz.pwnagotchiOnAndroid.datasources

import android.content.Context
import com.hereliesaz.pwnagotchiOnAndroid.LocalAgentManager
import com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiDataSource
import com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiUiState
import com.hereliesaz.pwnagotchiOnAndroid.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class LocalPwnagotchiSource(private val context: Context) : PwnagotchiDataSource {
    override val uiState: StateFlow<PwnagotchiUiState>
        get() = _uiState

    private val _uiState = MutableStateFlow<PwnagotchiUiState>(PwnagotchiUiState.Disconnected("Local mode not started"))
    private val localAgentManager = LocalAgentManager(context)
    private var webSocketClient: WebSocketClient? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())


    override fun connect(uri: URI) {
        serviceScope.launch {
            _uiState.value = PwnagotchiUiState.Connecting("Starting local agent...")
            if (localAgentManager.enableMonitorMode()) {
                _uiState.value = PwnagotchiUiState.Connecting("Monitor mode enabled. Starting bettercap...")
                localAgentManager.startBettercap()
                // TODO: Wait for bettercap to start
                webSocketClient = object : WebSocketClient(URI("ws://127.0.0.1:8765")) {
                    override fun onOpen(handshakedata: ServerHandshake?) {
                        _uiState.value = PwnagotchiUiState.Connected()
                    }

                    override fun onMessage(message: String?) {
                        // TODO: Handle messages from bettercap
                    }

                    override fun onClose(code: Int, reason: String?, remote: Boolean) {
                        _uiState.value = PwnagotchiUiState.Disconnected("Bettercap exited")
                    }

                    override fun onError(ex: Exception?) {
                        _uiState.value = PwnagotchiUiState.Error(ex?.message ?: "Unknown error")
                    }
                }
                webSocketClient?.connect()
            } else {
                _uiState.value = PwnagotchiUiState.Error("Failed to enable monitor mode")
            }
        }
    }

    override fun disconnect() {
        webSocketClient?.close()
        localAgentManager.stopBettercap()
        localAgentManager.disableMonitorMode()
        _uiState.value = PwnagotchiUiState.Disconnected("Local agent stopped")
    }

    override fun reconnect() {
        // Not applicable for local mode
    }

    override fun listPlugins() {
        // TODO: Implement local mode plugin listing
    }

    override fun togglePlugin(pluginName: String, enabled: Boolean) {
        // TODO: Implement local mode plugin toggling
    }

    override fun getCommunityPlugins() {
        // TODO: Implement local mode community plugin listing
    }

    override fun installCommunityPlugin(pluginName: String) {
        // TODO: Implement local mode community plugin installation
    }

    override fun fetchLeaderboard() {
        // Not applicable for local mode
    }
}
