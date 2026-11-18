package com.khatti.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.khatti.app.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

val Ink = Color(0xFF0B2D5B)
val Amber = Color(0xFFD47A2C)
val Paper = Color(0xFFF8F4E9)
val PaperCard = Color(0xFFFFFBF1)
val Gray = Color(0xFF9CA3AF)
val RuleRed = Color(0xFFD9534F)
val Good = Color(0xFF2E9E5B)

private val Light = lightColorScheme(
    primary = Ink, onPrimary = Color.White, secondary = Amber, onSecondary = Color.White,
    background = Paper, onBackground = Ink, surface = PaperCard, onSurface = Ink,
    surfaceVariant = Color(0xFFF1E8D2), onSurfaceVariant = Color(0xFF5A6477),
)
private val Dark = darkColorScheme(
    primary = Color(0xFF9DB8E8), onPrimary = Ink, secondary = Amber, onSecondary = Color.White,
    background = Color(0xFF101722), onBackground = Paper, surface = Color(0xFF1A2433), onSurface = Paper,
    surfaceVariant = Color(0xFF243247), onSurfaceVariant = Color(0xFFB8C0CF),
)

val UiFont = FontFamily(
    Font(R.font.cairo_regular, FontWeight.Normal),
    Font(R.font.cairo_semibold, FontWeight.SemiBold),
    Font(R.font.cairo_bold, FontWeight.Bold),
    Font(R.font.cairo_extrabold, FontWeight.ExtraBold),
)
val NaskhFont = FontFamily.Serif

val AppTypography = Typography(
    headlineMedium = TextStyle(fontFamily = UiFont, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = UiFont, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = UiFont, fontSize = 14.sp, lineHeight = 22.sp),
    labelSmall = TextStyle(fontFamily = UiFont, fontSize = 12.sp),
)

val CardShape = RoundedCornerShape(20.dp)

@Composable
fun KhattiTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = if (isSystemInDarkTheme()) Dark else Light,
            typography = AppTypography,
            content = content,
        )
    }
}
