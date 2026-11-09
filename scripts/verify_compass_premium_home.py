#!/usr/bin/env python3
"""Contract checks for the revised Cyrillic handwriting and card motion.

These checks validate the source and do not claim measured on-device FPS.
"""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
host = (root / "app/src/main/java/app/alfatiha/tafsir/MainActivity.java").read_text()
gradle = (root / "app/build.gradle").read_text()
license = (root / "app/src/main/icon/NEUCHA_OFL.txt").read_text()

# Use a real, bundled Cyrillic handwritten face, never synthesize heavy bold.
assert 'fonts/Neucha.ttf' in host
assert 'neucha_cyrillic.b64' in gradle
assert "Typeface.create(bundled,600,false)" not in host
assert "SIL OPEN FONT LICENSE" in license
assert "new SandPhraseView(" in host

# Substantial but bounded feedback + staggered view-property intro, no bitmap morph.
assert "private void animateHomeCardEntrance(" not in host
assert "installHomeCardPressMotion(hero)" in host
assert "installHomeCardPressMotion(quizResume)" in host
assert "animateHomeCardEntrance(f" not in host
assert "new androidx.dynamicanimation.animation.SpringAnimation(" in host
assert "setDampingRatio(.72f)" in host  # Updated restrained spring design
assert "scaleX(.97f)" in host  # New accessible feedback, no deep shrink
assert "new android.graphics.drawable.RippleDrawable(" in host
assert "class MorphContainerDrawable" not in host
assert "morphSnapshot(" not in host
assert "if(!morphAnimationsEnabled())" in host

# Existing reader, menus and app routing must survive.
assert "morphOpenDialogCard(" in host
assert "class MorphNavBackground" in host
assert "openAzkar()" in host and "openMedicine()" in host
assert "app.alfatiha.tafsir" in gradle
print("Neucha sand text and spring-home motion source contracts: OK")
