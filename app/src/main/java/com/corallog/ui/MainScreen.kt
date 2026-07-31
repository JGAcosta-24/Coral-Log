package com.corallog.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.corallog.R
import com.corallog.data.UserPreferencesRepository
import com.corallog.feature.calendar.CalendarScreen
import com.corallog.feature.home.HomeScreen
import com.corallog.feature.metrics.MetricsScreen
import com.corallog.feature.onboarding.OnboardingScreen
import com.corallog.feature.settings.SettingsScreen
import com.corallog.ui.navigation.Screen
import com.corallog.ui.theme.ManropeFontFamily
import com.corallog.ui.theme.OnPrimaryContainer
import com.corallog.ui.theme.OnSurfaceVariant
import com.corallog.ui.theme.Primary
import com.corallog.ui.theme.PrimaryContainer
import com.corallog.ui.theme.SurfaceContainer
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val prefsRepository: UserPreferencesRepository = koinInject()
    val onboardingCompleted by prefsRepository.isOnboardingCompletedFlow
        .collectAsStateWithLifecycle(initialValue = null)

    if (onboardingCompleted == null) {
        // Initializing DataStore...
        Box(modifier = Modifier.fillMaxSize())
    } else if (!onboardingCompleted!!) {
        OnboardingScreen(
            onFinished = { /* Flow will update automatically */ }
        )
    } else {
        val pagerState = rememberPagerState(initialPage = 0) { Screen.items.size }
        val coroutineScope = rememberCoroutineScope()

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = SurfaceContainer,
                    tonalElevation = 8.dp
                ) {
                    Screen.items.forEachIndexed { index, screen ->
                        val isSelected = pagerState.currentPage == index
                        
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
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
            HorizontalPadding(innerPadding) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = true,
                    beyondViewportPageCount = 1
                ) { pageIndex ->
                    when (Screen.items[pageIndex]) {
                        Screen.Home -> HomeScreen()
                        Screen.Calendar -> CalendarScreen()
                        Screen.Metrics -> {
                            MetricsScreen(
                                onNavigateToCalendar = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(Screen.items.indexOf(Screen.Calendar))
                                    }
                                }
                            )
                        }
                        Screen.Settings -> SettingsScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun HorizontalPadding(padding: androidx.compose.foundation.layout.PaddingValues, content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(padding)) {
        content()
    }
}
