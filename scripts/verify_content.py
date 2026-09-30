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

# 4. Deep prayer wording contracts for tashahhud and final sitting.
prayer_by_id = {x.get("id"): x for x in prayer if isinstance(x, dict)}
for lesson_id, minimum in [("tashahhud", 20), ("salawat_dua", 45)]:
    lesson = prayer_by_id.get(lesson_id)
    if not lesson:
        fail(f"prayer_secrets.json: missing {lesson_id}")
        continue
    wm = lesson.get("word_map") or []
    if len(wm) < minimum:
        fail(f"{lesson_id}: expected at least {minimum} word-by-word entries, got {len(wm)}")
    for i, word in enumerate(wm):
        if not str(word.get("arabic", "")).strip():
            fail(f"{lesson_id}[{i}]: missing Arabic form")
        if not str(word.get("group", "")).strip():
            fail(f"{lesson_id}[{i}]: missing internal group")

# 5. Religious assessment datasets must never ship with an empty source.

source_datasets = [
    "quiz_data.json", "expert_data.json", "multi_data.json", "match_data.json",
    "hadith_data.json", "free_data.json", "mind_check.json", "mind_exam.json",
    "mind_mistakes.json", "mind_life.json", "prayer_check.json", "prayer_heart_errors.json",
]
for name in source_datasets:
    data = parsed.get(name, [])
    for i, item in enumerate(data):
        if not isinstance(item, dict):
            fail(f"{name}[{i}]: expected object")
            continue
        raw = (
            item.get("src")
            or item.get("source")
            or item.get("sources")
            or item.get("sourceList")
            or item.get("reference")
        )
        if isinstance(raw, list):
            ok = any(bool(str(x).strip()) if not isinstance(x, dict) else bool(str(x.get("label", "")).strip()) for x in raw)
        else:
            ok = bool(str(raw or "").strip())
        if not ok:
            fail(f"{name}[{i}]: missing source")

# 6. Audited core sources must not fall back to vague placeholders.
audited_core = [
    "mind_qayyim_deep.json", "mind_connections.json", "mind_mistakes.json",
    "mind_life.json", "mind_check.json", "mind_exam.json",
    "prayer_secrets.json", "prayer_heart_errors.json", "prayer_check.json",
]
vague_source_phrases = [
    "материалы курса", "Завк ас-саля", "смысл построен",
    "практический вывод курса", "общий замысел",
]
for name in audited_core:
    raw = (ASSETS / name).read_text(encoding="utf-8")
    low = raw.lower()
    for phrase in vague_source_phrases:
        if phrase.lower() in low:
            fail(f"{name}: vague source phrase remains: {phrase!r}")

# 7. Single-choice checks should not teach a fixed answer position.
for name in ["mind_check.json", "mind_exam.json", "prayer_check.json"]:
    counts = [0, 0, 0, 0]
    total_single = 0
    for item in parsed.get(name, []):
        if not isinstance(item, dict):
            continue
        answer = item.get("a", item.get("correct"))
        opts = item.get("opts", item.get("options"))
        if isinstance(answer, int) and isinstance(opts, list) and len(opts) == 4:
            if 0 <= answer < 4:
                counts[answer] += 1
                total_single += 1
    if total_single >= 8 and max(counts) > (total_single + 1) // 2:
        fail(f"{name}: correct-answer position is too predictable: {counts}")

# 8. Removed UX patterns must not reappear in user-visible code/data.


forbidden = [
    "Одна мысль на намаз",
    "На ближайшую молитву удерживайте только одну мысль",
    "ПРАКТИКА В БЛИЖАЙШЕМ НАМАЗЕ",
    "Практика в намазе",
]
search_files = [MAIN] + sorted(ASSETS.glob("*.json"))
for path in search_files:
    text = path.read_text(encoding="utf-8")
    low = text.lower()
    for phrase in forbidden:
        if phrase.lower() in low:
            fail(f"{path.relative_to(ROOT)}: stale UX phrase {phrase!r}")

# 9. Literal screens passed to clear() must be restorable via Back.
src = MAIN.read_text(encoding="utf-8")
cleared = set(re.findall(r'clear\("([A-Za-z0-9_]+)"', src))
restored = set(re.findall(r'case"([A-Za-z0-9_]+)"\s*:', src))
# Dialog-only/non-history screens can be explicitly exempted here.
exempt = set()
missing_restore = sorted(cleared - restored - exempt)
if missing_restore:
    fail("MainActivity.java: screens missing from restore(): " + ", ".join(missing_restore))

# 10. Deleted prayer practice screens must stay deleted.
for symbol in ["renderPrayerPracticeHub", "renderPrayerBefore", "renderPrayerFlowPractice", "renderPrayerAfter", "renderMindPractice"]:
    if symbol in src:
        fail(f"MainActivity.java: stale removed symbol {symbol}")

if errors:
    print("CONTENT/NAVIGATION VERIFY: FAIL")
    for e in errors:
        print(" -", e)
    raise SystemExit(1)

print(f"CONTENT/NAVIGATION VERIFY: PASS ({len(parsed)} JSON assets, {word_count} detailed prayer entries, {len(cleared)} restorable screens)")
