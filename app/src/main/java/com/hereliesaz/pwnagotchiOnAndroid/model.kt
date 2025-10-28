package com.hereliesaz.pwnagotchiOnAndroid

import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import java.net.URI

enum class PwnagotchiMode {
    REMOTE,
    LOCAL,
    HYBRID
}

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}

interface PwnagotchiDataSource {
    val uiState: StateFlow<PwnagotchiUiState>
    fun connect(uri: URI)
    fun disconnect()
    fun reconnect()
    fun listPlugins()
    fun togglePlugin(pluginName: String, enabled: Boolean)
    fun getCommunityPlugins()
    fun installCommunityPlugin(pluginName: String)
    fun fetchLeaderboard()
}


@Serializable
data class Handshake(val ap: String, val sta: String, val filename: String)

@Serializable
data class Plugin(val name: String, val enabled: Boolean)

@Serializable
data class CommunityPlugin(val name: String, val description: String)

@Serializable
data class LeaderboardEntry(val name: String, val handshakes: Int, val rank: Int)

sealed class PwnagotchiUiState {
    data class Disconnected(val message: String) : PwnagotchiUiState()
    data class Connecting(val message: String) : PwnagotchiUiState()
    data class Connected(
        val face: String = "(·•᷄_•᷅ ·)",
        val channel: String = "CH: -",
        val aps: String = "APS: 0",
        val uptime: String = "UP: 00:00:00",
        val shakes: String = "PWND: 0",
        val mode: String = "MANU",
        val handshakes: List<Handshake> = emptyList(),
        val plugins: List<Plugin> = emptyList(),
        val leaderboard: List<LeaderboardEntry> = emptyList(),
        val communityPlugins: List<CommunityPlugin> = emptyList()
    ) : PwnagotchiUiState()
    data class Error(val message: String) : PwnagotchiUiState()
    data class MissingDependencies(val hasBettercap: Boolean, val hasBusybox: Boolean) : PwnagotchiUiState()
}

@Serializable
data class BaseMessage(val type: String)

@Serializable
data class UiUpdateMessage(val type: String, val data: UiUpdateData)

@Serializable
data class UiUpdateData(val face: String, val channel: String, val aps: String, val uptime: String, val shakes: String, val mode: String)

@Serializable
data class HandshakeMessage(val type: String, val data: HandshakeData)

@Serializable
data class HandshakeData(val ap: ApData, val sta: StaData, val filename: String)

@Serializable
data class ApData(val hostname: String)

@Serializable
data class StaData(val mac: String)

@Serializable
data class PluginData(val name: String, val enabled: Boolean)

@Serializable
data class PluginListMessage(val type: String, val data: List<PluginData>)

@Serializable
data class CommunityPluginData(val name: String, val description: String)

@Serializable
data class CommunityPluginListMessage(val type: String, val data: List<CommunityPluginData>)

sealed class SettingsUiState {
    object Loading : SettingsUiState()
    data class Loaded(
        val isRooted: Boolean,
        val hasNexmon: Boolean,
        val host: String,
        val apiKey: String,
        val city: String,
        val mode: PwnagotchiMode,
        val theme: AppTheme,
        val wirelessInterfaces: List<String> = emptyList(),
        val selectedInterface: String = ""
    ) : SettingsUiState()
}
