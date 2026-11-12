#!/usr/bin/env python3
"""Regression contracts for the Compass navigation hub and balanced dashboard cards.

Static checks complement (but do not replace) Android UI/device testing.
"""
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
src = (root / "app/src/main/java/app/alfatiha/tafsir/MainActivity.java").read_text(encoding="utf-8")
home = src.split("private LinearLayout homeFeature(", 1)[1].split("// --- Малый ширк", 1)[0]
nav = src.split("private void renderPersonalReadingHub(boolean push)", 1)[1].split("private void openContents()", 1)[0]
bottom = src.split("private void updateContentsTabLabel(String type)", 1)[1].split("private void navBtn(", 1)[0]

assert '?"Навигация":' in bottom, "Home tab must be labelled Навигация"
assert '"Задания"' in bottom and '"Содержание"' in bottom, "Keep contextual tasks and course TOCs"
assert 'c.setGravity(Gravity.CENTER_VERTICAL);' in home, "Dashboard tile content must be centered vertically"
assert re.search(r'^\s*t\.setMinLines\(1\);', home, re.MULTILINE), "Short titles must not reserve two lines"
assert not re.search(r'^\s*t\.setMinLines\(2\);', home, re.MULTILINE), "Avoid blank title-to-subtitle gap"
assert 't.setMaxLines(2);' in home and 'st.setMaxLines(2);' in home, "Retain two-line overflow support"
assert 'installHomeCardPressMotion(c)' in home, "Keep existing tile feedback"
assert 'header("Навигация"' in nav, "Navigator must have a clear heading"

required = [
    "Тренажёр викторин", "Все викторины", "Экзамены", "Повторение",
    "Осознанное чтение Аль-Фатихи", "Тайны молитвы", "Малый ширк",
    "Хадисы-кудси", "Азкары", "Медицина Пророка",
    "Практические ситуации", "Словарь молитвы", "Ошибки в молитве",
    "Поиск по материалам", "Прогресс и аналитика",
]
for label in required:
    assert 'personalReadingShortcut("' + label in nav, f"Missing navigator entry: {label}"
for forbidden in ("Сохранённые смыслы", "Сохранённое по малому ширку"):
    assert forbidden not in nav, f"Remove saved-items-centric entry: {forbidden}"
for route in ("renderTaskNavigator(0,true)", "renderQuizCenter(true)",
              "renderMindHub(true)", "renderPrayerSecretsHub(true)",
              "renderMinorShirkHub(true)", "renderHadithQudsiHub(true)",
              "this::openAzkar", "this::openMedicine", "renderSearch(true)"):
    assert route in nav, f"Navigator missing route {route}"
assert 'renderPersonalReadingHub(true)' in src.split('private void openContents()', 1)[1], "Home tab must open hub"

print("Compass global navigation and centered card contracts OK")
