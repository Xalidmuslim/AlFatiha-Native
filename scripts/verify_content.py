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

# Embedded Medicine of the Prophet module must retain the complete structured book.
MEDICINE_ASSETS = ROOT / "medicine" / "src" / "main" / "assets"
MEDICINE_SOURCE = ROOT / "medicine" / "src" / "main" / "java" / "com" / "xalid" / "meditsinaproroka" / "nativeapp"
medicine_book_path = MEDICINE_ASSETS / "book.json"
if not medicine_book_path.is_file():
    fail("medicine: missing embedded book.json")
else:
    try:
        medicine_book = json.loads(medicine_book_path.read_text(encoding="utf-8"))
        medicine_expected = {
            "chapters": (len(medicine_book.get("chapters", [])), 111),
            "topics": (len(medicine_book.get("topics", [])), 15),
            "remedies": (len(medicine_book.get("remedies", [])), 98),
            "treatments": (len(medicine_book.get("treatments", [])), 39),
        }
        for label, (actual, expected) in medicine_expected.items():
            if actual != expected:
                fail(f"medicine: expected {expected} {label}, got {actual}")
        if medicine_book.get("stats", {}).get("sourceWords") != 76149:
            fail("medicine: source word-count contract changed")
    except Exception as e:
        fail(f"medicine: invalid embedded book.json: {e}")

for required in [
    "App.kt", "MainActivity.kt", "Models.kt", "Reader.kt", "Routes.kt",
    "Screens.kt", "Store.kt", "Theme.kt", "Utils.kt", "WebDesign.kt", "MedicineSearchBridge.kt", "MedicineRuntimeWarmup.kt",
]:
    if not (MEDICINE_SOURCE / required).is_file():
        fail(f"medicine: missing native source {required}")

# 1. Every JSON asset must parse.
parsed = {}
for path in sorted(ASSETS.glob("*.json")):
    try:
        parsed[path.name] = json.loads(path.read_text(encoding="utf-8"))
    except Exception as e:
        fail(f"{path.name}: invalid JSON: {e}")

# 1.5. Embedded Hadith Qudsi collection is fixed to the reviewed corpus.
qudsi = parsed.get("hadith_qudsi.json", [])
if len(qudsi) != 44:
    fail(f"hadith_qudsi.json: expected exactly 44 hadiths, got {len(qudsi)}")
else:
    numbers = [item.get("number") for item in qudsi if isinstance(item, dict)]
    if numbers != list(range(1, 45)):
        fail("hadith_qudsi.json: numbering must be continuous 1..44")
    sahih_count = sum(1 for item in qudsi if isinstance(item, dict) and item.get("section") == "Сахих")
    hasan_count = sum(1 for item in qudsi if isinstance(item, dict) and item.get("section") == "Хасан")
    if sahih_count != 32:
        fail(f"hadith_qudsi.json: expected 32 sahih entries, got {sahih_count}")
    if hasan_count != 12:
        fail(f"hadith_qudsi.json: expected 12 hasan entries, got {hasan_count}")
    for i, item in enumerate(qudsi):
        if not isinstance(item, dict):
            fail(f"hadith_qudsi.json[{i}]: expected object")
            continue
        for key in ["number", "title", "text", "source", "grade", "section"]:
            if not str(item.get(key, "")).strip():
                fail(f"hadith_qudsi.json[{i}]: missing {key}")

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
    "alfatiha_medium.json", "prayer_medium.json", "prayer_hard.json",
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

# 6. New difficulty levels: size, structure, balanced answers and hard-option quality.
level_files = {
    "alfatiha_medium.json": 50,
    "expert_data.json": 50,
    "prayer_check.json": 50,
    "prayer_medium.json": 50,
    "prayer_hard.json": 50,
}
seen_level_questions = {}
for name, expected in level_files.items():
    data = parsed.get(name, [])
    if len(data) < expected:
        fail(f"{name}: expected at least {expected} questions, got {len(data)}")
    ids = []
    normalized_questions = []
    counts = [0, 0, 0, 0]
    local_questions = set()
    for i, item in enumerate(data):
        if not isinstance(item, dict):
            continue
        ids.append(str(item.get("id", "")))
        question = str(item.get("question", "")).strip()
        norm_q = re.sub(r"\s+", " ", question.lower())
        if not norm_q:
            fail(f"{name}[{i}]: empty question")
        elif norm_q in local_questions:
            fail(f"{name}[{i}]: duplicate question inside dataset")
        else:
            local_questions.add(norm_q)
            if norm_q in seen_level_questions:
                fail(f"{name}[{i}]: duplicate question also present in {seen_level_questions[norm_q]}")
            else:
                seen_level_questions[norm_q] = name
        opts = item.get("options")
        answer = item.get("correct")
        ex = item.get("explanations")
        if not isinstance(opts, list) or len(opts) != 4:
            fail(f"{name}[{i}]: expected exactly 4 options")
            continue
        if len(set(str(x).strip() for x in opts)) != 4:
            fail(f"{name}[{i}]: duplicate answer options")
        if not isinstance(answer, int) or not 0 <= answer < 4:
            fail(f"{name}[{i}]: invalid correct index {answer!r}")
        else:
            counts[answer] += 1
        if not isinstance(ex, list) or len(ex) != 4:
            fail(f"{name}[{i}]: expected 4 answer explanations")
        if name == "prayer_hard.json":
            short = [str(x) for x in opts if len(str(x).strip()) < 35]
            if short:
                fail(f"{name}[{i}]: hard-level option is too short/obvious: {short[0]!r}")
            if len({str(x).strip().lower() for x in opts}) != 4:
                fail(f"{name}[{i}]: hard-level answer choices are not distinct")
    if len(ids) != len(set(ids)):
        fail(f"{name}: duplicate question ids")
    if len(normalized_questions) != len(set(normalized_questions)):
        fail(f"{name}: duplicate question texts")

    # Reject strong near-duplicates, not only byte-identical question text.
    token_sets = []
    for item in data:
        q = str(item.get("question", "")).lower()
        tokens = {
            x for x in re.findall(r"[0-9a-zа-яё‘]+", q)
            if len(x) > 3
        }
        token_sets.append(tokens)
    for i in range(len(token_sets)):
        for j in range(i + 1, len(token_sets)):
            a, b = token_sets[i], token_sets[j]
            union = len(a | b)
            similarity = (len(a & b) / union) if union else 0.0
            if similarity >= 0.65:
                fail(f"{name}: questions {i+1} and {j+1} are too similar ({similarity:.2f})")

    if max(counts) - min(counts) > 1:
        fail(f"{name}: answer positions are imbalanced: {counts}")

# 7. Audited core sources must not fall back to vague placeholders.

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

# 8. Single-choice checks should not teach a fixed answer position.
for name in ["mind_check.json", "mind_exam.json", "prayer_check.json", "alfatiha_medium.json", "expert_data.json", "prayer_medium.json", "prayer_hard.json"]:
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

# 8.5. Minor-shirk section must remain substantial, sourced and genuinely advanced.
minor_intro = parsed.get("minor_shirk_intro.json", [])
minor_course = parsed.get("minor_shirk_course.json", [])
minor_daily = parsed.get("minor_shirk_daily.json", [])
minor_quiz = parsed.get("minor_shirk_quiz.json", [])

if len(minor_intro) != 1:
    fail(f"minor_shirk_intro.json: expected one preface object, got {len(minor_intro)}")
if len(minor_course) < 33:
    fail(f"minor_shirk_course.json: expected at least 33 topics, got {len(minor_course)}")
if len(minor_daily) < 60:
    fail(f"minor_shirk_daily.json: expected at least 60 scenarios, got {len(minor_daily)}")
if len(minor_quiz) != 100:
    fail(f"minor_shirk_quiz.json: expected exactly 100 advanced questions, got {len(minor_quiz)}")

for name, data, required in [
    ("minor_shirk_course.json", minor_course, ["id", "title", "short", "understand", "element", "boundary", "deep", "sources"]),
    ("minor_shirk_daily.json", minor_daily, ["id", "title", "case", "verdict", "inner", "element", "boundary", "correct", "source"]),
]:
    ids = []
    for i, item in enumerate(data):
        if not isinstance(item, dict):
            fail(f"{name}[{i}]: expected object")
            continue
        ids.append(str(item.get("id", "")).strip())
        for key in required:
            value = item.get(key)
            if isinstance(value, list):
                ok = any(str(x).strip() for x in value)
            else:
                ok = bool(str(value or "").strip())
            if not ok:
                fail(f"{name}[{i}]: missing {key}")
    if len(ids) != len(set(ids)):
        fail(f"{name}: duplicate ids")

minor_counts = [0, 0, 0, 0]
minor_questions = set()
for i, item in enumerate(minor_quiz):
    if not isinstance(item, dict):
        fail(f"minor_shirk_quiz.json[{i}]: expected object")
        continue
    question = re.sub(r"\s+", " ", str(item.get("q", "")).strip().lower())
    if not question:
        fail(f"minor_shirk_quiz.json[{i}]: empty question")
    elif question in minor_questions:
        fail(f"minor_shirk_quiz.json[{i}]: duplicate question")
    minor_questions.add(question)

    opts = item.get("o")
    answer = item.get("c")
    explanation = str(item.get("e", "")).strip()
    if not isinstance(opts, list) or len(opts) != 4:
        fail(f"minor_shirk_quiz.json[{i}]: expected exactly 4 options")
        continue
    normalized_opts = [re.sub(r"\s+", " ", str(x).strip()) for x in opts]
    if len(set(x.lower() for x in normalized_opts)) != 4:
        fail(f"minor_shirk_quiz.json[{i}]: duplicate options")
    for option in normalized_opts:
        if len(option) < 70:
            fail(f"minor_shirk_quiz.json[{i}]: option too short/obvious: {option!r}")
    if not isinstance(answer, int) or not 0 <= answer < 4:
        fail(f"minor_shirk_quiz.json[{i}]: invalid correct index {answer!r}")
    else:
        minor_counts[answer] += 1
    if len(explanation) < 55:
        fail(f"minor_shirk_quiz.json[{i}]: explanation too short")

if minor_quiz and max(minor_counts) - min(minor_counts) > 1:
    fail(f"minor_shirk_quiz.json: answer positions are imbalanced: {minor_counts}")

course_ids = {
    str(item.get("id", "")).strip()
    for item in minor_course
    if isinstance(item, dict) and str(item.get("id", "")).strip()
}
topic_counts = {topic_id: 0 for topic_id in course_ids}
for i, item in enumerate(minor_quiz):
    if not isinstance(item, dict):
        continue
    topic = str(item.get("topic", "")).strip()
    difficulty = str(item.get("difficulty", "")).strip()
    if difficulty != "advanced":
        fail(f"minor_shirk_quiz.json[{i}]: expected difficulty='advanced'")
    if not topic:
        fail(f"minor_shirk_quiz.json[{i}]: missing topic mapping")
    elif topic != "mixed" and topic not in course_ids:
        fail(f"minor_shirk_quiz.json[{i}]: unknown topic {topic!r}")
    elif topic in topic_counts:
        topic_counts[topic] += 1

for topic_id, count in sorted(topic_counts.items()):
    if count < 2:
        fail(f"minor_shirk_quiz.json: topic {topic_id!r} has only {count} mapped questions; expected at least 2")

# Book/user-facing content must never mention implementation history or developer migration notes.
minor_shirk_user_text = "\n".join(
    (ASSETS / name).read_text(encoding="utf-8").lower()
    for name in [
        "minor_shirk_intro.json",
        "minor_shirk_course.json",
        "minor_shirk_daily.json",
        "minor_shirk_quiz.json",
    ]
)
for phrase in [
    "такфир",
    "не переносим хукм",
    "хукм конкретного человека",
    "хукм конкретного лица",
    "обвинять человека в ширке",
    "обвинять владельца",
    "необоснованных ярлыков",
    "самообвинение в ширке",
]:
    if phrase in minor_shirk_user_text:
        fail(f"minor-shirk content: stale accusation/takfir framing remains: {phrase!r}")

developer_phrases = [
    "webview",
    "веб-приложение",
    "веб приложение",
    "веб-версия",
    "веб версия",
    "незаконченного проекта",
    "перенесён из проекта",
    "перенесен из проекта",
    "исходный проект",
    "для разработчика",
]
developer_scan_files = [MAIN] + [
    path for path in sorted(ASSETS.glob("*.json"))
    if path.name != "app_meta.json"
]
for path in developer_scan_files:
    raw = path.read_text(encoding="utf-8").lower()
    for phrase in developer_phrases:
        if phrase in raw:
            fail(f"{path.relative_to(ROOT)}: developer-facing phrase remains: {phrase!r}")

# 9. Removed UX patterns must not reappear in user-visible code/data.


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

# 10. Literal screens passed to clear() must be restorable via Back.
src = MAIN.read_text(encoding="utf-8")
cleared = set(re.findall(r'clear\("([A-Za-z0-9_]+)"', src))
restored = set(re.findall(r'case"([A-Za-z0-9_]+)"\s*:', src))
# Dialog-only/non-history screens can be explicitly exempted here.
exempt = set()
missing_restore = sorted(cleared - restored - exempt)
if missing_restore:
    fail("MainActivity.java: screens missing from restore(): " + ", ".join(missing_restore))

# 11. Deleted prayer practice screens must stay deleted.
for symbol in ["renderPrayerPracticeHub", "renderPrayerBefore", "renderPrayerFlowPractice", "renderPrayerAfter", "renderMindPractice"]:
    if symbol in src:
        fail(f"MainActivity.java: stale removed symbol {symbol}")

if errors:
    print("CONTENT/NAVIGATION VERIFY: FAIL")
    for e in errors:
        print(" -", e)
    raise SystemExit(1)

print(f"CONTENT/NAVIGATION VERIFY: PASS ({len(parsed)} JSON assets, {word_count} detailed prayer entries, {len(cleared)} restorable screens)")
