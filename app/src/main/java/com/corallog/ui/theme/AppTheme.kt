package com.corallog.ui.theme

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.corallog.R

/**
 * Enumeration of available application themes.
 * Centralizes theme identification, localized titles, and specific color palettes.
 */
enum class AppTheme(
    val id: String,
    @StringRes val titleRes: Int,
    val phaseColors: PhaseColors,
    val primaryColor: Color
) {
    CORAL(
        id = "CORAL",
        titleRes = R.string.theme_coral,
        phaseColors = PhaseColors(
            menstrual = PhaseMenstrualCoral,
            folicular = PhaseFolicularCoral,
            ovulacion = PhaseOvulacionCoral,
            lutea = PhaseLuteaCoral
        ),
        primaryColor = PhaseMenstrualCoral
    ),
    OCEANO(
        id = "OCEANO",
        titleRes = R.string.theme_ocean,
        phaseColors = PhaseColors(
            menstrual = MenstrualOcean,
            folicular = FolicularOcean,
            ovulacion = OvulacionOcean,
            lutea = LuteaOcean
        ),
        primaryColor = MenstrualOcean
    ),
    BOSQUE(
        id = "BOSQUE",
        titleRes = R.string.theme_forest,
        phaseColors = PhaseColors(
            menstrual = MenstrualForest,
            folicular = FolicularForest,
            ovulacion = OvulacionForest,
            lutea = LuteaForest
        ),
        primaryColor = MenstrualForest
    )
}
