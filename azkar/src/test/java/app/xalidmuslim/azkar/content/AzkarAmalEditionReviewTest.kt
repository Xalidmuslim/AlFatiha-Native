package app.xalidmuslim.azkar.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Source-based textual regression; printed Amal collation still requires interior page scans. */
class AzkarAmalEditionReviewTest {
    @Test fun dailyTahlilIsAccessibleLaterInTheDayWithoutInventingEveningSunnah() {
        assertEquals(18, AzkarCatalog.itemsFor(AzkarPeriod.Morning).size)
        assertEquals(17, AzkarCatalog.itemsFor(AzkarPeriod.Evening).size)
        val daily = AzkarCatalog.item("tahlil-hundred")
        assertEquals(100, daily.targetCount)
        assertTrue(AzkarPeriod.Evening in daily.periods)
        assertTrue(daily.note.orEmpty().contains("в течение дня"))
        assertTrue(daily.note.orEmpty().contains("не устанавливает"))
    }

    @Test fun byYouTranslatesResurrectionAndReturnCorrectly() {
        val morning = AzkarCatalog.item("by-you").resolve(AzkarPeriod.Morning)
        val evening = AzkarCatalog.item("by-you").resolve(AzkarPeriod.Evening)
        assertTrue(morning.arabic.endsWith("وَإِلَيْكَ النُّشُورُ"))
        assertTrue(morning.translation.contains("воскресение"))
        assertTrue(evening.arabic.endsWith("وَإِلَيْكَ الْمَصِيرُ"))
        assertTrue(evening.translation.contains("возвращение"))
    }

    @Test fun endOfBaqarahContainsAllPartsOfBothVersesInRussian() {
        val verse = AzkarCatalog.item("baqarah-last-two").resolve(AzkarPeriod.Night)
        assertTrue(verse.translation.contains("не делаем различий"))
        assertTrue(verse.translation.contains("Прости"))
        assertTrue(verse.translation.contains("Покровитель"))
    }

    @Test fun alMulkHasVerseAlignedLiteraryRussianTranslation() {
        assertEquals(30, AzkarNightQuran.alMulkVerses.size)
        assertEquals(30, AzkarNightQuran.alMulkRussianVerses.size)
        val translation = AzkarCatalog.item("night-mulk").resolve(AzkarPeriod.Night).translation
        assertTrue(translation.contains("﴿1﴾"))
        assertTrue(translation.contains("﴿30﴾"))
        assertTrue(translation.contains("вода"))
        assertTrue(translation.length > 1800)
    }
}
