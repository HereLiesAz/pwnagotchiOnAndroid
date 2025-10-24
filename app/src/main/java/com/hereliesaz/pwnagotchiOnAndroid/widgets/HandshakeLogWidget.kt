package com.hereliesaz.pwnagotchiOnAndroid.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.text.Text
import androidx.glance.GlanceId
import com.hereliesaz.pwnagotchiOnAndroid.Handshake
import kotlinx.serialization.json.Json

class HandshakeLogWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val repository = WidgetStateRepository(context)
            val handshakesJson by repository.handshakes.collectAsState(initial = "[]")
            val handshakes = try {
                Json.decodeFromString<List<Handshake>>(handshakesJson)
            } catch (e: Exception) {
                emptyList()
            }

            Content(handshakes = handshakes)
        }
    }

    @Composable
    private fun Content(handshakes: List<Handshake>) {
        LazyColumn {
            items(handshakes.size) { index ->
                val handshake = handshakes[index]
                Text(text = "${handshake.ap} - ${handshake.sta}")
            }
        }
    }
}
