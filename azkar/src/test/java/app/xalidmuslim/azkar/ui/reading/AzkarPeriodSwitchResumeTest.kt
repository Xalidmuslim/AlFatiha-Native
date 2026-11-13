package app.xalidmuslim.azkar.ui.reading

import app.xalidmuslim.azkar.content.AzkarCatalog
import app.xalidmuslim.azkar.content.AzkarPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AzkarPeriodSwitchResumeTest {
    @Test
    fun rapidMorningEveningNightSwitchesKeepEachPeriodPosition() {
        val reader = AzkarPeriodReaderController()
        reader.navigation.selectAnchor(2)
        assertTrue(reader.switchTo(AzkarPeriod.Evening))
        reader.navigation.selectAnchor(3)
        assertTrue(reader.switchTo(AzkarPeriod.Night))
        reader.navigation.selectAnchor(1)
        assertTrue(reader.switchTo(AzkarPeriod.Morning))
        assertEquals(2, reader.navigation.state.activeIndex)
        assertTrue(reader.switchTo(AzkarPeriod.Night))
        assertEquals(1, reader.navigation.state.activeIndex)
        assertTrue(reader.switchTo(AzkarPeriod.Evening))
        assertEquals(3, reader.navigation.state.activeIndex)
        assertFalse(reader.switchTo(AzkarPeriod.Evening))
    }

    @Test
    fun returningToUnvisitedPeriodRestoresSavedStableId() {
        val reader = AzkarPeriodReaderController()
        val savedId = AzkarCatalog.itemsFor(AzkarPeriod.Evening)[2].id
        assertTrue(reader.switchTo(AzkarPeriod.Evening, savedItemId = savedId))
        assertEquals(2, reader.navigation.state.activeIndex)
        assertTrue(reader.switchTo(AzkarPeriod.Morning))
        assertTrue(reader.switchTo(AzkarPeriod.Evening, savedItemId = savedId))
        assertEquals(2, reader.navigation.state.activeIndex)
    }

    @Test
    fun everyPeriodSwitchIncrementsTransitionGenerationOnce() {
        val reader = AzkarPeriodReaderController()
        assertEquals(0, reader.switchGeneration)
        assertTrue(reader.switchTo(AzkarPeriod.Evening))
        assertEquals(1, reader.switchGeneration)
        assertFalse(reader.switchTo(AzkarPeriod.Evening))
        assertEquals(1, reader.switchGeneration)
        assertTrue(reader.switchTo(AzkarPeriod.Night))
        assertEquals(2, reader.switchGeneration)
        assertTrue(reader.switchTo(AzkarPeriod.Morning))
        assertEquals(3, reader.switchGeneration)
    }
}
