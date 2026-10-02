package app.xalidmuslim.azkar.ui.reading

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Horizontal paging is implemented with Compose's orientation-aware draggable instead of a
 * hand-written pointer loop. The previous detector could classify a slightly diagonal swipe as
 * vertical before horizontal displacement reached its threshold; after that it never recovered.
 *
 * Draggable lets horizontal and vertical gesture recognizers compete on touch slop and keeps the
 * horizontal drag once it wins, which is substantially more reliable inside verticalScroll.
 */
internal fun Modifier.azkarHorizontalPaging(
    enabled: Boolean,
    threshold: Dp = 22.dp,
    flingVelocityThresholdPxPerSecond: Float = 620f,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
): Modifier = composed {
    if (!enabled) return@composed this

    val density = LocalDensity.current
    val thresholdPx = with(density) { threshold.toPx() }
    val latestPrevious by rememberUpdatedState(onPrevious)
    val latestNext by rememberUpdatedState(onNext)

    var totalDragPx by remember { mutableFloatStateOf(0f) }
    val dragState = rememberDraggableState { delta ->
        totalDragPx += delta
    }

    this.draggable(
        state = dragState,
        orientation = Orientation.Horizontal,
        enabled = enabled,
        startDragImmediately = false,
        onDragStarted = { _ ->
            totalDragPx = 0f
        },
        onDragStopped = { velocity ->
            val distance = totalDragPx
            totalDragPx = 0f

            val enoughDistance = abs(distance) >= thresholdPx
            val enoughVelocity = abs(velocity) >= flingVelocityThresholdPxPerSecond
            if (enoughDistance || enoughVelocity) {
                // Prefer actual finger displacement. Velocity is only a fallback for a very short fling.
                val signedDirection = if (abs(distance) >= 4f) distance else velocity
                if (signedDirection < 0f) {
                    latestNext()
                } else {
                    latestPrevious()
                }
            }
        },
    )
}
