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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.corallog.data.UserPreferencesRepository
import com.corallog.feature.calendar.CalendarScreen
import com.corallog.feature.settings.SettingsScreen
import com.corallog.ui.theme.*
import org.koin.android.ext.android.inject

/**
 * Main entry point of the Coral Log application.
 */
class MainActivity : ComponentActivity() {

    private val prefsRepository: UserPreferencesRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentTheme by prefsRepository.userThemeFlow
                .collectAsStateWithLifecycle(initialValue = "CORAL")

            CoralLogTheme(themeName = currentTheme) {
                var selectedTab by remember { mutableStateOf("Calendario") }

                Scaffold(
                    bottomBar = {
                        NavigationBar(containerColor = SurfaceContainer, tonalElevation = 8.dp) {
                            val navItems = listOf(
                                Triple("Inicio", Icons.Default.Home, "Inicio"),
                                Triple("Calendario", Icons.Default.CalendarMonth, "Calendario"),
                                Triple("Resumen", Icons.Default.BarChart, "Resumen"),
                                Triple("Ajustes", Icons.Default.Settings, "Ajustes")
                            )

                            navItems.forEach { (label, icon, route) ->
                                NavigationBarItem(
                                    selected = selectedTab == route,
                                    onClick = { selectedTab = route },
                                    icon = { Icon(icon, label) },
                                    label = { Text(label, fontSize = 11.sp) }
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
                            "Ajustes" -> SettingsScreen()
                            else -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                                    Text("Próximamente: $selectedTab")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
