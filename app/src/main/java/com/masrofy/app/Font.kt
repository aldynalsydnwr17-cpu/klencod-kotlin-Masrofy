package com.masrofy.app

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/** خط Cairo لكل التطبيق. */
val CairoFamily = FontFamily(
    Font(R.font.cairo_regular, FontWeight.Normal),
    Font(R.font.cairo_regular, FontWeight.Medium),
    Font(R.font.cairo_semibold, FontWeight.SemiBold),
    Font(R.font.cairo_bold, FontWeight.Bold),
    Font(R.font.cairo_extrabold, FontWeight.ExtraBold)
)

fun cairoTypography(): Typography {
    val b = Typography()
    return Typography(
        displayLarge = b.displayLarge.copy(fontFamily = CairoFamily),
        displayMedium = b.displayMedium.copy(fontFamily = CairoFamily),
        displaySmall = b.displaySmall.copy(fontFamily = CairoFamily),
        headlineLarge = b.headlineLarge.copy(fontFamily = CairoFamily),
        headlineMedium = b.headlineMedium.copy(fontFamily = CairoFamily),
        headlineSmall = b.headlineSmall.copy(fontFamily = CairoFamily),
        titleLarge = b.titleLarge.copy(fontFamily = CairoFamily),
        titleMedium = b.titleMedium.copy(fontFamily = CairoFamily),
        titleSmall = b.titleSmall.copy(fontFamily = CairoFamily),
        bodyLarge = b.bodyLarge.copy(fontFamily = CairoFamily),
        bodyMedium = b.bodyMedium.copy(fontFamily = CairoFamily),
        bodySmall = b.bodySmall.copy(fontFamily = CairoFamily),
        labelLarge = b.labelLarge.copy(fontFamily = CairoFamily),
        labelMedium = b.labelMedium.copy(fontFamily = CairoFamily),
        labelSmall = b.labelSmall.copy(fontFamily = CairoFamily)
    )
}
