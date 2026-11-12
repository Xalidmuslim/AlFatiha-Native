#!/usr/bin/env python3
"""Regression contract: Compass owns the single effective theme for every module.

Manual day/night toggles from any UI write BOTH theme_mode and legacy dark.
'System' takes effect only until the next manual choice. Device integration
(screenshots + night-mode configuration changes) remains a separate check.
"""
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
host = (root/"app/src/main/java/app/alfatiha/tafsir/MainActivity.java").read_text(encoding="utf-8")
medicine = (root/"medicine/src/main/java/com/xalid/meditsinaproroka/nativeapp/Store.kt").read_text(encoding="utf-8")
medicine_activity = (root/"medicine/src/main/java/com/xalid/meditsinaproroka/nativeapp/MainActivity.kt").read_text(encoding="utf-8")
azkar = (root/"azkar/src/main/java/app/xalidmuslim/azkar/ui/reading/AzkarReaderScreen.kt").read_text(encoding="utf-8")
azkar_activity = (root/"azkar/src/main/java/app/xalidmuslim/azkar/MainActivity.kt").read_text(encoding="utf-8")

header = host.split('Button theme=chromeMiniButton(',1)[1].split('top.addView(theme);',1)[0]
settings = host.split('settingsHeading("Оформление");',1)[1].split('if(isReadingRoute(',1)[0]
resumer = host.split('@Override protected void onResume()',1)[1].split('private void handleHeartNavIntent(',1)[0]

assert 'chooseThemeMode(dark?"light":"dark")' in header, "Compass header toggle must explicitly select a persisted mode"
assert 'putBoolean("dark",dark).apply()' not in header, "Do not update the legacy bool alone"
assert 'chooseThemeMode(option[0])' in settings, "Settings and toolbar must use one theme setter"
assert 'putString("theme_mode",themeMode)' in host, "Manual choices must overwrite system mode"
assert 'putBoolean("dark",dark)' in host, "Legacy tint for other modules must follow the same transaction"
assert 'readThemeMode()' in resumer, "When returning from Azkar or Medicine, read Compass preferences"
assert 'previousMode' in resumer, "Refresh selected settings even if auto/manual resolve to same color"
assert 'UI_MODE_NIGHT_MASK' in host, "Auto mode follows Android system appearance"

assert 'internal fun resolvedCompassTheme(context: Context)' in medicine, "Medicine must resolve Compass preferences"
assert 'getString("theme_mode", null)' in medicine, "Medicine cannot use only the dark bool"
assert 'Configuration.UI_MODE_NIGHT_MASK' in medicine, "Medicine auto mode must follow system"
assert 'resolvedCompassTheme(' in medicine_activity, "Medicine first frame uses Compass mode"
assert 'resolvedCompassTheme(' in medicine.split('fun syncSharedTheme()',1)[1], "Medicine must sync from Compass on return"
assert 'resolvedCompassTheme(' in medicine.split('private fun loadSettings()',1)[1], "Medicine cache must use Compass on first frame"
assert 'putString("theme_mode", next)' in medicine, "Medicine manual toggle must override system for the entire app"
assert 'next.theme != previous.theme' in medicine, "Unrelated Medicine text setting must not turn off system mode"
assert 'putString("theme_mode", next.theme)' in medicine, "Any Medicine theme selection must update Compass authority"

assert 'readHostAzkarThemeMode(globalUiPreferences)' in azkar, "Azkar first Compose frame uses Compass preference"
assert 'themeMode = sharedThemeMode' in azkar, "Azkar display honors the shared choice"
assert '.putString("theme_mode", if (nextDark) "dark" else "light")' in azkar, "Azkar manual toggle must override system"
assert 'getString("theme_mode", null)' in azkar_activity, "Azkar window background honors shared mode"
print("Single-owner day/night/auto contract across Compass, Azkar, and Medicine: OK")
