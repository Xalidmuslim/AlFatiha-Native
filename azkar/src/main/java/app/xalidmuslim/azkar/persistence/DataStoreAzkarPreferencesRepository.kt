package app.xalidmuslim.azkar.persistence

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import app.xalidmuslim.azkar.content.AzkarPeriod
import app.xalidmuslim.azkar.ui.designsystem.ArabicFontFamily
import app.xalidmuslim.azkar.ui.designsystem.AzkarDimensions
import app.xalidmuslim.azkar.ui.designsystem.AzkarThemeMode
import app.xalidmuslim.azkar.ui.designsystem.RussianFontFamily
import app.xalidmuslim.azkar.ui.reading.AzkarReaderSettings
import app.xalidmuslim.azkar.ui.reading.AzkarReaderStyle
import app.xalidmuslim.azkar.ui.reading.AzkarReaderViewMode
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DataStoreAzkarPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) : AzkarPreferencesRepository {
    override fun observeSnapshot(
        date: LocalDate,
        visibleItemIds: Set<String>,
    ): Flow<AzkarPreferencesSnapshot> =
        dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { preferences -> snapshot(preferences, date, visibleItemIds) }

    override suspend fun saveSettings(settings: AzkarReaderSettings) {
        val safe = validate(settings)
        dataStore.edit { preferences ->
            preferences[AzkarPreferenceKeys.RussianFont] = safe.russianFontFamily.name
            preferences[AzkarPreferenceKeys.ArabicFont] = safe.arabicFontFamily.name
            preferences[AzkarPreferenceKeys.ArabicSize] = safe.arabicSizeSp
            preferences[AzkarPreferenceKeys.RussianSize] = safe.russianSizeSp
            preferences[AzkarPreferenceKeys.TransliterationSize] = safe.transliterationSizeSp
            preferences[AzkarPreferenceKeys.LineHeight] = safe.lineHeight
            preferences[AzkarPreferenceKeys.ReaderStyle] = safe.readerStyle.name
            preferences[AzkarPreferenceKeys.ShowTranslation] = safe.showTranslation
            preferences[AzkarPreferenceKeys.ShowTransliteration] = safe.showTransliteration
            preferences[AzkarPreferenceKeys.ShowSources] = safe.showSources
            preferences[AzkarPreferenceKeys.ShowNotes] = safe.showNotes
            preferences[AzkarPreferenceKeys.HideCompleted] = safe.hideCompleted
            preferences[AzkarPreferenceKeys.Theme] = safe.themeMode.name
        }
    }

    override suspend fun saveViewMode(viewMode: AzkarReaderViewMode) {
        dataStore.edit { preferences ->
            preferences[AzkarPreferenceKeys.ViewMode] = viewMode.name
        }
    }

    override suspend fun saveLastPeriod(period: AzkarPeriod) {
        dataStore.edit { preferences ->
            preferences[AzkarPreferenceKeys.LastPeriod] = period.name
        }
    }

    override suspend fun saveLastItem(period: AzkarPeriod, stableDhikrId: String) {
        if (stableDhikrId.isBlank()) return
        dataStore.edit { preferences ->
            preferences[AzkarPreferenceKeys.lastItem(period)] = stableDhikrId
        }
    }

    override suspend fun incrementProgress(
        date: LocalDate,
        stableDhikrId: String,
        target: Int,
    ): Int {
        if (stableDhikrId.isBlank()) return 0
        val safeTarget = target.coerceAtLeast(1)
        val key = AzkarPreferenceKeys.progress(date, stableDhikrId)
        val result = dataStore.edit { preferences ->
            val current = (preferences[key] ?: 0).coerceIn(0, safeTarget)
            preferences[key] = if (current >= safeTarget) safeTarget else current + 1
        }
        return (result[key] ?: 0).coerceIn(0, safeTarget)
    }

    override suspend fun decrementProgress(
        date: LocalDate,
        stableDhikrId: String,
        target: Int,
    ): Int {
        if (stableDhikrId.isBlank()) return 0
        val safeTarget = target.coerceAtLeast(1)
        val key = AzkarPreferenceKeys.progress(date, stableDhikrId)
        val result = dataStore.edit { preferences ->
            val current = (preferences[key] ?: 0).coerceIn(0, safeTarget)
            preferences[key] = (current - 1).coerceAtLeast(0)
        }
        return (result[key] ?: 0).coerceIn(0, safeTarget)
    }

    override suspend fun resetProgress(
        date: LocalDate,
        visibleItemIds: Set<String>,
    ) {
        dataStore.edit { preferences ->
            visibleItemIds
                .filter { it.isNotBlank() }
                .forEach { stableId ->
                    preferences[AzkarPreferenceKeys.progress(date, stableId)] = 0
                }
        }
    }

    private fun snapshot(
        preferences: Preferences,
        date: LocalDate,
        visibleItemIds: Set<String>,
    ): AzkarPreferencesSnapshot {
        val defaults = AzkarReaderSettings()
        val settings = AzkarReaderSettings(
            russianFontFamily = enumOrDefault(
                preferences[AzkarPreferenceKeys.RussianFont],
                RussianFontFamily.values(),
                defaults.russianFontFamily,
            ).takeIf {
                it == RussianFontFamily.PT_SERIF ||
                    it == RussianFontFamily.INTER ||
                    it == RussianFontFamily.ANDROID_SANS ||
                    it == RussianFontFamily.LITERATA
            } ?: defaults.russianFontFamily,
            arabicFontFamily = enumOrDefault(
                preferences[AzkarPreferenceKeys.ArabicFont],
                ArabicFontFamily.values(),
                defaults.arabicFontFamily,
            ).takeIf {
                it == ArabicFontFamily.NOTO_NASKH_ARABIC ||
                    it == ArabicFontFamily.NOTO_SANS_ARABIC ||
                    it == ArabicFontFamily.AMIRI ||
                    it == ArabicFontFamily.SCHEHERAZADE_NEW
            } ?: defaults.arabicFontFamily,
            arabicSizeSp = validatedFloat(
                preferences[AzkarPreferenceKeys.ArabicSize],
                defaults.arabicSizeSp,
                22f,
                44f,
            ),
            russianSizeSp = validatedFloat(
                preferences[AzkarPreferenceKeys.RussianSize],
                defaults.russianSizeSp,
                13f,
                25f,
            ),
            transliterationSizeSp = validatedFloat(
                preferences[AzkarPreferenceKeys.TransliterationSize],
                defaults.transliterationSizeSp,
                AzkarDimensions.transliterationSizeMinSp,
                AzkarDimensions.transliterationSizeMaxSp,
            ),
            lineHeight = validatedFloat(
                preferences[AzkarPreferenceKeys.LineHeight],
                defaults.lineHeight,
                1.25f,
                2.00f,
            ),
            readerStyle = AzkarReaderStyle.Book,
            showTranslation = preferences[AzkarPreferenceKeys.ShowTranslation] ?: true,
            showTransliteration = preferences[AzkarPreferenceKeys.ShowTransliteration] ?: false,
            showSources = preferences[AzkarPreferenceKeys.ShowSources] ?: true,
            showNotes = preferences[AzkarPreferenceKeys.ShowNotes] ?: true,
            hideCompleted = preferences[AzkarPreferenceKeys.HideCompleted] ?: false,
            themeMode = enumOrDefault(
                preferences[AzkarPreferenceKeys.Theme],
                AzkarThemeMode.values(),
                AzkarThemeMode.System,
            ),
        )
        val viewMode = enumOrDefault(
            preferences[AzkarPreferenceKeys.ViewMode],
            AzkarReaderViewMode.values(),
            AzkarReaderViewMode.Cards,
        )
        val progress = visibleItemIds.associateWith { stableId ->
            (preferences[AzkarPreferenceKeys.progress(date, stableId)] ?: 0).coerceAtLeast(0)
        }
        val lastPeriod = enumOrDefault(
            preferences[AzkarPreferenceKeys.LastPeriod],
            AzkarPeriod.values(),
            AzkarPeriod.Morning,
        )
        val lastItemByPeriod = AzkarPeriod.values().mapNotNull { period ->
            preferences[AzkarPreferenceKeys.lastItem(period)]
                ?.takeIf { it.isNotBlank() }
                ?.let { period to it }
        }.toMap()
        return AzkarPreferencesSnapshot(
            settings = settings,
            viewMode = viewMode,
            progressById = progress,
            lastPeriod = lastPeriod,
            lastItemByPeriod = lastItemByPeriod,
        )
    }

    private fun validate(settings: AzkarReaderSettings): AzkarReaderSettings {
        val defaults = AzkarReaderSettings()
        return settings.copy(
            russianFontFamily = when (settings.russianFontFamily) {
                RussianFontFamily.LITERATA,
                RussianFontFamily.PT_SERIF,
                RussianFontFamily.INTER,
                RussianFontFamily.ANDROID_SANS -> settings.russianFontFamily
                else -> RussianFontFamily.ANDROID_SANS
            },
            arabicFontFamily = when (settings.arabicFontFamily) {
                ArabicFontFamily.NOTO_NASKH_ARABIC,
                ArabicFontFamily.NOTO_SANS_ARABIC,
                ArabicFontFamily.AMIRI,
                ArabicFontFamily.SCHEHERAZADE_NEW -> settings.arabicFontFamily
            },
            readerStyle = AzkarReaderStyle.Book,
            arabicSizeSp = validatedFloat(settings.arabicSizeSp, defaults.arabicSizeSp, 22f, 44f),
            russianSizeSp = validatedFloat(settings.russianSizeSp, defaults.russianSizeSp, 13f, 25f),
            transliterationSizeSp = validatedFloat(
                settings.transliterationSizeSp,
                defaults.transliterationSizeSp,
                AzkarDimensions.transliterationSizeMinSp,
                AzkarDimensions.transliterationSizeMaxSp,
            ),
            lineHeight = validatedFloat(settings.lineHeight, defaults.lineHeight, 1.25f, 2.00f),
        )
    }

    private fun validatedFloat(
        value: Float?,
        default: Float,
        min: Float,
        max: Float,
    ): Float = value
        ?.takeIf { it.isFinite() }
        ?.coerceIn(min, max)
        ?: default

    private fun <T : Enum<T>> enumOrDefault(
        raw: String?,
        values: Array<T>,
        default: T,
    ): T = values.firstOrNull { it.name == raw } ?: default
}
