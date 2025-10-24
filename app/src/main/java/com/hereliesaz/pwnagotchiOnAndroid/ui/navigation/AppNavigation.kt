package com.hereliesaz.pwnagotchiOnAndroid.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.hereliesaz.pwnagotchiOnAndroid.R

sealed class Screen(val route: String, @param:StringRes val title: Int, val icon: ImageVector) {
    object Home : Screen("home", R.string.home, Icons.Default.Home)
    object Settings : Screen("settings", R.string.settings, Icons.Default.Settings)
    object Plugins : Screen("plugins", R.string.plugins, Icons.AutoMirrored.Filled.List)
    object Opwngrid : Screen("opwngrid", R.string.opwngrid, Icons.Default.Star)
}