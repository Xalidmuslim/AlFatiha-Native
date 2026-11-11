#!/usr/bin/env python3
"""Layout contract: al-Mulk has 30 Arabic/translation pairs inside the reader card.
The existing scripture payload, progress, and alternative reading modes stay intact.
"""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
screen=(root/"azkar/src/main/java/app/xalidmuslim/azkar/ui/reading/AzkarGoldenReadingScreen.kt").read_text()
quran=(root/"azkar/src/main/java/app/xalidmuslim/azkar/content/AzkarNightQuran.kt").read_text()
catalog=(root/"azkar/src/main/java/app/xalidmuslim/azkar/content/AzkarCatalog.kt").read_text()
assert 'item.id == "night-mulk"' in screen, "al-Mulk needs its own reader branch"
assert 'AzkarMulkVerseText(' in screen, "al-Mulk uses verse-by-verse renderer"
assert 'AzkarNightQuran.alMulkPairs.forEach' in screen, "all 30 source pairs shown in order"
assert 'AzkarReadingTestTags.MulkVersePrefix' in screen, "verses must be independently addressable"
assert 'showTranslation && item.id != "muawwidhat" && item.id != "night-mulk"' in screen, "no duplicate global Russian block"
assert 'if (showTranslation)' in screen, "global translation toggle must work for each ayah"
assert 'AzkarNightQuran.alMulkArabic' in catalog and 'AzkarNightQuran.alMulkRussian' in catalog, "data for copy/export must remain unchanged"
assert 'val alMulkPairs:' in quran and 'require(alMulkVerses.size == 30)' in quran
assert 'AzkarDhikrCard(' in (root/"azkar/src/main/java/app/xalidmuslim/azkar/ui/reading/AzkarListReadingScreen.kt").read_text(), "list mode must reuse the card's renderer"
print("Al-Mulk UI regression contract: verse-by-verse, no double translation, preserved data")
