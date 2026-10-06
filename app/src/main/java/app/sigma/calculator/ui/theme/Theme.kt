package app.sigma.calculator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import app.sigma.calculator.R

/** Every color the app uses. Change a value here and it changes everywhere. */
@Immutable
data class SigmaColors(
    val background: Color,
    val surface: Color,
    val key: Color,
    val keyFunction: Color,
    val text: Color,
    val muted: Color,
    val line: Color,
    val accent: Color,
    val onAccent: Color,
    val accentSoft: Color,
    val danger: Color,
)

val LightColors = SigmaColors(
    background = Color(0xFFEDF0F5),
    surface = Color(0xFFFFFFFF),
    key = Color(0xFFF3F5F9),
    keyFunction = Color(0xFFE4E9F1),
    text = Color(0xFF141A24),
    muted = Color(0xFF5D6778),
    line = Color(0xFFDCE2EA),
    accent = Color(0xFF2F5BD3),
    onAccent = Color(0xFFFFFFFF),
    accentSoft = Color(0xFFE3EAFB),
    danger = Color(0xFFB8323A),
)

val DarkColors = SigmaColors(
    background = Color(0xFF0D1117),
    surface = Color(0xFF151A22),
    key = Color(0xFF1C232D),
    keyFunction = Color(0xFF232C39),
    text = Color(0xFFE7EBF1),
    muted = Color(0xFF8C96A6),
    line = Color(0xFF28303D),
    accent = Color(0xFF7097FF),
    onAccent = Color(0xFF0A1020),
    accentSoft = Color(0xFF1E2A47),
    danger = Color(0xFFFF8086),
)

/** Figtree for labels, IBM Plex Mono for numbers. The font files are in res/font. */
val Figtree = FontFamily(
    Font(R.font.figtree_regular, FontWeight.Normal),
    Font(R.font.figtree_medium, FontWeight.Medium),
    Font(R.font.figtree_semibold, FontWeight.SemiBold),
    Font(R.font.figtree_bold, FontWeight.Bold),
)

val PlexMono = FontFamily(
    Font(R.font.plex_mono_regular, FontWeight.Normal),
    Font(R.font.plex_mono_medium, FontWeight.Medium),
)

/** Lets any part of the screen read the current colors with LocalSigmaColors.current. */
val LocalSigmaColors = staticCompositionLocalOf { LightColors }

@Composable
fun SigmaTheme(dark: Boolean, content: @Composable () -> Unit) {
    val colors = if (dark) DarkColors else LightColors
    val materialColors = if (dark) {
        darkColorScheme(primary = colors.accent, background = colors.background, surface = colors.surface)
    } else {
        lightColorScheme(primary = colors.accent, background = colors.background, surface = colors.surface)
    }
    CompositionLocalProvider(LocalSigmaColors provides colors) {
        MaterialTheme(colorScheme = materialColors, content = content)
    }
}
