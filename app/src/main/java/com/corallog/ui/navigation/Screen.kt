package com.corallog.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.corallog.R

/**
 * Defines the navigation structure and routes for the Coral Log application.
 */
sealed class Screen(val route: String, val labelRes: Int, val icon: ImageVector) {
    object Home : Screen("home", R.string.nav_home, Icons.Default.Home)
    object Calendar : Screen("calendar", R.string.nav_calendar, Icons.Default.CalendarMonth)
    object Metrics : Screen("metrics", R.string.nav_metrics, Icons.Default.BarChart)
    object Settings : Screen("settings", R.string.nav_settings, Icons.Default.Settings)

    companion object {
        val items = listOf(Home, Calendar, Metrics, Settings)
    }
}
