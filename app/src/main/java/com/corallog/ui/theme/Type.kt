package com.corallog.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.corallog.R

// Local Font Families (Offline-first)
val RobotoFont = FontFamily(Font(R.font.roboto_variablefont_wdth_wght))
val MerriweatherFont = FontFamily(Font(R.font.merriweather_variablefont_opsz_wdth_wght))
val NunitoFont = FontFamily(Font(R.font.nunito_variablefont_wght))

/**
 * Returns a Typography object configured with the specified font family across ALL styles.
 * 
 * @param fontName The identifier of the font ("ROBOTO", "MERRIWEATHER", "NUNITO").
 */
fun getTypographyForFont(fontName: String): Typography {
    val selectedFamily = when (fontName) {
        "MERRIWEATHER" -> MerriweatherFont
        "NUNITO" -> NunitoFont
        else -> RobotoFont
    }

    return Typography(
        displayLarge = TextStyle(fontFamily = selectedFamily, fontWeight = FontWeight.Normal, fontSize = 57.sp),
        displayMedium = TextStyle(fontFamily = selectedFamily, fontWeight = FontWeight.Normal, fontSize = 45.sp),
        displaySmall = TextStyle(fontFamily = selectedFamily, fontWeight = FontWeight.Normal, fontSize = 36.sp),
        headlineLarge = TextStyle(fontFamily = selectedFamily, fontWeight = FontWeight.Bold, fontSize = 32.sp),
        headlineMedium = TextStyle(fontFamily = selectedFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp),
        headlineSmall = TextStyle(fontFamily = selectedFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp),
        titleLarge = TextStyle(fontFamily = selectedFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp),
        titleMedium = TextStyle(fontFamily = selectedFamily, fontWeight = FontWeight.Medium, fontSize = 18.sp),
        titleSmall = TextStyle(fontFamily = selectedFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
        bodyLarge = TextStyle(fontFamily = selectedFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
        bodyMedium = TextStyle(fontFamily = selectedFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
        bodySmall = TextStyle(fontFamily = selectedFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp),
        labelLarge = TextStyle(fontFamily = selectedFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
        labelMedium = TextStyle(fontFamily = selectedFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp),
        labelSmall = TextStyle(fontFamily = selectedFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp)
    )
}

// Default Typography (compatibility)
val Typography = getTypographyForFont("ROBOTO")

// Deprecated: Remove after refactoring all usages
val ManropeFontFamily = RobotoFont
