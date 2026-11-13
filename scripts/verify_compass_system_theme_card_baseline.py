#!/usr/bin/env python3
"""Regression contracts: one theme source, no Azkar first-frame mismatch, level card baselines.
Static checks only; verify the resulting APK visually on device as well.
"""
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
host = (root / "app/src/main/java/app/alfatiha/tafsir/MainActivity.java").read_text(encoding="utf-8")
azkar_activity = (root / "azkar/src/main/java/app/xalidmuslim/azkar/MainActivity.kt").read_text(encoding="utf-8")
azkar_reader = (root / "azkar/src/main/java/app/xalidmuslim/azkar/ui/reading/AzkarReaderScreen.kt").read_text(encoding="utf-8")

settings = host.split("private void renderSettings(boolean push)", 1)[1].split("private void renderMenu(boolean push)", 1)[0]
home = host.split("private LinearLayout homeFeature(", 1)[1].split("// --- Малый ширк", 1)[0]
assert '"theme_mode"' in host, "Host must persist theme mode"
assert '"system"' in settings and '"light"' in settings and '"dark"' in settings, "Three theme options required"
assert 'readThemeMode()' in host and 'resolveThemeDark()' in host, "Main screen must resolve system dark before first paint"
assert 'UI_MODE_NIGHT_MASK' in host, "Android night mode must be consulted"
assert 'themeMode=readThemeMode()' in host, "Host onCreate/onResume must read configured mode"
assert 'chooseThemeMode(option[0])' in settings, "Settings must use Compass global theme setter"
assert 'putString("theme_mode",themeMode)' in host, "Global setter must persist theme mode"
assert 'putBoolean("dark",dark)' in host, "Keep older modules relying on the legacy bool in sync"
assert 'c.setGravity(Gravity.CENTER_VERTICAL);' in home, "Tile content must be vertically centered"
assert 'textBlock.setGravity(Gravity.CENTER);' in home, "Shared centered title region is required"
assert 'textBlock.setMinimumHeight(dp(homeTileHeightDp<=128?54:62));' in home, "Card text regions must have matching heights"
assert 't.setMinLines(1);' in home and 't.setMaxLines(2);' in home
assert 'new LinearLayout.LayoutParams(-1,dp(homeTileIconDp))' in home
assert 'subLp.setMargins(0,dp(2),0,0)' in home, "Title and subtitle should stay together"
assert 'installHomeCardPressMotion(c)' in home, "Preserve home card touch animation"

assert 'getString("theme_mode", null)' in azkar_activity, "First Azkar window frame must read host theme mode"
assert 'UI_MODE_NIGHT_MASK' in azkar_activity, "Azkar splash must support automatic dark mode"
assert 'sharedThemeMode' in azkar_reader, "Azkar screen must derive theme from shared host preference"
assert 'themeMode = sharedThemeMode' in azkar_reader, "Azkar must paint the selected theme on first compose frame"
assert 'putString("theme_mode"' in azkar_reader, "Azkar toggle must switch the host into explicit light/dark"
assert 'if (key == "theme_mode" || key == "dark")' in azkar_reader, "Module observes host theme changes"
assert 'LaunchedEffect(readerUi.isHydrated, sharedThemeMode)' in azkar_reader, "Stored module theme must reconcile without flash"
print("System/manual theme and aligned dashboard card contracts OK")
