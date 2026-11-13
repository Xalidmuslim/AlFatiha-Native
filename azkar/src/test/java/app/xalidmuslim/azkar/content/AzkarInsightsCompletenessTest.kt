package app.xalidmuslim.azkar.content

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AzkarInsightsCompletenessTest {
    @Test fun everyDhikrHasFullWordAnalysisHeartMeaningAndProvenance() {
        val items = AzkarCatalog.all
        assertTrue(items.size >= 22)
        items.forEach { item ->
            val extra = AzkarExplanationEnrichment.forId(item.id)
            val reading = AzkarContemplationGuide.forId(item.id)
            assertTrue(item.id + ": missing word analysis", reading != null && reading.wordMeanings.size >= 3)
            assertTrue(item.id + ": missing substantive contemplation",
                reading != null && reading.contemplation.length >= 230)
            assertTrue(item.id + ": missing heart meaning",
                reading != null && reading.heartResponse.length >= 110)
            assertTrue(item.id + ": missing benefits", extra.benefits.size >= 2)
            assertTrue(item.id + ": missing source references", extra.references.isNotEmpty() || item.source.isNotBlank())
            assertTrue(item.id + ": missing deep reading", AzkarExplanationEnrichment.deepDiveForId(item.id).length >= 100)
        }
    }

    @Test fun anNasTranslationAndTasbihExplanationDescribeCompleteMeaning() {
        val muawwidhat = AzkarCatalog.item("muawwidhat").resolve(AzkarPeriod.Morning)
        assertTrue(muawwidhat.translation.contains("наущателя, который отступает и скрывается"))
        assertTrue(muawwidhat.translation.contains("нашёптывает в сердца людей"))
        val tasbih = AzkarContemplationGuide.forId("tasbih-hundred")!!
        assertTrue(tasbih.wordMeanings.any { it.contains("несправедливость") })
        assertTrue(tasbih.wordMeanings.any { it.contains("невежество") })
        assertTrue(tasbih.contemplation.contains("хвала"))
    }

    @Test fun disputedReportsRemainExplicitlyMarkedAndNeverPretendToBeHadith() {
        for (id in listOf("hasbi-seven", "blessing-gratitude", "night-ikhlas", "night-mulk")) {
            val item = AzkarCatalog.item(id)
            assertTrue(id, item.disputed)
            assertFalse(id + ": editorial text must not be presented as literal attribution",
                AzkarExplanationEnrichment.forId(id).references.isEmpty() && item.source.isBlank())
        }
    }
}
