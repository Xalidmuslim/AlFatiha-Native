# Native Phase 1 — Design System / Theme / Typography

Base commit: `48fdb6854cb5c494f9b65b3bc57ca326a8a1c32a`

Scope: **Phase 1 only**. No ReadingScreen, pager, bottom-sheet implementation, swipe, Back navigation, Azkar business state, or religious-content changes are included.

## 1. Android structure before Phase 1

- Android module: `:app`
- namespace / application package: `app.alfatiha.tafsir`
- production UI: Java `MainActivity`, classic Android Views
- AGP: `8.7.3`
- project Gradle runner: `8.9`
- compileSdk / targetSdk: `35`
- minSdk: `24`
- Kotlin before Phase 1: not configured
- Compose before Phase 1: not configured
- Material 3 before Phase 1: not configured
- existing production theme: `android:style/Theme.Material.Light.NoActionBar`
- isolated Azkar UI package: `app.alfatiha.tafsir.azkar.ui.designsystem`

Phase 1 does not connect the new Compose layer to `MainActivity` or production navigation.

## 2. Compose toolchain

Phase 1 adds only what is required to compile the isolated Azkar design system:

- Kotlin Android: `2.0.21`
- Kotlin Compose compiler plugin: `2.0.21`
- Compose BOM: `2024.10.01`
- AndroidX Core KTX: `1.15.0`
- Java/Kotlin JVM target: `17`
- Material 3: **not used**
- Dynamic Color / Material You: **not used**

The existing app module remains the only Android module; existing architecture is not rewritten.

## 3. CSS px → Compose mapping

At the 1x baseline used by the Golden Master:

- CSS geometry `Npx` → Compose `N.dp`
- CSS text `Npx` → Compose `N.sp`
- CSS unitless line-height → `fontSizeSp × lineHeight`
- CSS `em` letter-spacing → Compose `em`
- CSS viewport fractions for sheet max-heights remain fractions instead of being rounded to fixed dp
- CSS safe-area values remain runtime insets and are not replaced by invented constants

No arbitrary rounding or substitution is used. Values such as 10, 13, 14, 15, 24, 29, 42 and 620 are retained exactly where specified by `docs/NATIVE_UI_CONTRACT.md`.

## 4. Design-system files

- `AzkarColors.kt`
  - exact light/dark base colors
  - semantic derived color mixes
- `AzkarDimensions.kt`
  - `AzkarSpacing`
  - `AzkarRadius`
  - `AzkarBorders`
  - `AzkarElevation`
  - `AzkarDimensions`
- `AzkarTypography.kt`
  - `RussianFontFamily`
  - `ArabicFontFamily`
  - local native `FontFamily` mapping
  - fixed and reader typography from the Golden Master
- `AzkarMotion.kt`
  - page/sheet/progress/toggle timing and easing tokens
- `AzkarTheme.kt`
  - Light / Dark / System
  - no dynamic Material colors
  - edge-to-edge system-bar preparation
- `AzkarComponents.kt`
  - foundation components only
- `AzkarDesignSystemGallery.kt`
  - preview-only Light + Dark component evidence
- `AzkarDesignSystemTest.kt`
  - theme/font/token/color invariants

## 5. Offline runtime fonts

The exact official font files are fetched **at Android build time only** from the official `google/fonts` repository pinned to:

`23e54b51ddffbc7713c583748e3bd86f62b1fa4a`

Each TTF is verified before Android resource processing using:

1. exact byte length;
2. exact Git blob SHA-1 (`SHA1("blob " + length + NUL + bytes)`).

The verified files are generated into `build/generated/azkar-fonts/res/font` and packaged as normal Android `res/font` resources. There is no runtime font download and no INTERNET permission is introduced.

| Family | Official pinned source | Native weights |
|---|---|---|
| Literata | `ofl/literata/Literata[opsz,wght].ttf` | 400/500/600/700 |
| PT Serif | `PT_Serif-Web-Regular.ttf`, `PT_Serif-Web-Bold.ttf` | 400/700 |
| Inter | `Inter[opsz,wght].ttf` | 400/500/600/700 |
| Manrope | `Manrope[wght].ttf` | 400/500/600/700 |
| Noto Naskh Arabic | `NotoNaskhArabic[wght].ttf` | 400/500/600/700 |
| Noto Sans Arabic | `NotoSansArabic[wdth,wght].ttf` | 400/500/600/700 |
| Amiri | `Amiri-Regular.ttf`, `Amiri-Bold.ttf` | 400/700 |
| Scheherazade New | Regular / Medium / SemiBold / Bold | 400/500/600/700 |

Russian system choices map to Android `FontFamily.SansSerif` and `FontFamily.Serif`.

Official OFL license texts are committed under `third_party/fonts/<family>/OFL.txt`.

### Arabic rendering contract

Arabic reader typography explicitly sets:

- selected official Arabic font family;
- weight 500 where the Golden Master specifies it;
- RTL text direction;
- right alignment;
- exact selected line-height;
- `includeFontPadding = false`;
- `LineHeightStyle.Trim.None` so top/bottom line-height is not trimmed.

The design-system gallery includes the required harakat sample:

`بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ`

This gives a fixed native preview case for shaping, baseline and diacritic inspection.

## 6. Theme mapping

### Light

- background `#f4f0e7`
- foreground `#171c19`
- card `#fbf8f1`
- surface `#eee8dc`
- muted `#646a66`
- border `#d8d0c1`
- primary `#1f5c48`
- accent `#dce8e1`
- warning `#9d6a20`
- success `#2f6b52`

### Dark

- background `#111613`
- foreground `#edf2ee`
- card `#1a211c`
- surface `#222a24`
- muted `#aab5ad`
- border `#323c35`
- primary `#7fb99e`
- accent `#24382e`
- warning `#c99b58`
- success `#74ad90`

Derived success/warning/sticky/active colors preserve the exact percentages from `NATIVE_UI_CONTRACT.md`.

`System` only resolves to Light or Dark from the Android system setting. Dynamic Color is not enabled.

## 7. Motion mapping

- dhikr page: `620ms`
- page easing: `CubicBezierEasing(0.16, 1, 0.3, 1)`
- page starting X: `+24dp` / `-24dp`
- page starting opacity: `0.62`
- page starting scale: `0.994`
- sheet: `300ms`, same easing, `+18dp` Y, starting opacity `0.70`
- progress: `350ms`, CSS `ease` equivalent
- toggle/state transition: `200ms`

No pager or sheet behavior is implemented in Phase 1. Compose animations participate in Android animator-duration scaling; Phase 2 must also consume the approved reduced-motion behavior when actual transitions are wired.

## 8. Foundation components

Implemented only:

- `AzkarSurface`
- `AzkarCardSurface`
- `AzkarPrimaryButton`
- `AzkarOutlineButton`
- `AzkarIconButton`
- `AzkarBadge`
- `AzkarProgressBar`

These consume Azkar design tokens directly. Material components and default Material visual values are not used.

The component gallery is a Compose Preview and is not registered in the manifest.

Not implemented in Phase 1:

- ReadingScreen
- SettingsSheet
- ContentsSheet
- ExplanationSheet
- DhikrPager
- swipe
- Back navigation

## 9. Material deviations

**NONE.**

The Golden Master visual values are represented directly through Azkar tokens. Phase 1 does not introduce Material component radii, palettes, typography, Dynamic Color, or other Material visual substitutions.

## 10. Validation

Phase 1 CI is intentionally branch-scoped and runs:

- `:app:compileDebugKotlin`
- `:app:testDebugUnitTest`
- `:app:lintDebug`

It does not run `assembleDebug`, does not upload an APK, and does not run Phase 2 work.

## 11. Phase 2 boundary

Phase 2 may assemble these primitives into the approved reading surfaces and interaction model defined by `docs/NATIVE_CANONICAL_BEHAVIOR.md`.

Phase 1 stops before that assembly begins.
