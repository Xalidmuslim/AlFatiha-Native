package app.xalidmuslim.azkar.ui.designsystem

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.unit.dp

object AzkarMotion {
    // Motion profile matched to Al-Fatiha:
    // Shared short motion profile: navigation transitions use one 150ms tempo.
    const val dhikrPageDurationMillis = 150
    const val sheetDurationMillis = 150
    const val sheetExitDurationMillis = 150
    const val progressDurationMillis = 150
    const val toggleDurationMillis = 170
    const val stateTransitionDurationMillis = 150
    const val themeTransitionDurationMillis = 0

    val dhikrPageEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
    val sheetEasing = dhikrPageEasing
    val progressEasing = dhikrPageEasing

    val pageStartOffsetY = 7.dp
    val sheetStartOffsetY = 5.dp
    const val dhikrStartOpacity = 0f
    const val dhikrStartScale = 1f
    const val sheetStartOpacity = 0f
}
