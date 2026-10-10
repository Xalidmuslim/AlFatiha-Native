package com.xalid.meditsinaproroka.nativeapp

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF86AB94),
    onPrimary = Color(0xFF152019),
    primaryContainer = Color(0xFF29362E),
    onPrimaryContainer = Color(0xFFEDE7DC),
    background = Color(0xFF191D1A),
    onBackground = Color(0xFFEDE7DC),
    surface = Color(0xFF202622),
    onSurface = Color(0xFFEDE7DC),
    surfaceVariant = Color(0xFF292F2A),
    onSurfaceVariant = Color(0xFFD5DCD3),
    surfaceTint = Color(0xFF86AB94),
    surfaceDim = Color(0xFF191D1A),
    surfaceBright = Color(0xFF2A302B),
    surfaceContainerLowest = Color(0xFF181C19),
    surfaceContainerLow = Color(0xFF202622),
    surfaceContainer = Color(0xFF252B26),
    surfaceContainerHigh = Color(0xFF2B322C),
    surfaceContainerHighest = Color(0xFF323A33),
    inverseSurface = Color(0xFFEDE7DC),
    inverseOnSurface = Color(0xFF222821),
    scrim = Color(0xFF111611),
    outline = Color(0xFF343B35),
    outlineVariant = Color(0xFF343B35),
    secondary = Color(0xFFBDA276),
    onSecondary = Color(0xFF191D1A),
    error = Color(0xFFD08377),
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF305A43),
    onPrimary = Color(0xFFFFFAF2),
    primaryContainer = Color(0xFFE8EEE9),
    onPrimaryContainer = Color(0xFF203D2E),
    background = Color(0xFFF0E4D3),
    onBackground = Color(0xFF211D19),
    surface = Color(0xFFF8F0E4),
    onSurface = Color(0xFF211D19),
    surfaceVariant = Color(0xFFF3EBDD),
    onSurfaceVariant = Color(0xFF625D53),
    // Material 3 dialogs, bottom sheets and default containers now use the
    // same warm paper palette instead of fallback pink-lavender tones.
    surfaceTint = Color(0xFF305A43),
    surfaceDim = Color(0xFFE5D9C9),
    surfaceBright = Color(0xFFFCF6EC),
    surfaceContainerLowest = Color(0xFFFFF9EF),
    surfaceContainerLow = Color(0xFFF8F0E5),
    surfaceContainer = Color(0xFFF4EBDF),
    surfaceContainerHigh = Color(0xFFF0E6D8),
    surfaceContainerHighest = Color(0xFFE9DECF),
    inverseSurface = Color(0xFF302B25),
    inverseOnSurface = Color(0xFFF9F1E7),
    scrim = Color(0xFF161711),
    outline = Color(0xFFD8C8AE),
    outlineVariant = Color(0xFFD8C8AE),
    secondary = Color(0xFFB99A62),
    onSecondary = Color(0xFF101010),
    error = Color(0xFF9B4940),
)

private val WebTypography = Typography(
    displayLarge = TextStyle(fontFamily = WebLiterataFont, fontWeight = FontWeight.SemiBold, fontSize = 40.sp, lineHeight = 44.sp),
    displayMedium = TextStyle(fontFamily = WebLiterataFont, fontWeight = FontWeight.SemiBold, fontSize = 34.sp, lineHeight = 38.sp),
    displaySmall = TextStyle(fontFamily = WebLiterataFont, fontWeight = FontWeight.SemiBold, fontSize = 29.sp, lineHeight = 33.sp),
    headlineLarge = TextStyle(fontFamily = WebLiterataFont, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 32.sp),
    headlineMedium = TextStyle(fontFamily = WebLiterataFont, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 28.sp),
    headlineSmall = TextStyle(fontFamily = WebLiterataFont, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 24.sp),
    titleLarge = TextStyle(fontFamily = WebLiterataFont, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 23.sp),
    titleMedium = TextStyle(fontFamily = WebSansFont, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 21.sp),
    titleSmall = TextStyle(fontFamily = WebSansFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 19.sp),
    bodyLarge = TextStyle(fontFamily = WebSansFont, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontFamily = WebSansFont, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = WebSansFont, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontFamily = WebSansFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontFamily = WebSansFont, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = WebSansFont, fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp, lineHeight = 13.sp),
)

private val WebShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(13.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(22.dp),
)

@Composable
fun MedicinaTheme(mode: String, content: @Composable () -> Unit) {
    val dark = when (mode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as? Activity)?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
        }
    }
    MaterialTheme(
        colorScheme = if (dark) DarkScheme else LightScheme,
        typography = WebTypography,
        shapes = WebShapes,
        content = content,
    )
}
