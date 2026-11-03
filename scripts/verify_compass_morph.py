#!/usr/bin/env python3
"""Source-level regression guard for Compass Morph Motion.

Visual smoothness needs device profiling; this test only protects the motion
contracts and the reader's existing scroll behavior.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
host = (ROOT / "app/src/main/java/app/alfatiha/tafsir/MainActivity.java").read_text()
med = (ROOT / "medicine/src/main/java/com/xalid/meditsinaproroka/nativeapp/App.kt").read_text()
reader = (ROOT / "medicine/src/main/java/com/xalid/meditsinaproroka/nativeapp/Reader.kt").read_text()
antique = (ROOT / "medicine/src/main/java/com/xalid/meditsinaproroka/nativeapp/AntiqueDesign.kt").read_text()
azkar = (ROOT / "azkar/src/main/java/app/xalidmuslim/azkar/ui/reading/AzkarReaderScreen.kt").read_text()

assert "class MorphNavBackground" in host, "Host nav must have a moving capsule"
assert "animateMorphNavTo(" in host, "Host nav must animate between destinations"
assert "installPressMotion(c,.985f)" in host, "Home feature cards must respond to press"
assert "installPressMotion(c,.975f)" in host, "Section-menu cards must respond to press"
assert "private void clear(String type,String arg,boolean push)" in host
assert "page.animate().cancel()" in host, "Screen transition must cancel stale animation"
assert "areAnimatorsEnabled()" in host, "Honor reduced/system-disabled animations"
assert "BoxWithConstraints(" in med and 'label = "medicineMorphPill"' in med, (
    "Medicine nav needs a translated active capsule"
)
assert 'label = "medicineMorphIcon"' in med, "Medicine icons need micro-transform"
assert 'betweenContentsAndReader' in med and 'EnterTransition.None' in med, (
    "Preserve parchment no-ghost transitions"
)
assert 'label = "medicinePaperCardPress"' in antique, "Home paper cards need soft press"
assert "if (hasPressLayer) Modifier.graphicsLayer" in antique, (
    "Do not keep a permanent graphics layer on the scrolling Medicine home list"
)
assert "setTextIsSelectable(true)" in reader, "Reader selection must be preserved"
assert "showEntryFadeLayer" in reader, "Preserve approved scroll-layer optimization"
assert "scrollState.scrollToItem" in reader, "Preserve reading position"
assert "HeartPrayerNavItem" in azkar, "Preserve Azkar navigation and state"
print("Compass Morph Motion source contracts passed")
