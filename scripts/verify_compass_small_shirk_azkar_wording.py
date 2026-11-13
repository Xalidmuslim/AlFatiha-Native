#!/usr/bin/env python3
"""Regression for the requested two-line scoped correction only.

Checks that the shared six-card layout remains intact and only the
single-line 'Малый ширк' text group receives a small upward adjustment.
"""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
main = (root / "app/src/main/java/app/alfatiha/tafsir/MainActivity.java").read_text(encoding="utf-8")
catalog = (root / "azkar/src/main/java/app/xalidmuslim/azkar/content/AzkarCatalog.kt").read_text(encoding="utf-8")

start = main.index("private LinearLayout homeFeature(")
end = main.index("// --- Малый ширк", start)
card = main[start:end]
assert 'if ("Малый ширк".equals(title)) {' in card
assert 'textBlock.setTranslationY(-dp(8));' in card, "Move only the small shirk card labels up by eight dp"
assert card.count("setTranslationY(") == 1, "Do not move other cards or icons"
assert 'FrameLayout top=new FrameLayout(this)' in card
assert 'textBlock.setGravity(Gravity.CENTER);' in card
assert 't.setGravity(Gravity.CENTER);' in card
assert 'st.setGravity(Gravity.CENTER);' in card

expected = ("Пречист Аллах, и Ему принадлежит хвала — по числу Его творений, "
            "в меру Его довольства, по весу Его Трона и по количеству чернил, "
            "необходимых для записи Его слов.")
assert expected in catalog, "Use the agreed literary translation verbatim"
assert "чернил, потребных" not in catalog, "Do not retain the awkward expression"

print("Only Small Shirk labels raised; agreed Azkar wording present")
