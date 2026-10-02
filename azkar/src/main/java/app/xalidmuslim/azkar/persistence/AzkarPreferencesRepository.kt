package app.xalidmuslim.azkar.persistence

import app.xalidmuslim.azkar.content.AzkarPeriod
import app.xalidmuslim.azkar.ui.reading.AzkarReaderSettings
import app.xalidmuslim.azkar.ui.reading.AzkarReaderViewMode
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

data class AzkarPreferencesSnapshot(
    val settings: AzkarReaderSettings = AzkarReaderSettings(),
    val viewMode: AzkarReaderViewMode = AzkarReaderViewMode.Cards,
    val progressById: Map<String, Int> = emptyMap(),
    val lastPeriod: AzkarPeriod = AzkarPeriod.Morning,
    val lastItemByPeriod: Map<AzkarPeriod, String> = emptyMap(),
)

interface AzkarPreferencesRepository {
    fun observeSnapshot(
        date: LocalDate,
        visibleItemIds: Set<String>,
    ): Flow<AzkarPreferencesSnapshot>

    suspend fun saveSettings(settings: AzkarReaderSettings)
    suspend fun saveViewMode(viewMode: AzkarReaderViewMode)
    suspend fun saveLastPeriod(period: AzkarPeriod)
    suspend fun saveLastItem(period: AzkarPeriod, stableDhikrId: String)

    suspend fun incrementProgress(
        date: LocalDate,
        stableDhikrId: String,
        target: Int,
    ): Int

    suspend fun decrementProgress(
        date: LocalDate,
        stableDhikrId: String,
        target: Int,
    ): Int = 0

    suspend fun resetProgress(
        date: LocalDate,
        visibleItemIds: Set<String>,
    )
}
