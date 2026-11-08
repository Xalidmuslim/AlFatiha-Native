#!/usr/bin/env python3
"""Home motion contracts: restore wind-driven sand, legible Cyrillic and physics spring.

Source checks complement APK compile/lint; frame pacing still needs device tests.
"""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
host = (root/"app/src/main/java/app/alfatiha/tafsir/MainActivity.java").read_text()
gradle = (root/"app/build.gradle").read_text()
license = (root/"app/src/main/icon/NEUCHA_OFL.txt").read_text()

# Text is handwritten and Cyrillic-capable, with particle wind on first presentation.
assert 'fonts/Neucha.ttf' in host
assert "neucha_cyrillic.b64" in gradle
assert "SIL OPEN FONT LICENSE" in license
assert "class SandPhraseView extends View" in host
assert "new SandPhraseView(" in host
assert "buildSandParticles(" in host
assert "startSandAnimation(" in host
assert "particlePaint" in host and "wind=" in host
assert "drawFinalText(" in host
assert "morphAnimationsEnabled()" in host

# Remove all entrance cascade, including delayed scale/opacity.
assert "animateHomeCardEntrance(" not in host
assert "delayMs=Math.min(340" not in host
assert "scaleX(.976f)" not in host

# Actual physics, not just an overshoot easing on main dashboard.
assert "androidx.dynamicanimation:dynamicanimation:1.1.0" in gradle
assert "new androidx.dynamicanimation.animation.SpringAnimation(" in host
assert "new androidx.dynamicanimation.animation.SpringForce(" in host
assert "installHomeCardPressMotion(" in host
assert "openHomeCardWithSpring(" in host
assert "installHomeCardPressMotion(hero)" in host
assert "installHomeCardRipple(c)" in host
assert "homeCardNavigationPending" in host
assert "if(!morphAnimationsEnabled())" in host

# Protect old navigation, external modules, and the light reader transitions.
assert "class MorphNavBackground" in host
assert "morphOpenDialogCard(" in host
assert "openMedicine()" in host and "openAzkar()" in host
assert "class MorphContainerDrawable" not in host
assert "morphSnapshot(" not in host
assert "sandSpringCompare" in gradle
print("Sand-wind handwriting and spring-home card contracts: OK")
