package app.xalidmuslim.azkar.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression for al-Mulk reading: each Arabic ayah is immediately followed by
 * its own Russian translation. The existing two source arrays are authoritative.
 */
class AzkarMulkVersePairsTest {
    @Test fun everyAyahHasExactlyOneTranslationInTheSameOrder() {
        val pairs = AzkarNightQuran.alMulkPairs
        assertEquals(30, pairs.size)
        pairs.forEachIndexed { index, pair ->
            assertEquals(index + 1, pair.number)
            assertEquals(AzkarNightQuran.alMulkVerses[index], pair.arabic)
            assertEquals(AzkarNightQuran.alMulkRussianVerses[index], pair.translation)
            assertTrue(pair.arabic.isNotBlank())
            assertTrue(pair.translation.isNotBlank())
        }
    }

    @Test fun theFirstAndLastVersesAreNotSwapped() {
        val pairs = AzkarNightQuran.alMulkPairs
        assertTrue(pairs.first().arabic.startsWith("تَبَ"))
        assertTrue(pairs.first().translation.startsWith("Благословен"))
        assertTrue(pairs.last().arabic.contains("مَّعِينٍ"))
        assertTrue(pairs.last().translation.contains("воду"))
    }

    @Test fun existingSurahPayloadIsPreservedForExportAndReaderActions() {
        val entry = AzkarCatalog.item("night-mulk").resolve(AzkarPeriod.Night)
        assertEquals(AzkarNightQuran.alMulkArabic, entry.arabic)
        assertEquals(AzkarNightQuran.alMulkRussian, entry.translation)
    }
}
