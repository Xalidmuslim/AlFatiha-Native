package app.xalidmuslim.azkar.ui.reading

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AzkarNavigationDirection {
    None,
    Next,
    Previous,
}

data class AzkarReaderNavigationState(
    val activeIndex: Int,
    val history: List<Int> = emptyList(),
    val direction: AzkarNavigationDirection = AzkarNavigationDirection.None,
    val generation: Long = 0L,
)

class AzkarReaderNavigationController(
    private val itemCount: Int,
    initialIndex: Int = 0,
) {
    init {
        require(itemCount > 0) { "Reader requires at least one item" }
        require(initialIndex in 0 until itemCount) { "Initial index is out of bounds" }
    }

    var state by mutableStateOf(AzkarReaderNavigationState(activeIndex = initialIndex))
        private set

    var currentScrollY: Int = 0
        private set

    fun next(): Boolean = navigateTo(state.activeIndex + 1)

    fun previous(): Boolean = navigateTo(state.activeIndex - 1)

    fun navigateTo(index: Int): Boolean {
        if (index !in 0 until itemCount || index == state.activeIndex) return false

        val from = state.activeIndex
        state = state.copy(
            activeIndex = index,
            history = state.history + from,
            direction = if (index > from) {
                AzkarNavigationDirection.Next
            } else {
                AzkarNavigationDirection.Previous
            },
            generation = state.generation + 1L,
        )
        currentScrollY = 0
        return true
    }

    fun selectAnchor(index: Int): Boolean {
        if (index !in 0 until itemCount || index == state.activeIndex) return false
        state = state.copy(
            activeIndex = index,
            direction = AzkarNavigationDirection.None,
            generation = state.generation + 1L,
        )
        currentScrollY = 0
        return true
    }

    fun reopenCurrentAtTop() {
        state = state.copy(
            direction = AzkarNavigationDirection.None,
            generation = state.generation + 1L,
        )
        currentScrollY = 0
    }

    fun back(): Boolean {
        val destination = state.history.lastOrNull() ?: return false
        val from = state.activeIndex
        state = state.copy(
            activeIndex = destination,
            history = state.history.dropLast(1),
            direction = if (destination > from) {
                AzkarNavigationDirection.Next
            } else {
                AzkarNavigationDirection.Previous
            },
            generation = state.generation + 1L,
        )
        currentScrollY = 0
        return true
    }

    fun recordScrollY(scrollY: Int) {
        currentScrollY = scrollY.coerceAtLeast(0)
    }
}
