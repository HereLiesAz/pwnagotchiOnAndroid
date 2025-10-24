package com.hereliesaz.pwnagotchiOnAndroid.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import com.hereliesaz.pwnagotchiOnAndroid.ReconnectCallback

class QuickActionsWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Content()
        }
    }

    @Composable
    private fun Content() {
        Button(
            text = "Reconnect",
            onClick = actionRunCallback<ReconnectCallback>()
        )
    }
}
