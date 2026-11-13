#!/usr/bin/env python3
"""Guard sourced prayer preface: sincere salah and liberation from sinful habits."""
import json
import re
from pathlib import Path

root = Path(__file__).resolve().parents[1]
intro = json.loads((root / "app/src/main/assets/prayer_intro.json").read_text(encoding="utf-8"))
source = (root / "app/src/main/java/app/alfatiha/tafsir/MainActivity.java").read_text(encoding="utf-8")
assert len(intro) == 8, "Original six preface cards + two topical cards, without changing the course"
titles = [entry["title"] for entry in intro]
assert titles[:2] == [
    "Молитва — не набор движений",
    "Тело и сердце должны поклоняться вместе",
]
assert titles[4:] == [
    "Почему положения молитвы постоянно меняются",
    "Не нужно переводить всё в голове во время намаза",
    "Изучать поэтапно — не значит ограничивать намаз одним смыслом",
    "Главный критерий курса",
], "Original preface content and order must be intact"
new = intro[2:4]
full = " ".join(e["title"] + " " + e["text"] for e in new)
assert all(len(e["text"]) >= 900 for e in new), "New preface cards must be substantial"
for required in [
    "Абу Хурайр", "вор", "молитв", "29:45",
    "9778", "2560", "Шуайб", "побуждени",
    "пока", "покаяни", "Судн", "413", "864",
    "аль-Каййим", "не является цитат",
]:
    assert required.lower() in full.lower(), required
assert "гарантированно" not in full.lower()
assert "автоматически" not in full.lower()
assert not re.search(r"[\u0600-\u06FF]", full), "User requested no Arabic quotation or script"
assert 'JSONArray a=arr("prayer_intro.json");' in source, "Keep existing preface renderer"
assert 'addParagraphs(box,o.optString("text"),14.7f)' in source
assert '"Перейти к первому шагу"' in source
print("Prayer preface: two sourced literary sections inserted; six legacy sections preserved")
