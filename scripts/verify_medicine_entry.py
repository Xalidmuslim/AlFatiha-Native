#!/usr/bin/env python3
"""Regression guard for Medicine cold-entry preparation.

This is a source contract, NOT a frame-time benchmark. Hardware profiling
is required to assert that visual stutter is gone.
"""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
warmup = (root / "medicine/src/main/java/com/xalid/meditsinaproroka/nativeapp/MedicineRuntimeWarmup.kt").read_text()
activity = (root / "medicine/src/main/java/com/xalid/meditsinaproroka/nativeapp/MainActivity.kt").read_text()
host = (root / "app/src/main/java/app/alfatiha/tafsir/MainActivity.java").read_text()

assert "MedicineBookCache.preload(appContext)" in warmup, "Keep background book warmup"
assert "thread(" in warmup, "Warmup must run outside the UI thread"
assert "MedicineHomeArtwork.preload(appContext)" in warmup, (
    "Preload home artwork from the host before medicine Activity starts"
)
assert warmup.index("MedicineHomeArtwork.preload(appContext)") > warmup.index("thread("), (
    "Bitmap decoding must not occur synchronously in the host"
)
assert "MedicineHomeArtwork.isReady()" in activity, (
    "First render must check that home artwork is decoded"
)
assert "medicineEntry.setOnTouchListener(null);" in host, (
    "Avoid animating and dismissing the medicine menu launch button concurrently"
)
print("Medicine entry readiness contract: OK")
