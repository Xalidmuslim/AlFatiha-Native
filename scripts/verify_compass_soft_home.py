#!/usr/bin/env python3
"""Guard the requested home-only handwriting and gentle motion update.

Static contracts complement (but do not replace) runtime UI/device tests.
"""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
host = (root / "app/src/main/java/app/alfatiha/tafsir/MainActivity.java").read_text()
gradle = (root / "app/build.gradle").read_text()
license = (root / "app/src/main/icon/NEUCHA_OFL.txt").read_text()

# Readable, truly Cyrillic-capable handwriting only on the home reminder.
assert "class SandPhraseView extends View" in host
assert "new SandPhraseView(" in host
assert "fonts/Neucha.ttf" in host
assert "Neucha" in gradle and "assets.srcDir heartGeneratedAssets" in gradle
assert "SIL OPEN FONT LICENSE" in license
assert "class SandPhraseView" in host
assert "buildSandParticles(" in host

# Calm compositor-only home gestures: no bitmap overlays or spring bounce.
assert "class MorphContainerDrawable" not in host
assert "morphSnapshot(" not in host
assert "installHomeCardPressMotion(" in host
assert "new android.view.animation.OvershootInterpolator(.40f)" in host, (
    "Existing non-home controls should keep their old touch behavior"
)
assert "page.setScaleX(.997f)" not in host
assert "page.setTranslationX(home?0:dp(push?10:-7))" not in host

# Preserve separate modules' interactions and navigation.
assert "morphOpenDialogCard(" in host
assert "class MorphNavBackground" in host
assert "void renderHome(boolean push)" in host
assert "openMedicine()" in host and "openAzkar()" in host
print("Handwritten home reminder and soft home motion source contracts: OK")
