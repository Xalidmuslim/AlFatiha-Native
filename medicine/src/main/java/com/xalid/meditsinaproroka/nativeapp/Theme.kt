package com.xalid.meditsinaproroka.nativeapp

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
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
    onSurfaceVariant = Color(0xFFF3F3F3),
    outline = Color(0xFF343B35),
    outlineVariant = Color(0xFF343B35),
    secondary = Color(0xFFBDA276),
    onSecondary = Color(0xFF191D1A),
    error = Color(0xFFD08377),
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF315C45),
    onPrimary = Color(0xFFFFFAF2),
    primaryContainer = Color(0xFFE8EEE9),
    onPrimaryContainer = Color(0xFF203D2E),
    background = Color(0xFFF4EFE5),
    onBackground = Color(0xFF101010),
    surface = Color(0xFFFBF7EF),
    onSurface = Color(0xFF101010),
    surfaceVariant = Color(0xFFEAE3D7),
    onSurfaceVariant = Color(0xFF050505),
    outline = Color(0xFFD8CEBD),
    outlineVariant = Color(0xFFD8CEBD),
    secondary = Color(0xFFB99A62),
    onSecondary = Color(0xFF101010),
    error = Color(0xFF9B4940),
)

private val WebTypography = Typography(
    displayLarge = TextStyle(fontFamily = WebModernFont, fontWeight = FontWeight.SemiBold, fontSize = 40.sp, lineHeight = 44.sp),
    displayMedium = TextStyle(fontFamily = WebModernFont, fontWeight = FontWeight.SemiBold, fontSize = 34.sp, lineHeight = 38.sp),
    displaySmall = TextStyle(fontFamily = WebModernFont, fontWeight = FontWeight.SemiBold, fontSize = 29.sp, lineHeight = 33.sp),
    headlineLarge = TextStyle(fontFamily = WebModernFont, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 32.sp),
    headlineMedium = TextStyle(fontFamily = WebModernFont, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 28.sp),
    headlineSmall = TextStyle(fontFamily = WebModernFont, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 24.sp),
    titleLarge = TextStyle(fontFamily = WebModernFont, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 23.sp),
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
    MaterialTheme(
        colorScheme = if (dark) DarkScheme else LightScheme,
        typography = WebTypography,
        shapes = WebShapes,
        content = content,
    )
}
