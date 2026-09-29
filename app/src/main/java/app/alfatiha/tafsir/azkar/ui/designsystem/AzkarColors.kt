package app.alfatiha.tafsir.azkar.ui.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class AzkarColorScheme(
    val background: Color,
    val foreground: Color,
    val card: Color,
    val surface: Color,
    val muted: Color,
    val border: Color,
    val primary: Color,
    val accent: Color,
    val warning: Color,
    val success: Color,
    val primaryActionText: Color,
    val countButtonText: Color,
    val overlay: Color,
    val doneCardBorder: Color,
    val doneMarkerBorder: Color,
    val doneMarkerBackground: Color,
    val warningBadgeBorder: Color,
    val noteBorder: Color,
    val noteBackground: Color,
    val stickyToolbarBackground: Color,
    val activeItemBackground: Color,
    val sheetHandle: Color,
)

object AzkarColors {
    private fun mixSrgb(first: Color, firstWeight: Float, second: Color): Color {
        val secondWeight = 1f - firstWeight
        return Color(
            red = first.red * firstWeight + second.red * secondWeight,
            green = first.green * firstWeight + second.green * secondWeight,
            blue = first.blue * firstWeight + second.blue * secondWeight,
            alpha = first.alpha * firstWeight + second.alpha * secondWeight,
        )
    }

    private fun scheme(
        background: Color,
        foreground: Color,
        card: Color,
        surface: Color,
        muted: Color,
        border: Color,
        primary: Color,
        accent: Color,
        warning: Color,
        success: Color,
        countButtonText: Color,
    ): AzkarColorScheme = AzkarColorScheme(
        background = background,
        foreground = foreground,
        card = card,
        surface = surface,
        muted = muted,
        border = border,
        primary = primary,
        accent = accent,
        warning = warning,
        success = success,
        primaryActionText = Color.White,
        countButtonText = countButtonText,
        overlay = Color(
            red = 9f / 255f,
            green = 12f / 255f,
            blue = 10f / 255f,
            alpha = 0.44f,
        ),
        doneCardBorder = mixSrgb(success, 0.40f, border),
        doneMarkerBorder = mixSrgb(success, 0.35f, border),
        doneMarkerBackground = mixSrgb(success, 0.10f, card),
        warningBadgeBorder = mixSrgb(warning, 0.35f, border),
        noteBorder = mixSrgb(warning, 0.26f, border),
        noteBackground = mixSrgb(warning, 0.06f, surface),
        stickyToolbarBackground = card.copy(alpha = 0.94f),
        activeItemBackground = mixSrgb(primary, 0.05f, card),
        sheetHandle = foreground.copy(alpha = 0.60f),
    )

    val Light: AzkarColorScheme = scheme(
        background = Color(0xFFF4F0E7),
        foreground = Color(0xFF171C19),
        card = Color(0xFFFBF8F1),
        surface = Color(0xFFEEE8DC),
        muted = Color(0xFF646A66),
        border = Color(0xFFD8D0C1),
        primary = Color(0xFF1F5C48),
        accent = Color(0xFFDCE8E1),
        warning = Color(0xFF9D6A20),
        success = Color(0xFF2F6B52),
        countButtonText = Color.White,
    )

    val Dark: AzkarColorScheme = scheme(
        background = Color(0xFF111613),
        foreground = Color(0xFFEDF2EE),
        card = Color(0xFF1A211C),
        surface = Color(0xFF222A24),
        muted = Color(0xFFAAB5AD),
        border = Color(0xFF323C35),
        primary = Color(0xFF7FB99E),
        accent = Color(0xFF24382E),
        warning = Color(0xFFC99B58),
        success = Color(0xFF74AD90),
        countButtonText = Color(0xFF0F1713),
    )
}
