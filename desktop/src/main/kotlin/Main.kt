package com.hereliesaz.pwnagotchi.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.hereliesaz.pwnagotchi.desktop.ui.MainScreen

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Pwnagotchi Desktop") {
        MainScreen()
    }
}
