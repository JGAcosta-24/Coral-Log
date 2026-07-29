package com.corallog.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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
    val lutea: Color
)

/**
 * CompositionLocal to provide PhaseColors globally.
 */
val LocalPhaseColors = staticCompositionLocalOf<PhaseColors> { 
    error("No PhaseColors provided") 
}

/**
 * Palette for the "Coral" theme.
 */
private val CoralPhaseColors = PhaseColors(
    menstrual = PhaseMenstrualCoral,
    folicular = PhaseFolicularCoral,
    ovulacion = PhaseOvulacionCoral,
    lutea = PhaseLuteaCoral
)

/**
 * Palette for the "Océano" theme.
 */
private val OceanPhaseColors = PhaseColors(
    menstrual = MenstrualOcean,
    folicular = FolicularOcean,
    ovulacion = OvulacionOcean,
    lutea = LuteaOcean
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
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
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    // Selection of phase colors based on theme name
    val selectedPhaseColors = when (themeName) {
        "OCEANO" -> OceanPhaseColors
        else -> CoralPhaseColors
    }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    CompositionLocalProvider(LocalPhaseColors provides selectedPhaseColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = getTypographyForFont(fontName),
            content = content
        )
    }
}
