package com.hereliesaz.pwnagotchiOnAndroid

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.hereliesaz.pwnagotchiOnAndroid.R

object NotificationHelper {
    fun createNotification(
        context: Context,
        channelId: String,
        channelName: String,
        contentText: String,
        importance: Int = NotificationManager.IMPORTANCE_DEFAULT
    ): Notification {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                importance
            )
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(context, channelId)
            .setContentTitle("Pwnagotchi")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }
}
