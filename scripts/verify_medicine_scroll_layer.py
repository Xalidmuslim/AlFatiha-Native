#!/usr/bin/env python3
"""Regression guard for the opt-in reader scroll-layer A/B test.

This verifies a SOURCE contract; it does not measure frame pacing or prove
that scrolling improves on a physical device.
"""
from pathlib import Path

src = (Path(__file__).resolve().parents[1] /
       "medicine/src/main/java/com/xalid/meditsinaproroka/nativeapp/Reader.kt").read_text()
assert "setTextIsSelectable(true)" in src, "Must preserve text selection and annotations"
assert 'onReset = { tv ->' in src, "Preserve AndroidView reuse for reader paragraphs"
assert 'state = scrollState,' in src, "Preserve reading position"
assert 'val showEntryFadeLayer by remember(foregroundOpacity)' in src, (
    "The entry graphics layer must disappear after the 110ms fade"
)
assert 'if (showEntryFadeLayer) Modifier.graphicsLayer' in src, (
    "Apply transparency layer only while entry fade is active"
)
assert '.graphicsLayer { alpha = foregroundOpacity.value }' not in src, (
    "Do not keep a full-list graphics layer permanently in the scroll path"
)
print("Reader scroll-layer A/B source contract: OK")
