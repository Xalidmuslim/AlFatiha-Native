package app.xalidmuslim.azkar.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Exact author-inventory and data migration invariants; no Android device required. */
class AzkarTarifiCollationTest {
    @Test fun inventoryIsCompleteAndOriginalIdentifiersRemainStable() {
        val ids = AzkarCatalog.all.map { it.id }
        assertEquals(22, ids.size)
        assertEquals(22, ids.distinct().size)
        assertEquals(18, AzkarCatalog.itemsFor(AzkarPeriod.Morning).size)
        assertEquals(16, AzkarCatalog.itemsFor(AzkarPeriod.Evening).size)
        assertEquals(3, AzkarCatalog.itemsFor(AzkarPeriod.Night).size)
        val previouslyPublished = setOf(
            "sayyid-istighfar","tahlil-ten","tahlil-hundred","tasbih-hundred",
            "kingdom","by-you","bismillah-protection","afiyah","pleased",
            "hayy-qayyum","fatir","muawwidhat","creation-count","fitrah",
            "perfect-words","baqarah-last-two",
        )
        assertTrue(ids.toSet().containsAll(previouslyPublished))
    }

    @Test fun verifiedRepetitionsAndDisputedReportsAreExplicit() {
        assertEquals(3, AzkarCatalog.item("health-three").targetCount)
        assertEquals(7, AzkarCatalog.item("hasbi-seven").targetCount)
        assertEquals(100, AzkarCatalog.item("daily-istighfar").targetCount)
        assertEquals(1, AzkarCatalog.item("blessing-gratitude").targetCount)
        assertTrue(AzkarCatalog.item("hasbi-seven").disputed)
        assertTrue(AzkarCatalog.item("blessing-gratitude").disputed)
        assertTrue(AzkarCatalog.item("night-ikhlas").disputed)
        assertTrue(AzkarCatalog.item("night-mulk").disputed)
    }

    @Test fun morningEveningTextVariantsFollowAbridgedTarifiEdition() {
        val morning = AzkarCatalog.item("by-you").resolve(AzkarPeriod.Morning)
        val evening = AzkarCatalog.item("by-you").resolve(AzkarPeriod.Evening)
        assertTrue(morning.arabic.endsWith("وَإِلَيْكَ النُّشُورُ"))
        assertTrue(evening.arabic.endsWith("وَإِلَيْكَ الْمَصِيرُ"))
        val thanksMorning = AzkarCatalog.item("blessing-gratitude").resolve(AzkarPeriod.Morning)
        val thanksEvening = AzkarCatalog.item("blessing-gratitude").resolve(AzkarPeriod.Evening)
        assertTrue(thanksMorning.arabic.contains("أَصْبَحَ"))
        assertTrue(thanksEvening.arabic.contains("أَمْسَى"))
    }

    @Test fun nightReadingsAreNotMistakenForEveningDhikr() {
        val night = AzkarCatalog.itemsFor(AzkarPeriod.Night).map { it.id }
        assertEquals(listOf("baqarah-last-two","night-ikhlas","night-mulk"), night)
        assertFalse(AzkarCatalog.itemsFor(AzkarPeriod.Evening).any { it.id == "baqarah-last-two" })
        assertEquals(30, AzkarNightQuran.alMulkVerses.size)
        assertTrue(AzkarNightQuran.alMulkVerses[0].startsWith("تَبَ"))
        assertTrue(AzkarNightQuran.alMulkVerses.last().contains("مَّعِينٍۭ"))
        assertEquals(30, AzkarNightQuran.alMulkArabic.split("﴿").size - 1)
    }
}
