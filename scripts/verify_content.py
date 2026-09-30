#!/usr/bin/env python3
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app" / "src" / "main" / "assets"
MAIN = ROOT / "app" / "src" / "main" / "java" / "app" / "alfatiha" / "tafsir" / "MainActivity.java"

errors = []

def fail(msg):
    errors.append(msg)

# 1. Every JSON asset must parse.
parsed = {}
for path in sorted(ASSETS.glob("*.json")):
    try:
        parsed[path.name] = json.loads(path.read_text(encoding="utf-8"))
    except Exception as e:
        fail(f"{path.name}: invalid JSON: {e}")

# 2. Core course contracts.
mind = parsed.get("mind_data.json", [])
heart = parsed.get("mind_heart.json", [])
if len(mind) != 8:
    fail(f"mind_data.json: expected 8 parts, got {len(mind)}")
if len(heart) != 8:
    fail(f"mind_heart.json: expected 8 parts, got {len(heart)}")

prayer = parsed.get("prayer_secrets.json", [])
ids = [x.get("id") for x in prayer if isinstance(x, dict)]
if len(ids) != len(set(ids)):
    fail("prayer_secrets.json: duplicate ids")
if len(prayer) < 15:
    fail(f"prayer_secrets.json: unexpectedly short ({len(prayer)})")

required_word_fields = {"phrase", "meaning", "heart", "detail", "example", "application"}
word_count = 0
for lesson in prayer:
    if not isinstance(lesson, dict):
        continue
    for word in lesson.get("word_map") or []:
        word_count += 1
        missing = [k for k in required_word_fields if not str(word.get(k, "")).strip()]
        if missing:
            fail(f"prayer word {lesson.get('id')} / {word.get('phrase')}: missing {missing}")
if word_count < 40:
    fail(f"prayer_secrets.json: expected >=40 detailed word/phrase entries, got {word_count}")

# 3. Glossary links must resolve to actual prayer lesson ids.
glossary = parsed.get("prayer_glossary.json", [])
id_set = set(ids)
for item in glossary:
    for link in item.get("links") or []:
        if link not in id_set:
            fail(f"prayer_glossary.json: unknown lesson link {link!r}")

# 4. Removed UX patterns must not reappear in user-visible code/data.
forbidden = [
    "Одна мысль на намаз",
    "На ближайшую молитву удерживайте только одну мысль",
    "ПРАКТИКА В БЛИЖАЙШЕМ НАМАЗЕ",
    "Практика в намазе",
]
search_files = [MAIN] + sorted(ASSETS.glob("*.json"))
for path in search_files:
    text = path.read_text(encoding="utf-8")
    for phrase in forbidden:
        if phrase in text:
            fail(f"{path.relative_to(ROOT)}: stale UX phrase {phrase!r}")

# 5. Literal screens passed to clear() must be restorable via Back.
src = MAIN.read_text(encoding="utf-8")
cleared = set(re.findall(r'clear\("([A-Za-z0-9_]+)"', src))
restored = set(re.findall(r'case"([A-Za-z0-9_]+)"\s*:', src))
# Dialog-only/non-history screens can be explicitly exempted here.
exempt = set()
missing_restore = sorted(cleared - restored - exempt)
if missing_restore:
    fail("MainActivity.java: screens missing from restore(): " + ", ".join(missing_restore))

# 6. Deleted prayer practice screens must stay deleted.
for symbol in ["renderPrayerPracticeHub", "renderPrayerBefore", "renderPrayerFlowPractice", "renderPrayerAfter", "renderMindPractice"]:
    if symbol in src:
        fail(f"MainActivity.java: stale removed symbol {symbol}")

if errors:
    print("CONTENT/NAVIGATION VERIFY: FAIL")
    for e in errors:
        print(" -", e)
    raise SystemExit(1)

print(f"CONTENT/NAVIGATION VERIFY: PASS ({len(parsed)} JSON assets, {word_count} detailed prayer entries, {len(cleared)} restorable screens)")
