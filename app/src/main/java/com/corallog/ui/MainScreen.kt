package com.corallog.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.corallog.R
import com.corallog.feature.calendar.CalendarScreen
import com.corallog.feature.metrics.MetricsScreen
import com.corallog.ui.navigation.Screen
import com.corallog.ui.theme.ManropeFontFamily
import com.corallog.ui.theme.OnPrimaryContainer
import com.corallog.ui.theme.OnSurfaceVariant
import com.corallog.ui.theme.Primary
import com.corallog.ui.theme.PrimaryContainer
import com.corallog.ui.theme.SurfaceContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceContainer,
                tonalElevation = 8.dp
            ) {
                Screen.items.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, stringResource(screen.labelRes), modifier = Modifier.size(24.dp)) },
                        label = { 
                            Text(
                                text = stringResource(screen.labelRes), 
                                fontFamily = ManropeFontFamily, 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 11.sp
                            ) 
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OnPrimaryContainer,
                            selectedTextColor = Primary,
                            indicatorColor = PrimaryContainer,
                            unselectedIconColor = OnSurfaceVariant,
                            unselectedTextColor = OnSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Calendar.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                // Placeholder
                Text(stringResource(R.string.nav_home), modifier = Modifier.padding(16.dp))
            }
            composable(Screen.Calendar.route) {
                CalendarScreen()
            }
            composable(Screen.Metrics.route) {
                MetricsScreen(
                    onNavigateToCalendar = {
                        navController.navigate(Screen.Calendar.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.Settings.route) {
                // TODO: Implement Settings Screen (Epic 4)
                Text(stringResource(R.string.nav_settings), modifier = Modifier.padding(16.dp))
            }
        }
    }
}
