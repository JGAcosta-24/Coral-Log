package com.corallog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.corallog.data.UserPreferencesRepository
import com.corallog.ui.MainScreen
import com.corallog.ui.theme.CoralLogTheme
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
            val currentTheme = prefsRepository.userThemeFlow.collectAsStateWithLifecycle(initialValue = "CORAL")
            val currentFont = prefsRepository.userFontFlow.collectAsStateWithLifecycle(initialValue = "ROBOTO")

            CoralLogTheme(themeName = currentTheme.value, fontName = currentFont.value) {
                MainScreen()
            }
        }
    }
}
