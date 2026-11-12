#!/usr/bin/env python3
"""Regression: navigation lists exercises, dashboard tile content is balanced.

Static layout contracts supplement on-device screenshot inspection.
"""
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
src = (root / "app/src/main/java/app/alfatiha/tafsir/MainActivity.java").read_text(encoding="utf-8")
home = src.split("private LinearLayout homeFeature(", 1)[1].split("// --- Малый ширк", 1)[0]
hub = src.split("private void renderPersonalReadingHub(boolean push)", 1)[1].split("private void openContents()", 1)[0]
dashboard = src.split("private void renderHome(boolean push)", 1)[1].split("private LinearLayout homeFeature(", 1)[0]

assert 'page.addView(kicker("КУРСЫ И КНИГИ"' not in hub, "Remove duplicated course-and-books category"
for title in ("Осознанное чтение Аль-Фатихи", "Тайны молитвы", "Малый ширк",
              "Хадисы-кудси", "Азкары", "Медицина Пророка"):
    assert 'personalReadingShortcut("' + title + '",' not in hub, f"Duplicate home course entry: {title}"
for item in ("Тренажёр викторин", "Все викторины", "Экзамены", "Повторение",
             "Тренировка чтения", "Практические ситуации", "Поиск по материалам",
             "Прогресс и аналитика"):
    assert 'personalReadingShortcut("' + item in hub, f"Important navigation entry lost: {item}"
assert 'header("Навигация"' in hub
for title in ("Осознанное\\nчтение Аль-Фатихи", "Тайны молитвы", "Малый ширк",
              "Хадисы-кудси", "Викторины", "Азкары"):
    assert title in dashboard, f"Home tile removed: {title}"

assert 'c.setGravity(Gravity.CENTER_VERTICAL);' in home, "Card content must be centered vertically"
assert re.search(r'textBlock\.setMinimumHeight\(dp\(', home), "Reserve consistent layout height for text groups"
assert 'textBlock.setGravity(Gravity.TOP|Gravity.START);' in home, "All titles must begin on the same baseline"
assert 't.setMinLines(1);' in home and 't.setMaxLines(2);' in home
assert 'subLp.setMargins(0,dp(2),0,0);' in home, "Title and subtitle must remain close"
assert 'installHomeCardPressMotion(c)' in home
assert '"theme_mode"' in src and 'resolveThemeDark()' in src, "Keep automatic day/night implementation"
print("Compact practice navigator and centered, baseline-aligned home cards: OK")
