package com.hereliesaz.pwnagotchiOnAndroid.datasources

import android.content.Context
import com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiService
import java.net.URI

class RemotePwnagotchiSource(context: Context, service: PwnagotchiService) :
    BaseWebSocketDataSource(context, service) {

    override fun connect(uri: URI) {
        currentUri = uri
        reconnectionJob?.cancel()
        webSocketClient?.close()

        _uiState.value = com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiUiState.Connecting(context.getString(com.hereliesaz.pwnagotchiOnAndroid.R.string.status_connecting, uri.toString()))
        webSocketClient = createWebSocketClient(uri)
        webSocketClient?.connect()
    }
}
