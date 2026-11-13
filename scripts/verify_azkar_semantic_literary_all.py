#!/usr/bin/env python3
"""Regression: meaning-preserving Russian Azkar editorial review.

Structural checks cannot prove theological accuracy; Arabic words, individual
hadith meanings and Quranic verses still require editorial comparison.
"""
from pathlib import Path
import re

root=Path(__file__).resolve().parents[1]
catalog=(root/"azkar/src/main/java/app/xalidmuslim/azkar/content/AzkarCatalog.kt").read_text(encoding="utf-8")
night=(root/"azkar/src/main/java/app/xalidmuslim/azkar/content/AzkarNightQuran.kt").read_text(encoding="utf-8")
items=re.findall(r'AzkarContentItem\(\s*id\s*=\s*"([^"]+)"([\s\S]*?)(?=\n        AzkarContentItem\(|\n    \)\s*\n)',catalog)
arabics=re.findall(r'^\s*arabic\s*=\s*"((?:\\.|[^"\\])*)"',catalog,re.M)
translations=re.findall(r'^\s*translation\s*=\s*"((?:\\.|[^"\\])*)"',catalog,re.M)
assert len(items)==22
assert len(arabics)==len(translations)==24
assert len(set(x for x,_ in items))==22
assert all(x.strip() for x in translations)

hashval=2166136261
for ar in arabics:
    data=ar.encode("utf-16-le")
    for i in range(0,len(data),2):
        codepoint=data[i] | data[i+1]<<8
        hashval=((hashval^codepoint)*16777619)&0xffffffff
    hashval=((hashval^10)*16777619)&0xffffffff
assert hashval==0xf143509e, f"Arabic text was changed: {hashval:08x}"

by_id={id:block for id,block in items}
def translations_of(id):
    return re.findall(r'^\s*translation\s*=\s*"((?:\\.|[^"\\])*)"',by_id[id],re.M)
health=translations_of("health-three")[0]
for part in ("теле", "слух", "зрени", "здоровье", "благополучие", "Нет божества"):
    assert part in health, f"Health petition lost: {part}"
assert health.count("О Аллах") == 3, "Keep all three petitions separately"
afiyah=translations_of("afiyah")[0]
for part in ("прощен", "благополуч", "вероисповедании", "семь", "имуществ",
             "сокрой", "страх", "спереди", "сзади", "справа", "слева",
             "сверху", "снизу"):
    assert part in afiyah.lower(), f"Afiyah petition lost meaning: {part}"
assert "верю в Твоё обещание" in translations_of("sayyid-istighfar")[0]
assert "признаю" in translations_of("sayyid-istighfar")[0].lower()
assert "Вечно Живой" in translations_of("hayy-qayyum")[0]
assert "ни на мгновение ока" in translations_of("hayy-qayyum")[0]
assert "зла шайтана" in translations_of("fatir")[0]
assert len(translations_of("kingdom"))==len(translations_of("by-you"))==len(translations_of("blessing-gratitude"))==2
assert "вечер" in translations_of("blessing-gratitude")[1].lower()
assert "воскресени" in translations_of("by-you")[0].lower()
assert "возвращен" in translations_of("by-you")[1].lower()
assert "изменяй смысл" not in catalog
for key in ("muawwidhat","baqarah-last-two","night-ikhlas"):
    assert all(t.strip() for t in translations_of(key)), f"Quran translation missing: {key}"
for name in ("alMulkVerses", "alMulkRussianVerses"):
    values=night.split(f"val {name}: List<String> = listOf(",1)[1].split("\n    )",1)[0]
    assert values.count('        "') == 30, f"Al-Mulk should retain all 30 entries: {name}"
print("Azkar linguistic guards passed: 22 entries / 24 variants / original Arabic unchanged / 30 Al-Mulk verses")
