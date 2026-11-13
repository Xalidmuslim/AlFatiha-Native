#!/usr/bin/env python3
"""Static regression contracts for per-period Azkar resume and modern home tiles.

Device tests are still required to confirm animation continuity at frame level.
"""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
prod = (root / "azkar/src/main/java/app/xalidmuslim/azkar/ui/reading/AzkarProductionReaderScreen.kt").read_text(encoding="utf-8")
reader = (root / "azkar/src/main/java/app/xalidmuslim/azkar/ui/reading/AzkarReaderScreen.kt").read_text(encoding="utf-8")
tabs = (root / "azkar/src/main/java/app/xalidmuslim/azkar/ui/reading/AzkarGoldenReadingScreen.kt").read_text(encoding="utf-8")
host = (root / "app/src/main/java/app/alfatiha/tafsir/MainActivity.java").read_text(encoding="utf-8")
home = host.split("private LinearLayout homeFeature(", 1)[1].split("// --- Малый ширк", 1)[0]
head = host.split("private void appTop()",1)[1].split("private void renderHome(",1)[0]

assert "sessionIndexByPeriod" in prod
assert "sessionIndexByPeriod[period] = navigation.state.activeIndex" in prod
assert "rememberedIndex ?: initialIndex(newPeriod, savedItemId)" in prod
assert "sessionPositionRestored = rememberedIndex != null" in prod
assert "periodController.sessionPositionRestored" in prod
assert "uiController.saveLastItem(oldPeriod, item.id)" in prod, "Save old period before switching"
assert "savedItemId = uiController.state.lastItemByPeriod[newPeriod]" in prod
assert "periodSwitchGeneration = periodController.switchGeneration" in prod
assert "preserveSessionPosition = periodController.sessionPositionRestored" in prod
assert "mutableStateOf(preserveSessionPosition)" in reader, "Do not overwrite recent in-memory item with older DataStore state"
assert "hasPageTransition = navigation.generation > 0L || periodSwitchGeneration > 0" in reader
assert "LaunchedEffect(navigation.generation, periodSwitchGeneration, animationsEnabled)" in reader
assert "LocalAzkarTabIndicator provides sharedTabIndicator" in prod
assert "LocalAzkarTabIndicator.current ?: fallbackPosition" in tabs, "Keep one animated indicator across switched readers"
assert 'onPeriodChange = { newPeriod ->' in prod

assert 'FrameLayout top=new FrameLayout(this);' in home, "Icon center must be independent of arrow"
assert 'Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL' in home, "Keep icons horizontally centered"
assert 'textBlock.setGravity(Gravity.CENTER);' in home
assert 't.setGravity(Gravity.CENTER);' in home and 'st.setGravity(Gravity.CENTER);' in home
assert 't.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL))' in home
assert 't.setLetterSpacing(-.012f)' in home
assert 'homeText(sub,homeTileSubSp,muted(),false)' in home, "Small text font must remain unchanged"
assert 'if(homeTop) title.setLetterSpacing(-.012f);' in head
assert 'Typeface.create("sans-serif-medium",Typeface.NORMAL)' in head
assert 'R.drawable.home_minor_shirk_exact' in host and 'R.drawable.home_azkar_mockup' in host
print("Azkar period resume, continuous transitions and centered modern home typography: OK")
