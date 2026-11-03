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

assert "class MorphContainerDrawable" in host, "Home cards must use genuine expanding container geometry"
assert "morphOpenHomeCard(" in host, "Home cards should open through transform"
assert "morphReturnToHomeCard(" in host, "Return home should reverse the container transform"
assert "morphOpenDialogCard(" in host, "Menu cards need a consistent morph transition"
assert "getWindow().getDecorView()" in host, "Transform uses overlay, not new activity"
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
print("Compass Morph navigation, card and reader protection contract: OK")
