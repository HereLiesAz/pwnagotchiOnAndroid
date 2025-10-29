package com.hereliesaz.pwnagotchiOnAndroid.datasources

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.hereliesaz.pwnagotchiOnAndroid.OpwngridClient
import com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiDataSource
import com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiService
import com.hereliesaz.pwnagotchiOnAndroid.R
import com.hereliesaz.pwnagotchiOnAndroid.Handshake
import com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiUiState
import com.hereliesaz.pwnagotchiOnAndroid.widgets.WidgetStateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import com.hereliesaz.pwnagotchiOnAndroid.BaseMessage
import com.hereliesaz.pwnagotchiOnAndroid.CommunityPlugin
import com.hereliesaz.pwnagotchiOnAndroid.CommunityPluginListMessage
import com.hereliesaz.pwnagotchiOnAndroid.HandshakeMessage
import com.hereliesaz.pwnagotchiOnAndroid.Plugin
import com.hereliesaz.pwnagotchiOnAndroid.PluginListMessage
import com.hereliesaz.pwnagotchiOnAndroid.UiUpdateMessage
import java.net.ConnectException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

abstract class BaseWebSocketDataSource(
    protected val context: Context,
    protected val service: PwnagotchiService
) : PwnagotchiDataSource {

    protected var webSocketClient: WebSocketClient? = null
    protected val _uiState = MutableStateFlow<PwnagotchiUiState>(PwnagotchiUiState.Disconnected(context.getString(R.string.status_not_connected)))
    override val uiState: StateFlow<PwnagotchiUiState> = _uiState

    protected val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    protected var reconnectionJob: Job? = null
    var currentUri: URI? = null
    private val maxReconnectionAttempts = 5
    protected val json = Json { ignoreUnknownKeys = true }
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private var isNetworkAvailable = false
    private lateinit var opwngridClient: OpwngridClient
    private lateinit var widgetStateRepository: WidgetStateRepository

    init {
        opwngridClient = OpwngridClient()
        widgetStateRepository = WidgetStateRepository(context)
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = createNetworkCallback()
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    protected fun createWebSocketClient(uri: URI): WebSocketClient {
        return object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                serviceScope.launch {
                    _uiState.value = PwnagotchiUiState.Connected()
                    service.updateCustomNotification(context.getString(R.string.status_connected_to_pwnagotchi), "Ready to pwn!")
                    listPlugins()
                    getCommunityPlugins()
                }
            }

            override fun onMessage(message: String?) {
                message ?: return
                try {
                    val baseMessage = json.decodeFromString<BaseMessage>(message)
                    when (baseMessage.type) {
                        "ui_update" -> {
                            val uiUpdate = json.decodeFromString<UiUpdateMessage>(message)
                            val data = uiUpdate.data
                            _uiState.update {
                                (it as? PwnagotchiUiState.Connected)?.copy(
                                    face = data.face,
                                    channel = "CH: ${data.channel}",
                                    aps = "APS: ${data.aps}",
                                    uptime = "UP: ${data.uptime}",
                                    shakes = "PWND: ${data.shakes}",
                                    mode = data.mode
                                ) ?: it
                            }
                            val statusText = "CH: ${data.channel} | APS: ${data.aps} | UP: ${data.uptime}"
                            val messageText = "PWND: ${data.shakes} | MODE: ${data.mode}"
                            service.updateCustomNotification(statusText, messageText, data.face)
                            serviceScope.launch {
                                widgetStateRepository.updateFace(data.face)
                                widgetStateRepository.updateMessage(messageText)
                            }
                        }
                        "handshake" -> {
                            val handshakeMsg = json.decodeFromString<HandshakeMessage>(message)
                            val data = handshakeMsg.data
                            val handshake = Handshake(
                                ap = data.ap.hostname,
                                sta = data.sta.mac,
                                filename = data.filename
                            )
                            _uiState.update {
                                (it as? PwnagotchiUiState.Connected)?.copy(
                                    handshakes = it.handshakes + handshake
                                ) ?: it
                            }
                            service.showHandshakeNotification(handshake)
                            serviceScope.launch {
                                (_uiState.value as? PwnagotchiUiState.Connected)?.let {
                                    widgetStateRepository.updateHandshakes(json.encodeToString(it.handshakes))
                                }
                            }
                        }
                        "plugin_list" -> {
                            val pluginListMsg = json.decodeFromString<PluginListMessage>(message)
                            val plugins = pluginListMsg.data.map { Plugin(it.name, it.enabled) }
                            _uiState.update {
                                (it as? PwnagotchiUiState.Connected)?.copy(plugins = plugins) ?: it
                            }
                        }
                        "community_plugin_list" -> {
                            val communityPluginListMsg = json.decodeFromString<CommunityPluginListMessage>(message)
                            val communityPlugins = communityPluginListMsg.data.map { CommunityPlugin(it.name, it.description) }
                            _uiState.update {
                                (it as? PwnagotchiUiState.Connected)?.copy(communityPlugins = communityPlugins) ?: it
                            }
                        }
                    }
                } catch (e: Exception) {
                    _uiState.value = PwnagotchiUiState.Error(context.getString(R.string.error_parsing_message, e.message))
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                val message = "Connection closed: $reason (code: $code)"
                _uiState.value = PwnagotchiUiState.Disconnected(message)
                service.updateCustomNotification(message, "Connection closed", "(⇀‿‿↼)")
                if (isNetworkAvailable) {
                    scheduleReconnect()
                }
            }

            override fun onError(ex: Exception?) {
                val errorMessage = when (ex) {
                    is UnknownHostException -> "Unknown host: ${uri.host}. Please check the address."
                    is ConnectException -> "Connection refused. Is the Pwnagotchi on and the WebSocket server running?"
                    is SSLHandshakeException -> "SSL handshake failed. The server's certificate might be invalid or self-signed."
                    else -> ex?.message ?: context.getString(R.string.error_unknown)
                }
                _uiState.value = PwnagotchiUiState.Error(errorMessage)
                if (isNetworkAvailable) {
                    scheduleReconnect()
                }
            }
        }
    }

    override fun disconnect() {
        currentUri = null
        reconnectionJob?.cancel()
        webSocketClient?.close()
        _uiState.value = PwnagotchiUiState.Disconnected(context.getString(R.string.status_disconnected_by_user))
        service.updateCustomNotification(context.getString(R.string.status_disconnected_by_user), "User disconnected", "( ´•︵•` )")
    }

    override fun reconnect() {
        val sharedPreferences = context.getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
        val host = sharedPreferences.getString("host", null)
        if (host != null) {
            connect(URI("wss://$host:8765"))
        }
    }

    override fun fetchLeaderboard() {
        serviceScope.launch {
            try {
                val leaderboard = opwngridClient.getLeaderboard().mapIndexed { index, (name, pwned) -> com.hereliesaz.pwnagotchiOnAndroid.LeaderboardEntry(name, pwned, index + 1) }
                _uiState.update {
                    (it as? PwnagotchiUiState.Connected)?.copy(leaderboard = leaderboard) ?: it
                }
                widgetStateRepository.updateLeaderboard(json.encodeToString(leaderboard))
            } catch (e: Exception) {
                _uiState.value = PwnagotchiUiState.Error("Failed to fetch leaderboard: ${e.message}")
            }
        }
    }

    private fun isWebSocketOpen(): Boolean {
        return webSocketClient?.isOpen == true
    }

    override fun listPlugins() {
        if (isWebSocketOpen()) {
            webSocketClient?.send("{\"command\": \"list_plugins\"}")
        } else {
            _uiState.value = PwnagotchiUiState.Error(context.getString(R.string.error_websocket_not_open))
        }
    }

    override fun togglePlugin(pluginName: String, enabled: Boolean) {
        if (isWebSocketOpen()) {
            webSocketClient?.send("{\"command\": \"toggle_plugin\", \"plugin_name\": \"$pluginName\", \"enabled\": $enabled}")
        } else {
            _uiState.value = PwnagotchiUiState.Error(context.getString(R.string.error_websocket_not_open))
        }
    }

    override fun getCommunityPlugins() {
        if (isWebSocketOpen()) {
            webSocketClient?.send("{\"command\": \"get_community_plugins\"}")
        } else {
            _uiState.value = PwnagotchiUiState.Error(context.getString(R.string.error_websocket_not_open))
        }
    }

    override fun installCommunityPlugin(pluginName: String) {
        if (isWebSocketOpen()) {
            webSocketClient?.send("{\"command\": \"install_community_plugin\", \"plugin_name\": \"$pluginName\"}")
        } else {
            _uiState.value = PwnagotchiUiState.Error(context.getString(R.string.error_websocket_not_open))
        }
    }

    private fun scheduleReconnect() {
        if (reconnectionJob?.isActive == true) return
        var attempts = 0
        reconnectionJob = serviceScope.launch {
            var delayMs = 1000L
            val maxDelayMs = 60000L
            while (attempts < maxReconnectionAttempts) {
                _uiState.value = PwnagotchiUiState.Connecting(context.getString(R.string.status_reconnection_attempt, attempts + 1, maxReconnectionAttempts))
                delay(delayMs)
                currentUri?.let {
                    webSocketClient?.reconnect()
                }
                delayMs = (delayMs * 2).coerceAtMost(maxDelayMs)
                attempts++
            }
            _uiState.value = PwnagotchiUiState.Error(context.getString(R.string.error_failed_to_reconnect, maxReconnectionAttempts))
        }
    }

    private fun createNetworkCallback(): ConnectivityManager.NetworkCallback {
        return object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isNetworkAvailable = true
                scheduleReconnect()
            }

            override fun onLost(network: Network) {
                isNetworkAvailable = false
                reconnectionJob?.cancel()
                _uiState.value = PwnagotchiUiState.Disconnected(context.getString(R.string.status_network_lost))
                service.updateCustomNotification(context.getString(R.string.notification_network_lost), "Network lost", "(´•(oo)•`)")
            }
        }
    }
}
