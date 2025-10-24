package com.hereliesaz.pwnagotchiOnAndroid

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Binder
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.hereliesaz.pwnagotchiOnAndroid.datasources.HybridPwnagotchiSource
import com.hereliesaz.pwnagotchiOnAndroid.datasources.LocalPwnagotchiSource
import com.hereliesaz.pwnagotchiOnAndroid.datasources.RemotePwnagotchiSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import java.net.URI

class PwnagotchiService : Service() {

    private val binder = LocalBinder()
    private lateinit var dataSource: PwnagotchiDataSource
    val uiState: StateFlow<PwnagotchiUiState> by lazy { dataSource.uiState }

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var currentMode = PwnagotchiMode.REMOTE // Default to remote

    inner class LocalBinder : Binder() {
        fun getService(): PwnagotchiService = this@PwnagotchiService
    }

    override fun onCreate() {
        super.onCreate()
        val sharedPreferences = getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
        val modeString = sharedPreferences.getString("mode", PwnagotchiMode.REMOTE.name) ?: PwnagotchiMode.REMOTE.name
        currentMode = PwnagotchiMode.valueOf(modeString)
        dataSource = createDataSource()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "com.hereliesaz.pwnagotchiOnAndroid.RECONNECT" -> {
                reconnect()
            }
            else -> {
                val notification = createCustomNotification(getString(R.string.status_connecting), "Waiting for Pwnagotchi...")
                startForeground(1, notification)
                val sharedPreferences = getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
                val host = sharedPreferences.getString("host", null)
                if (host != null) {
                    when (currentMode) {
                        PwnagotchiMode.REMOTE, PwnagotchiMode.HYBRID -> connect(URI("wss://$host:8765"))
                        PwnagotchiMode.LOCAL -> connect(URI("ws://127.0.0.1:8080/api/events"))
                    }
                }
            }
        }
        return START_STICKY
    }

    fun connect(uri: URI) {
        dataSource.connect(uri)
    }

    fun disconnect() {
        dataSource.disconnect()
    }

    fun reconnect() {
        dataSource.reconnect()
    }

    fun fetchLeaderboard() {
        dataSource.fetchLeaderboard()
    }

    fun listPlugins() {
        dataSource.listPlugins()
    }

    fun togglePlugin(pluginName: String, enabled: Boolean) {
        dataSource.togglePlugin(pluginName, enabled)
    }

    fun getCommunityPlugins() {
        dataSource.getCommunityPlugins()
    }

    fun installCommunityPlugin(pluginName: String) {
        dataSource.installCommunityPlugin(pluginName)
    }

    fun switchMode(mode: PwnagotchiMode) {
        if (currentMode != mode) {
            dataSource.disconnect()
            currentMode = mode
            dataSource = createDataSource()
        }
    }

    private fun createDataSource(): PwnagotchiDataSource {
        return when (currentMode) {
            PwnagotchiMode.REMOTE -> RemotePwnagotchiSource(this, this)
            PwnagotchiMode.LOCAL -> LocalPwnagotchiSource(this, this)
            PwnagotchiMode.HYBRID -> HybridPwnagotchiSource(this, this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        dataSource.disconnect()
    }

    private fun getFaceDrawable(face: String): Int {
        return when (face) {
            "happy" -> R.drawable.pwnagotchi_happy
            "sad" -> R.drawable.pwnagotchi_sad
            else -> R.drawable.pwnagotchi_neutral
        }
    }

    private fun createCustomNotification(status: String, message: String, face: String = "neutral"): Notification {
        val remoteViews = RemoteViews(packageName, R.layout.notification_pwnagotchi).apply {
            setImageViewResource(R.id.notification_face, getFaceDrawable(face))
            setTextViewText(R.id.notification_status, status)
            setTextViewText(R.id.notification_message, message)
        }

        val channelId = "pwnagotchi_service_channel"
        val channelName = "Pwnagotchi Service Channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }


        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setCustomContentView(remoteViews)
            .setOngoing(true)
            .build()
    }

    fun updateCustomNotification(status: String, message: String, face: String = "neutral") {
        val notification = createCustomNotification(status, message, face)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, notification)
    }

    fun showHandshakeNotification(handshake: Handshake) {
        val contentText = getString(R.string.notification_handshake_captured, handshake.ap)
        val notification = NotificationHelper.createNotification(this, "handshake_channel", "Handshake Notifications", contentText)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(2, notification)
    }
}
