package app.xalidmuslim.azkar.ui.reading

import app.xalidmuslim.azkar.content.AzkarPeriod
import app.xalidmuslim.azkar.persistence.AzkarPreferencesRepository
import app.xalidmuslim.azkar.persistence.AzkarPreferencesSnapshot
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AzkarResumeTest {
    @Test
    fun cachedWarmSnapshotMustNotBeTreatedAsFreshRestoration() {
        val updates = MutableSharedFlow<AzkarPreferencesSnapshot>()
        val cached = AzkarPreferencesSnapshot(
            lastPeriod = AzkarPeriod.Morning,
            lastItemByPeriod = mapOf(AzkarPeriod.Morning to "morning-old"),
        )
        val controller = AzkarReaderUiController(
            initialSnapshot = cached,
            repository = FakeRepository(updates),
            persistenceScope = CoroutineScope(Dispatchers.Unconfined),
        )

        assertEquals(AzkarPeriod.Morning, controller.state.lastPeriod)
        assertFalse("Warm cache must not finalize the resume target", controller.state.hasLiveSnapshot)

        assertTrue(
            updates.tryEmit(
                AzkarPreferencesSnapshot(
                    lastPeriod = AzkarPeriod.Evening,
                    lastItemByPeriod = mapOf(AzkarPeriod.Evening to "evening-new"),
                ),
            ),
        )
        assertTrue(controller.state.hasLiveSnapshot)
        assertEquals(AzkarPeriod.Evening, controller.state.lastPeriod)
        assertEquals("evening-new", controller.state.lastItemByPeriod[AzkarPeriod.Evening])
    }

    private class FakeRepository(
        private val changes: Flow<AzkarPreferencesSnapshot>,
    ) : AzkarPreferencesRepository {
        override fun observeSnapshot(date: LocalDate, visibleItemIds: Set<String>) = changes
        override suspend fun saveSettings(settings: AzkarReaderSettings) = Unit
        override suspend fun saveViewMode(viewMode: AzkarReaderViewMode) = Unit
        override suspend fun saveLastPeriod(period: AzkarPeriod) = Unit
        override suspend fun saveLastItem(period: AzkarPeriod, stableDhikrId: String) = Unit
        override suspend fun incrementProgress(date: LocalDate, stableDhikrId: String, target: Int) = 0
        override suspend fun resetProgress(date: LocalDate, visibleItemIds: Set<String>) = Unit
    }
}
