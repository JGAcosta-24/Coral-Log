package com.corallog.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Data class to hold menstrual phase colors.
 */
data class PhaseColors(
    val menstrual: Color,
    val folicular: Color,
    val ovulacion: Color,
    val lutea: Color,
)

/**
 * CompositionLocal to provide PhaseColors globally.
 */
val LocalPhaseColors = staticCompositionLocalOf<PhaseColors> { 
    error("No PhaseColors provided") 
}

/**
 * Base light color scheme for the application.
 */
private val BaseLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant
)

/**
 * Base dark color scheme for the application.
 * Component colors are overridden dynamically in [CoralLogTheme].
 */
private val BaseDarkColorScheme = darkColorScheme(
    onPrimary = Color.White,
    secondary = Secondary,
    onSecondary = OnSecondary,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant
)

@Composable
fun CoralLogTheme(
    themeName: String = "CORAL",
    fontName: String = "ROBOTO",
    themeSelection: String = "SYSTEM",
    content: @Composable () -> Unit
) {
    // 1. Determine dark mode based on selection
    val darkTheme = when (themeSelection) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    // 2. Lookup the current theme configuration from AppTheme Enum
    val currentThemeConfig = AppTheme.entries.find { it.id == themeName } ?: AppTheme.CORAL

    // 2. Select base scheme
    val baseScheme = if (darkTheme) BaseDarkColorScheme else BaseLightColorScheme

    // 3. FORCED SCHEME: Overriding critical Material 3 slots to ensure 
    // the primary theme color (Green, Blue, or Coral) flows everywhere.
    val customColorScheme = if (darkTheme) {
        baseScheme.copy(
            primary = currentThemeConfig.primaryColor,
            primaryContainer = currentThemeConfig.primaryColor,
            onPrimaryContainer = Color.White,
            secondaryContainer = currentThemeConfig.primaryColor.copy(alpha = 0.25f),
            onSecondaryContainer = currentThemeConfig.primaryColor
        )
    } else {
        baseScheme.copy(
            primary = currentThemeConfig.primaryColor,
            primaryContainer = currentThemeConfig.primaryColor.copy(alpha = 0.12f),
            onPrimaryContainer = currentThemeConfig.primaryColor,
            secondaryContainer = currentThemeConfig.primaryColor.copy(alpha = 0.08f),
            onSecondaryContainer = currentThemeConfig.primaryColor
        )
    }
    
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = customColorScheme.surface.toArgb()
            window.navigationBarColor = customColorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalPhaseColors provides currentThemeConfig.phaseColors) {
        MaterialTheme(
            colorScheme = customColorScheme,
            typography = getTypographyForFont(fontName),
            content = content
        )
    }
}
