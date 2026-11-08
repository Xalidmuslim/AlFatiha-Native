#!/usr/bin/env python3
"""Cross-module motion contract.

Source contracts are not frame-time benchmarks. They guard intentional
interaction and book/scroll preservation while running CI compilation.
"""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
host = (root / "app/src/main/java/app/alfatiha/tafsir/MainActivity.java").read_text()
medicine = (root / "medicine/src/main/java/com/xalid/meditsinaproroka/nativeapp/App.kt").read_text()
reader = (root / "medicine/src/main/java/com/xalid/meditsinaproroka/nativeapp/Reader.kt").read_text()
azkar = (root / "azkar/src/main/java/app/xalidmuslim/azkar/ui/reading/AzkarReaderScreen.kt").read_text()
azkar_components = (root / "azkar/src/main/java/app/xalidmuslim/azkar/ui/designsystem/AzkarComponents.kt").read_text()

assert "class MorphContainerDrawable" not in host, "Home must no longer draw a fullscreen bitmap overlay"
assert "morphOpenHomeCard(" not in host, "Home tiles should navigate without geometry expansion"
assert "installHomeCardPressMotion(" in host, "Home cards keep soft touch feedback"
assert "class SandPhraseView" in host, "Cyrillic reminder preserves the wind-driven particle animation"
assert "morphOpenDialogCard(" in host, "Menu cards retain their separate transition"
assert "morphAnimationsEnabled()" in host, "Respect Android animation scale"
assert "class MorphNavBackground" in host, "Preserve native moving navigation indicator"
assert 'label = "azkarMorphPill"' in azkar, "Azkar nav must share the capsule motion pattern"
assert "activeNavIndex" in azkar, "Azkar selection should actually update on interaction"
assert 'label = "azkar-bottom-nav-press"' in azkar
assert "scaleIn(" in medicine and "scaleOut(" in medicine, "Non-reader sections should use subtle scale"
assert "betweenContentsAndReader" in medicine and "EnterTransition.None" in medicine
assert "showEntryFadeLayer" in reader and "setTextIsSelectable(true)" in reader, (
    "Preserve proven reader smoothness, native text selection and bookmarks"
)
assert "AzkarCardSurface(" in azkar_components, "Reading content remains unchanged"
print("Compass soft-home, menu, navigation and reader protection contract: OK")
