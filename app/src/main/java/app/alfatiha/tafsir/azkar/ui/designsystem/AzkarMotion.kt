package app.alfatiha.tafsir.azkar.ui.designsystem

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.unit.dp

object AzkarMotion {
    const val dhikrPageDurationMillis = 620
    const val sheetDurationMillis = 300
    const val progressDurationMillis = 350
    const val toggleDurationMillis = 200
    const val stateTransitionDurationMillis = 200
    val dhikrPageEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val sheetEasing = dhikrPageEasing
    val progressEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
    val nextStartOffsetX = 24.dp
    val previousStartOffsetX = (-24).dp
    val sheetStartOffsetY = 18.dp
    const val dhikrStartOpacity = 0.62f
    const val dhikrStartScale = 0.994f
    const val sheetStartOpacity = 0.70f
}
