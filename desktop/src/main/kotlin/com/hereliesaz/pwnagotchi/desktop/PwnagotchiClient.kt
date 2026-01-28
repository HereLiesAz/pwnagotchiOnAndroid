package com.hereliesaz.pwnagotchi.desktop

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

@Serializable
data class BaseMessage(val type: String)

@Serializable
data class UiUpdateMessage(val data: UiUpdateData)
@Serializable
data class UiUpdateData(val face: String, val channel: String, val aps: Int, val uptime: String, val shakes: Int, val mode: String)

@Serializable
data class PluginListMessage(val data: List<PluginData>)
@Serializable
data class PluginData(val name: String, val enabled: Boolean)

@Serializable
data class CommunityPluginListMessage(val data: List<CommunityPluginData>)
@Serializable
data class CommunityPluginData(val name: String, val description: String)

data class PwnagotchiState(
    val connected: Boolean = false,
    val error: String? = null,
    val face: String = "(O_O)",
    val channel: String = "",
    val aps: Int = 0,
    val uptime: String = "",
    val shakes: Int = 0,
    val mode: String = "MANU",
    val plugins: List<PluginData> = emptyList(),
    val communityPlugins: List<CommunityPluginData> = emptyList()
)

class PwnagotchiClient {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var webSocketClient: WebSocketClient? = null
    private val json = Json { ignoreUnknownKeys = true }

    private val _state = MutableStateFlow(PwnagotchiState())
    val state: StateFlow<PwnagotchiState> = _state

    fun connect(url: String = "ws://localhost:8765") {
        try {
            val uri = URI(url)
            webSocketClient?.close()
            webSocketClient = object : WebSocketClient(uri) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    _state.update { it.copy(connected = true, error = null) }
                    listPlugins()
                    getCommunityPlugins()
                }

                override fun onMessage(message: String?) {
                    message ?: return
                    try {
                        val base = json.decodeFromString<BaseMessage>(message)
                        when (base.type) {
                            "ui_update" -> {
                                val msg = json.decodeFromString<UiUpdateMessage>(message)
                                val d = msg.data
                                _state.update {
                                    it.copy(
                                        face = d.face,
                                        channel = d.channel,
                                        aps = d.aps,
                                        uptime = d.uptime,
                                        shakes = d.shakes,
                                        mode = d.mode
                                    )
                                }
                            }
                            "plugin_list" -> {
                                val msg = json.decodeFromString<PluginListMessage>(message)
                                _state.update { it.copy(plugins = msg.data) }
                            }
                            "community_plugin_list" -> {
                                val msg = json.decodeFromString<CommunityPluginListMessage>(message)
                                _state.update { it.copy(communityPlugins = msg.data) }
                            }
                        }
                    } catch (e: Exception) {
                        println("Error parsing: ${e.message}")
                    }
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    _state.update { it.copy(connected = false, error = "Closed: $reason") }
                    scope.launch {
                        delay(5000)
                        if (!_state.value.connected) reconnect()
                    }
                }

                override fun onError(ex: Exception?) {
                    _state.update { it.copy(connected = false, error = ex?.message) }
                }
            }
            webSocketClient?.connect()
        } catch (e: Exception) {
            _state.update { it.copy(error = e.message) }
        }
    }

    fun listPlugins() {
        send("{\"command\": \"list_plugins\"}")
    }

    fun getCommunityPlugins() {
        send("{\"command\": \"get_community_plugins\"}")
    }

    fun togglePlugin(name: String, enabled: Boolean) {
        send("{\"command\": \"toggle_plugin\", \"plugin_name\": \"$name\", \"enabled\": $enabled}")
    }

    private fun send(msg: String) {
        if (webSocketClient?.isOpen == true) {
            webSocketClient?.send(msg)
        }
    }
}
