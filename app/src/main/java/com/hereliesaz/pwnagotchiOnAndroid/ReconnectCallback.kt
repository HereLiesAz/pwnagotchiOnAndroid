package com.hereliesaz.pwnagotchiOnAndroid

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

class ReconnectCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(context, PwnagotchiService::class.java).apply {
            action = "com.hereliesaz.pwnagotchiOnAndroid.RECONNECT"
        }
        context.startService(intent)
    }
}
