#!/usr/bin/env python3
"""Regression contracts for Compass Heart UX preview, not a substitute for device tests."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
host = (root/"app/src/main/java/app/alfatiha/tafsir/MainActivity.java").read_text()
search = (root/"app/src/main/java/app/alfatiha/tafsir/ContentSearchEntry.java").read_text()
gradle = (root/"app/build.gradle").read_text()
medicine = (root/"medicine/src/main/java/com/xalid/meditsinaproroka/nativeapp/App.kt").read_text()

# Less pronounced motion; never hold navigation for 300ms.
assert ".scaleX(.88f)" not in host
assert "},300);" not in host
assert ".scaleX(.97f)" in host and ".scaleY(.97f)" in host

# Dedicated app preference + OS animator scale, including a low-motion mode.
assert '"motion_mode"' in host
assert '"minimal"' in host and '"off"' in host
assert "morphAnimationsEnabled()" in host and "motionIsFull()" in host
assert "toggleReadingFocus()" in host
assert "isReadingRoute(" in host
assert "readingLineSpacing" in host

# Readability and restrained card chrome, with original art retained.
assert "homeTileTitleSp=14.0f" in host
assert "homeTileSubSp=12.0f" in host
assert "R.drawable.heart_prayer_hero_exact" in host
assert "R.drawable.home_book_mockup" in host
assert "featureStroke=0" in host

# Content search displays a useful excerpt with highlighted match.
assert "String snippet(String query)" in search
assert "searchHighlight(" in host
assert "e.snippet(q)" in host
assert "openMedicineChapter(e.arg,query)" in host
assert '"medicine_search_query"' in host

# Keep reader position for resumes; review uses existing schedule not duplicate data.
assert '"last_reader_route"' in host
assert "renderReviewToday(true)" in host
assert "unifiedUXCompare" in gradle
assert "Route.Reader(chapter.id, anchor)" in medicine
print("Compass unified UX contracts: OK")
