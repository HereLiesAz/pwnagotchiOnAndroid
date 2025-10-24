package com.hereliesaz.pwnagotchiOnAndroid.widgets

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "widget_state")

class WidgetStateRepository(private val context: Context) {

    private val faceKey = stringPreferencesKey("face")
    private val messageKey = stringPreferencesKey("message")
    private val handshakesKey = stringPreferencesKey("handshakes")
    private val leaderboardKey = stringPreferencesKey("leaderboard")

    val face: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[faceKey] ?: "(·•᷄_•᷅ ·)"
    }

    val message: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[messageKey] ?: "Not connected"
    }

    val handshakes: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[handshakesKey] ?: "[]"
    }

    val leaderboard: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[leaderboardKey] ?: "[]"
    }

    suspend fun updateFace(face: String) {
        context.dataStore.edit { preferences ->
            preferences[faceKey] = face
        }
    }

    suspend fun updateMessage(message: String) {
        context.dataStore.edit { preferences ->
            preferences[messageKey] = message
        }
    }

    suspend fun updateHandshakes(handshakes: String) {
        context.dataStore.edit { preferences ->
            preferences[handshakesKey] = handshakes
        }
    }

    suspend fun updateLeaderboard(leaderboard: String) {
        context.dataStore.edit { preferences ->
            preferences[leaderboardKey] = leaderboard
        }
    }
}