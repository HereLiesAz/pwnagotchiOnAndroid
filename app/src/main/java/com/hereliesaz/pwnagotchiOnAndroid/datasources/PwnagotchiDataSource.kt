package com.hereliesaz.pwnagotchiOnAndroid.datasources

import com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiUiState
import kotlinx.coroutines.flow.StateFlow

interface PwnagotchiDataSource {
    val uiState: StateFlow<PwnagotchiUiState>
    suspend fun start(params: DataSourceParams? = null)
    suspend fun stop()
    suspend fun sendCommand(command: String)
}

data class DataSourceParams(val host: String? = null)
