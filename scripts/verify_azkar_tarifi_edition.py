#!/usr/bin/env python3
"""Source-contract regression for the abridged Tarifi morning/evening/night inventory.
Not a hadith-authenticity verdict or a replacement for visual Arabic collation.
"""
import re
from pathlib import Path

root = Path(__file__).resolve().parents[1]
content = (root/"azkar/src/main/java/app/xalidmuslim/azkar/content/AzkarCatalog.kt").read_text()
period = (root/"azkar/src/main/java/app/xalidmuslim/azkar/content/AzkarPeriod.kt").read_text()
quran = (root/"azkar/src/main/java/app/xalidmuslim/azkar/content/AzkarNightQuran.kt").read_text()
screen = (root/"azkar/src/main/java/app/xalidmuslim/azkar/ui/reading/AzkarGoldenReadingScreen.kt").read_text()
items = re.split(r"(?=        AzkarContentItem\()", content)[1:]
assert len(items)==22, f"Expected 22 distinct items after collation, got {len(items)}"
ids = re.findall(r'\bid = "([^"]+)"', content)
assert len(ids)==len(set(ids))==22, "Stable IDs must be unique and never renamed"
base = {"sayyid-istighfar","tahlil-ten","tahlil-hundred","tasbih-hundred","kingdom",
        "by-you","bismillah-protection","afiyah","pleased","hayy-qayyum","fatir",
        "muawwidhat","creation-count","fitrah","perfect-words","baqarah-last-two"}
assert base.issubset(ids), "Every original progress/ID must be preserved"
assert "Night" in period, "Night is a third, distinct reading period"
assert sum("periods = bothPeriods()" in x or "periods = setOf(AzkarPeriod.Morning)" in x for x in items) == 18
assert sum("periods = bothPeriods()" in x or "periods = setOf(AzkarPeriod.Evening)" in x for x in items) == 17
for tag, count, periods in [
    ("health-three",3,["Morning","Evening"]),
    ("hasbi-seven",7,["Morning","Evening"]),
    ("daily-istighfar",100,["Morning","Evening"]),
    ("blessing-gratitude",1,["Morning","Evening"]),
    ("night-ikhlas",1,["Night"]),
    ("night-mulk",1,["Night"]),
    ("baqarah-last-two",1,["Night"]),
]:
    seg=next((x for x in items if f'id = "{tag}"' in x),None)
    assert seg is not None,f"Missing {tag}"
    assert f"targetCount = {count}" in seg,f"Wrong repetitions for {tag}"
    assert all(("bothPeriods()" in seg if p in ("Morning","Evening") and len(periods)==2 else f"AzkarPeriod.{p}" in seg) for p in periods),f"Wrong period for {tag}"
assert 'id = "blessing-gratitude"' in content and "eveningVariant = AzkarTextVariant(" in content
assert 'id = "by-you"' in content
by_you = next(x for x in items if 'id = "by-you"' in x)
assert by_you.index("وَإِلَيْكَ النُّشُورُ") < by_you.index("eveningVariant")
assert by_you.index("وَإِلَيْكَ الْمَصِيرُ") > by_you.index("eveningVariant")
assert "PeriodNight" in screen and '"☾ Ночь"' in screen
assert 'AzkarPeriod.Night -> "Ночные"' in screen
verses=re.findall(r'^\s*"(.+?)",\s*$',quran,re.MULTILINE)
assert len(verses)==30, f"Al-Mulk needs exactly 30 verses, got {len(verses)}"
assert verses[0].startswith("تَبَ") and verses[-1].endswith("مَّعِينٍۭ")
assert "Read" not in quran and all(len(v)>20 for v in verses)
assert 'disputed = true' in next(x for x in items if 'id = "hasbi-seven"' in x)
assert 'disputed = true' in next(x for x in items if 'id = "blessing-gratitude"' in x)
assert 'disputed = true' in next(x for x in items if 'id = "night-ikhlas"' in x)
assert 'disputed = true' in next(x for x in items if 'id = "night-mulk"' in x)
assert len(re.findall(r'^\s*"[^"\n]+",\s*$',quran,re.MULTILINE))==60, "Need 30 Arabic and 30 literary Russian verses"\nprint("Azkar edition audit: 22 stable IDs; 18 morning; 17 evening; 3 night; 30 Arabic/30 Russian verses")\n