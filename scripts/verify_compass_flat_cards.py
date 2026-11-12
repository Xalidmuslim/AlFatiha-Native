#!/usr/bin/env python3
"""Screenshot-driven card alignment and drop-shadow regression contracts.

Does not substitute for device screenshots or frame-time measurements.
"""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
MAIN = ROOT / "app/src/main/java/app/alfatiha/tafsir/MainActivity.java"
AZKAR = ROOT / "azkar/src/main/java/app/xalidmuslim/azkar"
MEDICINE = ROOT / "medicine/src/main/java/com/xalid/meditsinaproroka/nativeapp"
native = MAIN.read_text(encoding="utf-8")
home = native.split("private LinearLayout homeFeature(", 1)[1].split("// --- Малый ширк", 1)[0]
assert re.search(r'^\s*t\.setMinLines\(1\);', home, re.MULTILINE), "Natural-height titles prevent empty gaps below one-line headings"
assert 'c.setGravity(Gravity.TOP);' in home, "All cards must use the same icon/title baseline"
assert 'textBlock.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);' in home, "Title/subtitle group should be compact"
assert 'subLp.setMargins(0,dp(2),0,0);' in home, "Title/subtitle gap should remain consistent"
assert 'new LinearLayout.LayoutParams(1,0,1f)' not in home, "Weighted spacer causes single-line titles to drop"
assert 't.setMaxLines(2);' in home and 'st.setMaxLines(2);' in home
assert "homeTileIconDp" in home and "homeTileTitleSp" in home, "Do not break responsive design"
assert "installHomeCardPressMotion(c)" in home, "Retain existing touch feedback"

# Do not use Android elevation as decoration anywhere in the main Activity.
calls = re.findall(r'\.setElevation\(\s*(.*?)\s*\);', native, flags=re.DOTALL)
assert calls, "Audit must explicitly account for native elevation calls"
assert all(arg == "0f" for arg in calls), f"Non-flat native surfaces: {calls}"
assert native.count("surfaceBg(") >= 29 and native.count("solidBg(") >= 18, "Retain the audited native surfaces and borders"

components = (AZKAR / "ui/designsystem/AzkarComponents.kt").read_text(encoding="utf-8")
dims = (AZKAR / "ui/designsystem/AzkarDimensions.kt").read_text(encoding="utf-8")
assert "paint.setShadowLayer(" not in components, "Azkar custom painted shadow still present"
assert ".shadow(" not in components, "Azkar card uses built-in elevation"
assert "Modifier.azkarShadow(" in components, "Keep call-site API compatibility"
assert "AzkarShadowLayer(2.dp" not in dims and "AzkarShadowLayer(14.dp" not in dims, "Remove painted drop-shadow layers"
assert "val LightCard = emptyList<AzkarShadowLayer>()" in dims
assert "val BottomSheet = emptyList<AzkarShadowLayer>()" in dims
assert "val ActivePeriod = emptyList<AzkarShadowLayer>()" in dims

reading = (AZKAR / "ui/reading/AzkarReaderScreen.kt").read_text(encoding="utf-8")
assert ".shadow(" not in reading, "Azkar bottom navigator shadow remains"

for kt in AZKAR.rglob("*.kt"):
    source = kt.read_text(encoding="utf-8")
    assert ".shadow(" not in source, f"Compose elevation remains: {kt.relative_to(ROOT)}"
    assert "setShadowLayer(" not in source, f"Artificial drawn shadow remains: {kt.relative_to(ROOT)}"

for kt in MEDICINE.glob("*.kt"):
    source = kt.read_text(encoding="utf-8")
    assert ".shadow(" not in source, f"Medicine custom shadow: {kt.name}"
    assert "setShadowLayer(" not in source, f"Medicine custom painted shadow: {kt.name}"
    for val in re.findall(r'shadowElevation\s*=\s*([^,\n)]+)', source):
        assert val.strip() == "0.dp", f"Medicine surface elevation still nonzero: {kt.name} {val}"
    for val in re.findall(r'defaultElevation\s*=\s*([^,\n)]+)', source):
        assert val.strip() == "0.dp", f"Medicine card elevation still nonzero: {kt.name} {val}"

print(f"Flat surfaces OK: six compact baseline-aligned home cards, {len(calls)} native elevation calls set to zero, Azkar/Medicine shadows off")
