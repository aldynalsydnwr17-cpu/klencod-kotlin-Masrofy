package app.fnfmodmanager.ui.theme

import android.content.Context
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp

/** Palette from the design brief plus the extra tones used by the SVG components. */
object FnfColors {
    val Background = Color(0xFF101014)
    val Surface = Color(0xFF17151D)
    val Card = Color(0xFF25232B)
    val Accent = Color(0xFFF21D3D)
    val Text = Color(0xFFF5F5F7)
    val Muted = Color(0xFFA69EAE)

    // taken from components/*.svg
    val AccentHigh = Color(0xFFFF3154)
    val AccentLow = Color(0xFFC9002A)
    val AccentEdge = Color(0xFFFF5570)
    val GlassStroke = Color(0xFF51404F)
    val PlaceholderFill = Color(0xFF211D29)

    // status tones
    val Success = Color(0xFF2ED573)
    val Info = Color(0xFF3B82F6)
    val Warning = Color(0xFFFFB020)
}

/**
 * IBM Plex Sans Arabic is loaded from assets/fonts (scripts/fetch_fonts.sh puts the four weights there).
 * If the files are not there yet the system font is used instead, so the app still runs.
 */
private fun plexFamily(context: Context): FontFamily {
    val am = context.assets
    fun has(path: String) = runCatching { am.open(path).close(); true }.getOrDefault(false)
    val weights = listOf(
        "Regular" to FontWeight.Normal,
        "Medium" to FontWeight.Medium,
        "SemiBold" to FontWeight.SemiBold,
        "Bold" to FontWeight.Bold,
    )
    val fonts = weights.mapNotNull { (file, weight) ->
        val path = "fonts/IBMPlexSansArabic-$file.ttf"
        if (has(path)) Font(path, am, weight) else null
    }
    return if (fonts.isEmpty()) FontFamily.Default else FontFamily(fonts)
}

private fun fnfTypography(f: FontFamily) = Typography(
    titleLarge = TextStyle(fontFamily = f, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 32.sp),
    titleMedium = TextStyle(fontFamily = f, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 28.sp),
    titleSmall = TextStyle(fontFamily = f, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontFamily = f, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontFamily = f, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontFamily = f, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = f, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 22.sp),
    labelMedium = TextStyle(fontFamily = f, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontFamily = f, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
)

@Composable
fun FnfTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val typography = remember { fnfTypography(plexFamily(context)) }
    val scheme = darkColorScheme(
        primary = FnfColors.Accent,
        onPrimary = Color.White,
        background = FnfColors.Background,
        onBackground = FnfColors.Text,
        surface = FnfColors.Card,
        onSurface = FnfColors.Text,
        surfaceVariant = FnfColors.Surface,
        onSurfaceVariant = FnfColors.Muted,
        outline = FnfColors.GlassStroke,
    )
    // The whole interface is Arabic: always right-to-left, whatever the phone's language is.
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        LocalContentColor provides FnfColors.Text,
    ) {
        MaterialTheme(colorScheme = scheme, typography = typography) {
            ProvideTextStyle(typography.bodyMedium.copy(color = FnfColors.Text)) { content() }
        }
    }
}
