package com.hereliesaz.pwnagotchiOnAndroid.ui.screens

import androidx.compose.runtime.Composable
import com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiUiState

@Composable
fun HomeScreen(
    pwnagotchiUiState: PwnagotchiUiState,
    onReconnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    when (pwnagotchiUiState) {
        is PwnagotchiUiState.Connecting -> ConnectingScreen(pwnagotchiUiState.message)
        is PwnagotchiUiState.Connected -> ConnectedScreen(
            uiState = pwnagotchiUiState,
            onDisconnect = onDisconnect
        )
        is PwnagotchiUiState.Disconnected -> DisconnectedScreen(pwnagotchiUiState.message, onReconnect)
        is PwnagotchiUiState.Error -> ErrorScreen(pwnagotchiUiState.message, onReconnect)
    }
}
