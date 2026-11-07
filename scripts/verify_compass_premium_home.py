#!/usr/bin/env python3
"""Contract checks for the revised Cyrillic handwriting and card motion.

These checks validate the source and do not claim measured on-device FPS.
"""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
host = (root / "app/src/main/java/app/alfatiha/tafsir/MainActivity.java").read_text()
gradle = (root / "app/build.gradle").read_text()
license = (root / "app/src/main/icon/MARCK_SCRIPT_OFL.txt").read_text()

# Use a real, bundled Cyrillic handwritten face, never synthesize heavy bold.
assert 'fonts/MarckScript-Regular.ttf' in host
assert 'marckscript_cyrillic.b64' in gradle
assert "Typeface.create(bundled,600,false)" not in host
assert "SIL OPEN FONT LICENSE" in license
assert "new HandwrittenPhraseView(" in host

# Substantial but bounded feedback + staggered view-property intro, no bitmap morph.
assert "private void animateHomeCardEntrance(" in host
assert "animateHomeCardEntrance(hero," in host
assert "animateHomeCardEntrance(quizResume," in host
assert host.count("animateHomeCardEntrance(f") >= 6
assert "setStartDelay(" in host
assert "setDuration(450)" in host
assert "scaleX(.972f)" in host
assert "new android.graphics.drawable.RippleDrawable(" in host
assert "class MorphContainerDrawable" not in host
assert "morphSnapshot(" not in host
assert "if(!morphAnimationsEnabled())return" in host

# Existing reader, menus and app routing must survive.
assert "morphOpenDialogCard(" in host
assert "class MorphNavBackground" in host
assert "openAzkar()" in host and "openMedicine()" in host
assert "app.alfatiha.tafsir" in gradle
print("Premium dashboard motion and Marck Script source contracts: OK")
