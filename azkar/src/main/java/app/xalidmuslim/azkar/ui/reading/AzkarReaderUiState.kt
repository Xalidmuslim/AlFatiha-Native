package app.xalidmuslim.azkar.ui.reading

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.xalidmuslim.azkar.persistence.AzkarDateProvider
import app.xalidmuslim.azkar.persistence.AzkarPreferencesRepository
import app.xalidmuslim.azkar.persistence.SystemAzkarDateProvider
import app.xalidmuslim.azkar.content.AzkarPeriod
import app.xalidmuslim.azkar.ui.designsystem.ArabicFontFamily
import app.xalidmuslim.azkar.ui.designsystem.AzkarDimensions
import app.xalidmuslim.azkar.ui.designsystem.AzkarThemeMode
import app.xalidmuslim.azkar.ui.designsystem.RussianFontFamily
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

enum class AzkarReaderSheet {
    None,
    Settings,
    Contents,
    Explanation,
    SourceInfo,
    Actions,
}

enum class AzkarReaderStyle {
    Book,
    Compact,
}

enum class AzkarReaderViewMode {
    Cards,
    List,
}

data class AzkarReaderSettings(
    val russianFontFamily: RussianFontFamily = RussianFontFamily.ANDROID_SANS,
    val arabicFontFamily: ArabicFontFamily = ArabicFontFamily.NOTO_SANS_ARABIC,
    val arabicSizeSp: Float = AzkarDimensions.defaultArabicSizeSp,
    val russianSizeSp: Float = AzkarDimensions.defaultRussianSizeSp,
    val transliterationSizeSp: Float = AzkarDimensions.defaultTransliterationSizeSp,
    val lineHeight: Float = AzkarDimensions.defaultReaderLineHeight,
    val readerStyle: AzkarReaderStyle = AzkarReaderStyle.Book,
    val showTranslation: Boolean = true,
    val showTransliteration: Boolean = false,
    val showSources: Boolean = true,
    val showNotes: Boolean = true,
    val hideCompleted: Boolean = false,
    val themeMode: AzkarThemeMode = AzkarThemeMode.System,
)

data class AzkarReaderUiState(
    val activeSheet: AzkarReaderSheet = AzkarReaderSheet.None,
    val selectedExplanationId: String? = null,
    val selectedActionId: String? = null,
    val settings: AzkarReaderSettings = AzkarReaderSettings(),
    val viewMode: AzkarReaderViewMode = AzkarReaderViewMode.Cards,
    val progressById: Map<String, Int> = emptyMap(),
    val lastPeriod: AzkarPeriod = AzkarPeriod.Morning,
    val lastItemByPeriod: Map<AzkarPeriod, String> = emptyMap(),
    val isHydrated: Boolean = true,
) {
    fun allowsHorizontalPaging(entryCount: Int): Boolean =
        viewMode == AzkarReaderViewMode.Cards &&
            activeSheet == AzkarReaderSheet.None &&
            entryCount > 1
}

class AzkarReaderUiController(
    initialSettings: AzkarReaderSettings = AzkarReaderSettings(),
    initialViewMode: AzkarReaderViewMode = AzkarReaderViewMode.Cards,
    private val repository: AzkarPreferencesRepository? = null,
    dateProvider: AzkarDateProvider = SystemAzkarDateProvider,
    private val persistenceScope: CoroutineScope? = null,
    visibleItemIds: Set<String> = emptySet(),
) {
    private val sessionDate: LocalDate = dateProvider.currentDate()
    private val persistentVisibleItemIds: Set<String> = visibleItemIds.toSet()

    var state by mutableStateOf(
        AzkarReaderUiState(
            settings = initialSettings,
            viewMode = initialViewMode,
            progressById = if (repository == null) {
                emptyMap()
            } else {
                persistentVisibleItemIds.associateWith { 0 }
            },
            isHydrated = repository == null,
        ),
    )
        private set

    init {
        if (repository != null) {
            val scope = requireNotNull(persistenceScope) {
                "Persistent reader controller requires a CoroutineScope"
            }
            scope.launch {
                try {
                    repository.observeSnapshot(sessionDate, persistentVisibleItemIds)
                        .collect { snapshot ->
                            state = state.copy(
                                settings = snapshot.settings,
                                viewMode = snapshot.viewMode,
                                progressById = snapshot.progressById,
                                lastPeriod = snapshot.lastPeriod,
                                lastItemByPeriod = snapshot.lastItemByPeriod,
                                isHydrated = true,
                            )
                        }
                } catch (error: Throwable) {
                    if (error is CancellationException) throw error
                    state = state.copy(
                        settings = AzkarReaderSettings(),
                        viewMode = AzkarReaderViewMode.Cards,
                        progressById = persistentVisibleItemIds.associateWith { 0 },
                        isHydrated = true,
                    )
                }
            }
        }
    }

    fun openSettings() {
        state = state.copy(
            activeSheet = AzkarReaderSheet.Settings,
            selectedExplanationId = null,
            selectedActionId = null,
        )
    }

    fun openContents() {
        state = state.copy(
            activeSheet = AzkarReaderSheet.Contents,
            selectedExplanationId = null,
            selectedActionId = null,
        )
    }

    fun openExplanation(itemId: String) {
        state = state.copy(
            activeSheet = AzkarReaderSheet.Explanation,
            selectedExplanationId = itemId,
            selectedActionId = null,
        )
    }

    fun openSourceInfo() {
        state = state.copy(
            activeSheet = AzkarReaderSheet.SourceInfo,
            selectedExplanationId = null,
            selectedActionId = null,
        )
    }

    fun openActions(itemId: String) {
        state = state.copy(
            activeSheet = AzkarReaderSheet.Actions,
            selectedExplanationId = null,
            selectedActionId = itemId,
        )
    }

    fun closeSheet(): Boolean {
        if (state.activeSheet == AzkarReaderSheet.None) return false
        state = state.copy(
            activeSheet = AzkarReaderSheet.None,
            selectedExplanationId = null,
            selectedActionId = null,
        )
        return true
    }

    fun updateSettings(transform: (AzkarReaderSettings) -> AzkarReaderSettings) {
        val updated = transform(state.settings)
        if (updated == state.settings) return
        state = state.copy(settings = updated)
        launchPersistence { repository?.saveSettings(updated) }
    }

    fun setViewMode(viewMode: AzkarReaderViewMode): Boolean {
        if (state.viewMode == viewMode) return false
        state = state.copy(viewMode = viewMode)
        launchPersistence { repository?.saveViewMode(viewMode) }
        return true
    }

    fun setLastPeriod(period: AzkarPeriod) {
        if (state.lastPeriod == period) return
        state = state.copy(lastPeriod = period)
        launchPersistence { repository?.saveLastPeriod(period) }
    }

    fun saveLastItem(period: AzkarPeriod, stableDhikrId: String) {
        if (stableDhikrId.isBlank()) return
        if (state.lastItemByPeriod[period] == stableDhikrId) return
        state = state.copy(lastItemByPeriod = state.lastItemByPeriod + (period to stableDhikrId))
        launchPersistence { repository?.saveLastItem(period, stableDhikrId) }
    }

    fun currentCount(stableDhikrId: String, fallback: Int = 0): Int =
        state.progressById[stableDhikrId] ?: fallback

    fun incrementProgress(stableDhikrId: String, target: Int): Boolean {
        if (target <= 0) return false
        val current = currentCount(stableDhikrId).coerceIn(0, target)
        if (current >= target) return false

        state = state.copy(
            progressById = state.progressById + (stableDhikrId to current + 1),
        )
        launchPersistence {
            repository?.incrementProgress(sessionDate, stableDhikrId, target)
        }
        return true
    }

    fun decrementProgress(stableDhikrId: String, target: Int): Boolean {
        if (target <= 0) return false
        val current = currentCount(stableDhikrId).coerceIn(0, target)
        if (current <= 0) return false

        state = state.copy(
            progressById = state.progressById + (stableDhikrId to current - 1),
        )
        launchPersistence {
            repository?.decrementProgress(sessionDate, stableDhikrId, target)
        }
        return true
    }

    fun resetSingleProgress(stableDhikrId: String) {
        if (stableDhikrId.isBlank()) return
        state = state.copy(
            progressById = state.progressById + (stableDhikrId to 0),
        )
        launchPersistence {
            repository?.resetProgress(sessionDate, setOf(stableDhikrId))
        }
    }

    fun resetProgress(visibleItemIds: Collection<String>) {
        val ids = visibleItemIds.filter { it.isNotBlank() }.toSet()
        if (ids.isEmpty()) return
        state = state.copy(
            progressById = state.progressById.toMutableMap().apply {
                ids.forEach { put(it, 0) }
            },
        )
        launchPersistence {
            repository?.resetProgress(sessionDate, ids)
        }
    }

    fun handleBack(navigation: AzkarReaderNavigationController): Boolean {
        if (closeSheet()) return true
        return navigation.back()
    }

    private fun launchPersistence(block: suspend () -> Unit) {
        val scope = persistenceScope ?: return
        if (repository == null) return
        scope.launch {
            try {
                block()
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
            }
        }
    }
}
