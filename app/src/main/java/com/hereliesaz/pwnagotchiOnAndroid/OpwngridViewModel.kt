package com.hereliesaz.pwnagotchiOnAndroid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class OpwngridUiState {
    object Loading : OpwngridUiState()
    data class Success(val leaderboard: List<LeaderboardEntry>) : OpwngridUiState()
    data class Error(val message: String) : OpwngridUiState()
}

class OpwngridViewModel(private val opwngridClient: OpwngridClient) : ViewModel() {

    private val _uiState = MutableStateFlow<OpwngridUiState>(OpwngridUiState.Loading)
    val uiState: StateFlow<OpwngridUiState> = _uiState

    init {
        fetchLeaderboard()
    }

    fun fetchLeaderboard() {
        viewModelScope.launch {
            _uiState.value = OpwngridUiState.Loading
            try {
                val leaderboard = opwngridClient.getLeaderboard().mapIndexed { index, (name, pwned) -> LeaderboardEntry(name, pwned, index + 1) }
                _uiState.value = OpwngridUiState.Success(leaderboard)
            } catch (e: Exception) {
                _uiState.value = OpwngridUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
