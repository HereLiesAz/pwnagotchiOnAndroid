package com.hereliesaz.pwnagotchiOnAndroid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hereliesaz.pwnagotchiOnAndroid.CommunityPlugin
import com.hereliesaz.pwnagotchiOnAndroid.Plugin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PwnagotchiViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<PwnagotchiUiState>(PwnagotchiUiState.Disconnected("Not connected"))
    val uiState: StateFlow<PwnagotchiUiState> = _uiState

    private val _errorFlow = MutableStateFlow<String?>(null)
    val errorFlow: StateFlow<String?> = _errorFlow

    private var pwnagotchiService: PwnagotchiService? = null

    val plugins: StateFlow<List<Plugin>> = uiState.map {
        (it as? PwnagotchiUiState.Connected)?.plugins ?: emptyList()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val communityPlugins: StateFlow<List<CommunityPlugin>> = uiState.map {
        (it as? PwnagotchiUiState.Connected)?.communityPlugins ?: emptyList()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )


    fun setService(service: PwnagotchiService?) {
        pwnagotchiService = service
        viewModelScope.launch {
            service?.uiState?.collect {
                _uiState.value = it
                if (it is PwnagotchiUiState.Error) {
                    _errorFlow.value = it.message
                }
            }
        }
    }

    fun fetchLeaderboard() {
        pwnagotchiService?.fetchLeaderboard()
    }

    private val _navigateToSettings = MutableStateFlow(false)
    val navigateToSettings: StateFlow<Boolean> = _navigateToSettings

    fun clearError() {
        _errorFlow.value = null
    }

    fun setUiState(state: PwnagotchiUiState) {
        _uiState.value = state
    }

    fun navigateToSettings() {
        _navigateToSettings.value = true
    }

    fun onSettingsNavigated() {
        _navigateToSettings.value = false
    }
}
