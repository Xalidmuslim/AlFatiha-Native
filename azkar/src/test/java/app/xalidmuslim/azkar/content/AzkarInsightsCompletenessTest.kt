package app.xalidmuslim.azkar.content

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AzkarInsightsCompletenessTest {
    @Test fun everyDhikrHasMeaningBenefitsApplicationAndProvenance() {
        val items = AzkarCatalog.all
        assertTrue(items.size >= 22)
        items.forEach { item ->
            val extra = AzkarExplanationEnrichment.forId(item.id)
            assertTrue(item.id + ": missing detailed meanings", extra.keyMeanings.size >= 3)
            assertTrue(item.id + ": missing heart reflection", !extra.heartFocus.isNullOrBlank())
            assertTrue(item.id + ": missing benefits", extra.benefits.size >= 2)
            assertTrue(item.id + ": missing practical application", !extra.practicalApplication.isNullOrBlank())
            assertTrue(item.id + ": missing source references", extra.references.isNotEmpty() || item.source.isNotBlank())
            assertTrue(item.id + ": missing deep reading", AzkarExplanationEnrichment.deepDiveForId(item.id).length >= 100)
        }
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
