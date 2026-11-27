package app.fnfmodmanager.ui.theme

import android.content.Context
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    /** Set from the Settings screen: pure black variant for OLED phones. */
    var amoled by mutableStateOf(false)
    val Background: Color get() = if (amoled) Color(0xFF000000) else Color(0xFF101014)
    val Surface: Color get() = if (amoled) Color(0xFF0B0A0F) else Color(0xFF17151D)
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
    val TileDark = Color(0xFF0C0B10)
    val ButtonDarkEdge = Color(0xFF55525E)

    // status tones
    val Success = Color(0xFF35C96B)
    val Info = Color(0xFF3B82F6)
    val Warning = Color(0xFFF4B740)
    val Error = Color(0xFFFF5268)
}

/**
 * Cairo (variable font, weights 400/700/800/900 from the design tokens) is loaded from assets/fonts/Cairo-VF.ttf
 * (scripts/fetch_fonts.sh downloads it). If it is missing, IBM Plex Sans Arabic, then the system font, is used.
 */
private fun appFamily(context: Context): FontFamily {
    val am = context.assets
    fun has(path: String) = runCatching { am.open(path).close(); true }.getOrDefault(false)
    if (has("fonts/Cairo-VF.ttf")) {
        return FontFamily(
            listOf(
                FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold,
                FontWeight.Bold, FontWeight.ExtraBold, FontWeight.Black,
            ).map { Font("fonts/Cairo-VF.ttf", am, it) },
        )
    }
    val plex = listOf(
        "Regular" to FontWeight.Normal, "Medium" to FontWeight.Medium,
        "SemiBold" to FontWeight.SemiBold, "Bold" to FontWeight.Bold,
    ).mapNotNull { (file, weight) ->
        val path = "fonts/IBMPlexSansArabic-$file.ttf"
        if (has(path)) Font(path, am, weight) else null
    }
    return if (plex.isEmpty()) FontFamily.Default else FontFamily(plex)
}

/** Sizes from design-tokens.json: caption 12, body 14, button 16, cardTitle 18, screenTitle 26, headline 32. */
private fun fnfTypography(f: FontFamily) = Typography(
    headlineMedium = TextStyle(fontFamily = f, fontWeight = FontWeight.Black, fontSize = 32.sp, lineHeight = 44.sp),
    titleLarge = TextStyle(fontFamily = f, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, lineHeight = 38.sp),
    titleMedium = TextStyle(fontFamily = f, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 28.sp),
    titleSmall = TextStyle(fontFamily = f, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontFamily = f, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontFamily = f, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontFamily = f, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = f, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, lineHeight = 24.sp),
    labelMedium = TextStyle(fontFamily = f, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 20.sp),
    labelSmall = TextStyle(fontFamily = f, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 16.sp),
)

@Composable
fun FnfTheme(amoled: Boolean = false, content: @Composable () -> Unit) {
    SideEffect { FnfColors.amoled = amoled }
    val context = LocalContext.current
    val typography = remember { fnfTypography(appFamily(context)) }
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
