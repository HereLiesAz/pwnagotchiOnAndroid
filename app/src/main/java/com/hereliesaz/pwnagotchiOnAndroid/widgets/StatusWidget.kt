package com.hereliesaz.pwnagotchiOnAndroid.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.layout.padding
import androidx.glance.text.Text
import com.hereliesaz.pwnagotchiOnAndroid.R

class StatusWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val repository = WidgetStateRepository(context)
            val face by repository.face.collectAsState(initial = "(·•᷄_•᷅ ·)")
            val message by repository.message.collectAsState(initial = "Awaiting updates...")

            Column(modifier = GlanceModifier.padding(16.dp)) {
                val faceResId = when (face) {
                    "(·•᷄_•᷅ ·)" -> R.drawable.pwnagotchi_sad
                    "(·•ᴗ• ·)" -> R.drawable.pwnagotchi_happy
                    else -> R.drawable.pwnagotchi_neutral
                }
                Image(
                    provider = ImageProvider(faceResId),
                    contentDescription = "Pwnagotchi Face",
                    modifier = GlanceModifier.padding(bottom = 8.dp)
                )
                Text(text = "Status: $message")
            }
        }
    }
}
