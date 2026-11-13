#!/usr/bin/env python3
"""Regression checks for non-prescriptive contemplation of all existing Azkar."""
from pathlib import Path
import re
root=Path(__file__).resolve().parents[1]
base=root/"azkar/src/main/java/app/xalidmuslim/azkar"
catalog=(base/"content/AzkarCatalog.kt").read_text(encoding="utf-8")
extras=(base/"content/AzkarExplanationEnrichment.kt").read_text(encoding="utf-8")
guide=(base/"content/AzkarContemplationGuide.kt").read_text(encoding="utf-8")
sheet=(base/"ui/reading/AzkarReaderSheets.kt").read_text(encoding="utf-8")
ids=set(re.findall(r'^\s*id = "([^"]+)"',catalog,re.M))
guides=re.findall(r'^\s*"([^"]+)" to AzkarContemplation\(',guide,re.M)
assert len(ids)==22 and len(guides)==22
assert len(set(guides))==22 and set(guides)==ids, (ids-set(guides),set(guides)-ids)
assert guide.count('wordMeanings = listOf(')==22, "All Azkar need separate word definitions"
assert guide.count('contemplation = "')==22, "All Azkar need expansive reflection"
assert guide.count('heartResponse = "')==22, "All Azkar need a careful description of heart response"
assert '«Субхана-Ллах»' in guide and 'несправедливост' in guide and 'невежеств' in guide, "Give concrete examples of what Allah is exalted above"
assert '«аль-Ханнас»' in guide and 'отступает' in guide, "Explain An-Nas key word"
assert '«Мидад»' in guide, "Explain the unlimited measure of words"
assert 'title = "О чём размышлять"' in sheet
assert 'title = "Что означают слова"' in sheet
assert 'title = "Состояние сердца"' in sheet
assert 'AzkarContemplationGuide.forId(item.id)' in sheet
assert 'title = "Как применять"' not in sheet
assert 'practicalApplication' not in extras and 'practicalApplication' not in sheet
assert 'heartFocus' not in extras and 'heartFocus' not in sheet
assert 'AzkarInsightTone.Practice' not in sheet
assert 'ограничься двумя' not in guide.lower()
assert 'вспомни 1–2' not in extras.lower()
assert 'после каждого повторения удерживай один смысл' not in extras.lower()
assert 'от зла наущателя, который отступает и скрывается' in catalog, "Previously agreed An-Nas must be applied"
assert 'который нашёптывает в сердца людей' in catalog, "An-Nas must retain semantics of waswasa"
assert catalog.count('id = "muawwidhat"')==1
ar=re.findall(r'^\s*arabic\s*=\s*"((?:\\.|[^"\\])*)"',catalog,re.M)
h=2166136261
for entry in ar:
    units=entry.encode("utf-16-le")
    for i in range(0,len(units),2):
        h=((h ^ (units[i]|(units[i+1]<<8)))*16777619)&0xffffffff
    h=((h^10)*16777619)&0xffffffff
assert len(ar)==24 and h==0xf143509e, f"Arabic text changed: {h:08x}"
night=(base/"content/AzkarNightQuran.kt").read_text(encoding="utf-8")
for name in ("alMulkVerses","alMulkRussianVerses"):
    body=night.split(f"val {name}: List<String> = listOf(",1)[1].split("\n    )",1)[0]
    assert body.count('        "')==30
print("All 22 Azkar contemplations present; no practice prompts; An-Nas revised; Arabic unchanged")
