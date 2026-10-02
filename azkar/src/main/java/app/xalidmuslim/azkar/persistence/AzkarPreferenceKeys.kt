package app.xalidmuslim.azkar.persistence

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import app.xalidmuslim.azkar.content.AzkarPeriod
import java.time.LocalDate

internal object AzkarPreferenceKeys {
    val RussianFont = stringPreferencesKey("reader_russian_font")
    val ArabicFont = stringPreferencesKey("reader_arabic_font")
    val ArabicSize = floatPreferencesKey("reader_arabic_size")
    val RussianSize = floatPreferencesKey("reader_russian_size")
    val TransliterationSize = floatPreferencesKey("reader_transliteration_size")
    val LineHeight = floatPreferencesKey("reader_line_height")
    val ReaderStyle = stringPreferencesKey("reader_style")
    val ShowTranslation = booleanPreferencesKey("reader_show_translation")
    val ShowTransliteration = booleanPreferencesKey("reader_show_transliteration")
    val ShowSources = booleanPreferencesKey("reader_show_sources")
    val ShowNotes = booleanPreferencesKey("reader_show_notes")
    val HideCompleted = booleanPreferencesKey("reader_hide_completed")
    val Theme = stringPreferencesKey("reader_theme")
    val ViewMode = stringPreferencesKey("reader_view_mode")
    val LastPeriod = stringPreferencesKey("reader_last_period")
    val LastMorningItem = stringPreferencesKey("reader_last_morning_item")
    val LastEveningItem = stringPreferencesKey("reader_last_evening_item")

    fun lastItem(period: AzkarPeriod) = when (period) {
        AzkarPeriod.Morning -> LastMorningItem
        AzkarPeriod.Evening -> LastEveningItem
    }

    fun progress(date: LocalDate, stableDhikrId: String) =
        intPreferencesKey("progress_${date}_${stableDhikrId}")
}
