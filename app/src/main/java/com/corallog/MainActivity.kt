package com.corallog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.corallog.ui.MainScreen
import com.corallog.ui.theme.CoralLogTheme

/**
 * Main entry point of the Coral Log application.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CoralLogTheme {
                MainScreen()
            }
        }
    }
}
