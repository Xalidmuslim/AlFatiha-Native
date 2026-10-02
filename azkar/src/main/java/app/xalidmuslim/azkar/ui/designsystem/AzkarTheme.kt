package app.xalidmuslim.azkar.ui.designsystem

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AzkarThemeMode { Light, Dark, System }

internal fun resolveAzkarDarkTheme(mode: AzkarThemeMode, systemIsDark: Boolean): Boolean = when (mode) {
    AzkarThemeMode.Light -> false
    AzkarThemeMode.Dark -> true
    AzkarThemeMode.System -> systemIsDark
}

private val LocalAzkarColors = staticCompositionLocalOf { AzkarColors.Light }
private val LocalAzkarTypography = staticCompositionLocalOf { AzkarTypography.create() }
private val LocalAzkarElevation = staticCompositionLocalOf { AzkarElevation.Light }

object AzkarThemeValues {
    val colors: AzkarColorScheme
        @Composable @ReadOnlyComposable get() = LocalAzkarColors.current
    val typography: AzkarTypographySet
        @Composable @ReadOnlyComposable get() = LocalAzkarTypography.current
    val elevation: AzkarElevationScheme
        @Composable @ReadOnlyComposable get() = LocalAzkarElevation.current
}

@Composable
fun AzkarTheme(
    themeMode: AzkarThemeMode = AzkarThemeMode.System,
    russianFontFamily: RussianFontFamily = RussianFontFamily.LITERATA,
    arabicFontFamily: ArabicFontFamily = ArabicFontFamily.NOTO_NASKH_ARABIC,
    arabicSizeSp: Float = AzkarDimensions.defaultArabicSizeSp,
    russianSizeSp: Float = AzkarDimensions.defaultRussianSizeSp,
    readerLineHeight: Float = AzkarDimensions.defaultReaderLineHeight,
    prepareEdgeToEdge: Boolean = true,
    content: @Composable () -> Unit,
) {
    val dark = resolveAzkarDarkTheme(themeMode, isSystemInDarkTheme())
    val colors = if (dark) AzkarColors.Dark else AzkarColors.Light
    val typography = AzkarTypography.create(
        russianFont = russianFontFamily,
        arabicFont = arabicFontFamily,
        arabicSizeSp = arabicSizeSp,
        russianSizeSp = russianSizeSp,
        readerLineHeight = readerLineHeight,
    )
    val elevation = if (dark) AzkarElevation.Dark else AzkarElevation.Light

    if (prepareEdgeToEdge) AzkarEdgeToEdgeEffect(dark)

    CompositionLocalProvider(
        LocalAzkarColors provides colors,
        LocalAzkarTypography provides typography,
        LocalAzkarElevation provides elevation,
        content = content,
    )
}

@Composable
private fun AzkarEdgeToEdgeEffect(dark: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    SideEffect {
        val activity = view.context.findActivity() ?: return@SideEffect
        val window = activity.window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
