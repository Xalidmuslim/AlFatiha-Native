#!/usr/bin/env python3
"""Review quality gate for native Azkar catalog; not a substitute for printed-book collation."""
from pathlib import Path
import re

root=Path(__file__).resolve().parents[1]
catalog=(root/"azkar/src/main/java/app/xalidmuslim/azkar/content/AzkarCatalog.kt").read_text()
quran=(root/"azkar/src/main/java/app/xalidmuslim/azkar/content/AzkarNightQuran.kt").read_text()
translit=(root/"azkar/src/main/java/app/xalidmuslim/azkar/content/AzkarTransliteration.kt").read_text()
notes=(root/"docs/AZKAR_AMAL_2021_TEXT_AUDIT.md").read_text()
items=catalog.split("        AzkarContentItem(")[1:]
assert len(items)==22, f"Expected 22 unique items, got {len(items)}"
ids=[re.search(r'\bid = "([^"]+)"',x).group(1) for x in items]
assert len(set(ids))==22, "Never collide stable progress keys"
assert sum("periods = bothPeriods()" in x or "periods = setOf(AzkarPeriod.Morning)" in x for x in items)==18
assert sum("periods = bothPeriods()" in x or "periods = setOf(AzkarPeriod.Evening)" in x for x in items)==17
assert sum("periods = setOf(AzkarPeriod.Night)" in x for x in items)==3

def block(id):
    return next(x for x in items if f'id = "{id}"' in x)

assert "periods = bothPeriods()" in block("tahlil-hundred")
assert "не устанавливает отдельной вечерней сунны" in block("tahlil-hundred")
assert "воскресение" in block("by-you")
assert "возвращение" in block("by-you")
assert "не делаем различий" in block("baqarah-last-two")
assert "translation = AzkarNightQuran.alMulkRussian" in block("night-mulk")
assert "не дословный перевод" not in block("night-mulk")
assert "печатного издания" in notes and "НЕ доказана" in notes
assert "Русский текст — самостоятельный литературный" in (root/"azkar/src/main/java/app/xalidmuslim/azkar/ui/reading/AzkarReaderSheets.kt").read_text()

# Text aligned to 30 Arabic and 30 independent Russian verses.
arabic_section=quran.split("val alMulkVerses:")[1].split("val alMulkRussianVerses:")[0]
russian_section=quran.split("val alMulkRussianVerses:")[1].split("val alMulkRussian:")[0]
verses_ar=re.findall(r'^\s*"([^"\n]+)",\s*$',arabic_section,re.MULTILINE)
verses_ru=re.findall(r'^\s*"([^"\n]+)",\s*$',russian_section,re.MULTILINE)
assert len(verses_ar)==len(verses_ru)==30
assert all(len(x)>20 and not re.search(r'[A-Za-z]',x) for x in verses_ar)
assert all(len(x)>25 and re.search(r'[А-Яа-я]',x) for x in verses_ru)
assert verses_ar[0].startswith("تَبَ") and verses_ar[-1].endswith("مَّعِينٍۭ")
assert verses_ru[-1].endswith("воду?»")

# Ensure there are no invisible replacement-character corruptions in the recitation text.
for value in (catalog,quran,translit):
    assert "\ufffd" not in value
for id in ("health-three","hasbi-seven","daily-istighfar","blessing-gratitude","night-ikhlas"):
    assert f'"{id}" to ' in translit, f"Missing Russian reading aid for {id}"
# Detect grossly unvocalized newly entered phrases without asserting false proof of exact tashkeel.
for id in ("health-three","hasbi-seven","daily-istighfar","blessing-gratitude","night-ikhlas"):
    arabic=re.search(r'arabic = "([^"\n]+)"',block(id)).group(1)
    vowels=sum(arabic.count(v) for v in "ًٌٍَُِّْ")
    assert vowels >= 8, f"Insufficient Arabic diacritics in {id}"
print("Amal draft integrity: 22 stable IDs, 18/17/3 inventory, 30 aligned Quran verses, no replacements")
