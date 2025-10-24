package com.hereliesaz.pwnagotchiOnAndroid.datasources

import android.content.Context
import com.hereliesaz.pwnagotchiOnAndroid.LocalAgentManager
import com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiService
import kotlinx.coroutines.launch
import java.net.URI

class HybridPwnagotchiSource(context: Context, service: PwnagotchiService) :
    BaseWebSocketDataSource(context, service) {

    private val localAgentManager = LocalAgentManager(context)

    override fun connect(uri: URI) {
        currentUri = uri
        reconnectionJob?.cancel()
        webSocketClient?.close()

        _uiState.value = com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiUiState.Connecting("Configuring USB network...")
        serviceScope.launch {
            val result = localAgentManager.configureUsbNetwork()
            if (result.isSuccess) {
                _uiState.value = com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiUiState.Connecting(context.getString(com.hereliesaz.pwnagotchiOnAndroid.R.string.status_connecting, uri.toString()))
                webSocketClient = createWebSocketClient(uri)
                webSocketClient?.connect()
            } else {
                _uiState.value = com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiUiState.Error("Failed to configure USB network: ${result.err}")
            }
        }
    }
}
