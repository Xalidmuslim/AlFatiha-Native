# Full Arabic localization of Compass Heart / بوصلة القلب

**State: foundation implemented; full Arabic religious content NOT COMPLETE.**

This document prevents confusing a functional language selector with a fully translated publication.

## Source baseline
- Russian native Compass main before localization: `6a9cac4274659882ad691a437baf79208e593275`.
- Implementation branch: `codex/compass-full-arabic-localization-20261010`.
- Existing Medicine performance baseline is preserved; no Medicine reader / geometry / content code changed.
- Test preview uses distinct Android package `app.alfatiha.tafsir.arpreview147`; do not replace user installed versions.

## Implemented
- Settings: selectable `Русский` / `العربية`, stored in `alfatiha_native.app_language`, no reset of progress or font/theme settings.
- Native RTL view direction when Arabic is selected. Russian maintains LTR.
- In-memory Arabic UI lexicon in `CompassLanguage.java` for dashboard / primary navigation / study controls. Do not perform SharedPreferences access on each label.
- Arabic equivalents of all 27 rotating home reflections, maintaining the original no-repeat/random order.
- Parallel optional Arabic course JSON under `app/src/main/assets/ar/<source_name>`, with strict Russian fallback when translation has not been created.
- Six initial prayer lessons translated into an Arabic editorial draft at `app/src/main/assets/ar/prayer_intro.json`. They are NOT asserted to be verbatim passages of Ibn al-Qayyim.
- Android `values-ar` includes Arabic launcher label for device-localized Android.
- `scripts/verify_arabic_locale.py` validates translated lesson object shapes and protects original IDs and quiz answer keys.
- Continues to use native Java / Android Views + Kotlin / Jetpack Compose, not WebView.

## Remaining for the requested *full* Arabic app
- Translate and independently review the remaining Russian content under `app/src/main/assets/*.json`, preserving arrays, IDs, options, answer keys, references and detailed pedagogical explanations.
- Replace the hardcoded remaining Russian UI literals throughout `MainActivity.java`; their Russian wording is not removed until equivalent Arabic is ready.
- Port/verify Arabic source texts for the Hadith Qudsi collection; preserve narrator, gradings, numbering and exact references.
- Integrate Arabic text and sourced glosses into native `medicine` library; the current bundled book is Russian, and original sources are NOT guaranteed recoverable from a Russian paraphrase.
- Translate the `azkar` app navigation, instructions and explanation layers, preserving original Arabic dhikr text and repetition counts.
- Enforce RTL text alignment and glyph shaping across all three modules; manually test screen-reader labels, search index, chapter navigation, swipe semantics, quiz selection and small-screen overflow.
- Arabic religious content MUST be reviewed against Arabic scholarly sources before being called verified, attributed as a quotation, or publicly published. Machine-generated Arabic text, if used as a draft, is not by itself a verified Quran/hadith/tafsir source.

## Acceptance criteria for *complete* release
Every visible navigation label and explanatory paragraph in Compass + Azkar + Medicine is available in Arabic, with no unmarked Russian fallbacks; all question options and explanations carry equivalent meaning; answers and IDs match the Russian source exactly; Quran / hadith source quotations and citations checked; RTL and full R8 Android QA pass. Full completion cannot be claimed before this checklist is satisfied.

**Do not merge this experimental branch into main as if this were a complete Arabic edition.**
