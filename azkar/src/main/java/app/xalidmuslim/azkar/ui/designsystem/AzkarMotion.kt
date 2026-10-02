package app.xalidmuslim.azkar.ui.designsystem

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.unit.dp

object AzkarMotion {
    // Motion profile matched to Al-Fatiha:
    // page fade + 7dp lift ~155ms, small reveals ~170ms.
    const val dhikrPageDurationMillis = 155
    const val sheetDurationMillis = 170
    const val sheetExitDurationMillis = 140
    const val progressDurationMillis = 155
    const val toggleDurationMillis = 170
    const val stateTransitionDurationMillis = 155
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
