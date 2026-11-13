#!/usr/bin/env python3
"""Regression: rounded navigation brand icon, Arabic text untouched, full translations.

This is a structural and data-integrity check, not theological certification.
"""
from pathlib import Path
import re

root=Path(__file__).resolve().parents[1]
ui=(root/"app/src/main/java/app/alfatiha/tafsir/MainActivity.java").read_text(encoding="utf-8")
corpus=(root/"azkar/src/main/java/app/xalidmuslim/azkar/content/AzkarCatalog.kt").read_text(encoding="utf-8")
mulk=(root/"azkar/src/main/java/app/xalidmuslim/azkar/content/AzkarNightQuran.kt").read_text(encoding="utf-8")
chrome=ui.split("private void appTop()",1)[1].split("private void renderHome(boolean push)",1)[0]
assert '"personalReadingHub".equals(current.type)' in chrome, "Navigator icon must use the rounded home-style icon container"
assert "mark.setClipToOutline(true);" in chrome, "Round icon must clip artwork to the radius"
assert "mark.setBackground(surfaceBg(" in chrome

item_ids=re.findall(r'^\s*id = "([^"]+)"', corpus, re.M)
arabics=re.findall(r'^\s*arabic\s*=\s*"((?:\\.|[^"\\])*)"', corpus, re.M)
translations=re.findall(r'^\s*translation\s*=\s*"((?:\\.|[^"\\])*)"', corpus, re.M)
assert len(item_ids)==22, f"Expected 22 catalog entries, got {len(item_ids)}"
assert len(arabics)==len(translations)==24, "All 24 Arabic variants must have complete translations"
assert all(t.strip() and len(t)>10 for t in translations), "Translations cannot be empty/truncated"

# FNV-1a of UTF-16 code units of every Arabic variant + separator (unchanged input corpus).
hashval=2166136261
for arabic in arabics:
    raw=arabic.encode("utf-16-le")
    for i in range(0,len(raw),2):
        unit=raw[i] | raw[i+1]<<8
        hashval=((hashval ^ unit)*16777619)&0xffffffff
    hashval=((hashval ^ 10)*16777619)&0xffffffff
assert hashval==0xf143509e, f"Arabic original modified: {hashval:08x}"
assert 'данному Тебе обещанию' not in corpus, "Distinguish Allah's promise from human promise"
assert 'верю в Твоё обещание' in corpus, "Sayyid al-Istighfar must convey faith in Allah's promise"
assert 'в моём вероисповедании' in corpus, "Afiyah must preserve the faith-specific plea"
assert 'Вечно Живой' in corpus, "Preserve the divine name al-Hayy accurately"
assert 'Предоставляй меня' not in corpus, "Imperative must be a negated plea, not a positive instruction"
assert 'Слова и глубокий смысл' in ui, "Retain home descriptions"

# Don't conflate Al-Mulk with prose edits. Keep each verse and translation in order.
arblock=mulk.split("val alMulkVerses: List<String> = listOf(",1)[1].split("\n    )",1)[0]
rublock=mulk.split("val alMulkRussianVerses: List<String> = listOf(",1)[1].split("\n    )",1)[0]
assert arblock.count('        "')==30 and rublock.count('        "')==30, "30 Al-Mulk verses and 30 translations mandatory"
print("Rounded navigator and 24 complete non-truncated translations with stable Arabic corpus: OK")
