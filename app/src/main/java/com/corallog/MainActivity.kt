package com.corallog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.corallog.data.UserPreferencesRepository
import com.corallog.feature.calendar.CalendarScreen
import com.corallog.feature.metrics.MetricsScreen
import com.corallog.feature.settings.SettingsScreen
import com.corallog.ui.theme.*
import org.koin.android.ext.android.inject

/**
 * Main entry point of the Coral Log application.
 */
class MainActivity : ComponentActivity() {

    private val prefsRepository: UserPreferencesRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the Splash Screen before super.onCreate()
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentTheme by prefsRepository.userThemeFlow
                .collectAsStateWithLifecycle(initialValue = "CORAL")
            val currentFont by prefsRepository.userFontFlow
                .collectAsStateWithLifecycle(initialValue = "ROBOTO")

            CoralLogTheme(themeName = currentTheme, fontName = currentFont) {
                var selectedTab by remember { mutableStateOf("Calendario") }

                Scaffold(
                    bottomBar = {
                        // NavigationBar now inherits colors correctly from MaterialTheme.colorScheme
                        NavigationBar(containerColor = SurfaceContainer, tonalElevation = 8.dp) {
                            val navItems = listOf(
                                Triple(stringResource(R.string.nav_home), Icons.Default.Home, "Inicio"),
                                Triple(stringResource(R.string.nav_calendar), Icons.Default.CalendarMonth, "Calendario"),
                                Triple(stringResource(R.string.nav_metrics), Icons.Default.BarChart, "Resumen"),
                                Triple(stringResource(R.string.nav_settings), Icons.Default.Settings, "Ajustes")
                            )

                            navItems.forEach { (label, icon, route) ->
                                NavigationBarItem(
                                    selected = selectedTab == route,
                                    onClick = { selectedTab = route },
                                    icon = { Icon(icon, label) },
                                    label = { 
                                        Text(
                                            text = label, 
                                            style = MaterialTheme.typography.labelSmall 
                                        ) 
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (selectedTab) {
                            "Calendario" -> CalendarScreen()
                            "Resumen" -> MetricsScreen(
                                onNavigateToCalendar = { selectedTab = "Calendario" }
                            )
                            "Ajustes" -> SettingsScreen()
                            else -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                                    Text(
                                        text = "Próximamente: $selectedTab",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
