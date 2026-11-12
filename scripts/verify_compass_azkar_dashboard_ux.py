#!/usr/bin/env python3
"""UI regression contracts for Compass / Azkar. Device visual QA still required."""
from pathlib import Path
import re
root=Path(__file__).resolve().parents[1]
az=(root/"azkar/src/main/java/app/xalidmuslim/azkar/ui/reading/AzkarGoldenReadingScreen.kt").read_text()
sheets=(root/"azkar/src/main/java/app/xalidmuslim/azkar/ui/reading/AzkarReaderSheets.kt").read_text()
list_ui=(root/"azkar/src/main/java/app/xalidmuslim/azkar/ui/reading/AzkarListReadingScreen.kt").read_text()
host=(root/"app/src/main/java/app/alfatiha/tafsir/MainActivity.java").read_text()
daily=(root/"app/src/main/assets/minor_shirk_daily.json").read_text()
course=(root/"app/src/main/assets/minor_shirk_course.json").read_text()
quiz=(root/"app/src/main/assets/minor_shirk_quiz.json").read_text()
assert 'AzkarProgressCard(state, onResetProgress)' not in az
assert 'AzkarProgressCard(summaryState, onResetProgress)' not in list_ui
assert 'AzkarDropletIndicator(' in az
assert '☾ Ночь' in az and 'Вечер' in az
assert 'title = "Смысл и польза"' in sheets
assert 'title = "Как применять"' in sheets
assert 'title = "Хадисы и источники"' in sheets
section=sheets.split('private fun AzkarExplanationSheet(')[1].split('private enum class AzkarInsightTone')[0]
assert section.count('AzkarInsightSection(')==3, 'Only three compact accordions should remain'
assert 'fixedHeight = true' not in section
assert 'rememberSaveable(resetKey, title)' in sheets
assert 'renderPersonalReadingHub' in host
assert 'if("home".equals(type))' in host
assert 'ПОЛЕЗНЫЕ МАТЕРИАЛЫ' in host
assert 'profileCompactMeter' in host
assert 'profileRing' in host
for content in (host,daily,course,quiz):
    assert not re.search(r'\bкейс(?:ах|е|ов|ы|ом)?\b',content,re.IGNORECASE), 'Old learner-facing word КЕЙС remains'
print("Compass/Azkar UX contracts OK")
