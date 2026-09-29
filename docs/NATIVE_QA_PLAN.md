# Native QA Plan — Azkar

Scope: Phase 0 documentation only.

This plan is derived from the current `azkar-web` golden master. No Android build, APK generation, deployment check, screenshot automation, or Android source modification is part of Phase 0.

## 1. Objective

Native Android must reproduce the observable web golden master without redesign:
- layout and hierarchy
- typography
- colors
- spacing, padding and radii
- borders and shadows
- card states
- bottom sheets
- settings behavior
- progress behavior
- morning/evening filtering
- list/cards modes
- gestures
- back navigation
- motion
- light/dark theme
- safe-area behavior

The UI contract is `docs/NATIVE_UI_CONTRACT.md`.
The state/screen inventory is `docs/NATIVE_SCREEN_MATRIX.md`.

## 2. Golden-master inputs

Required source files:
- `index.html`
- `styles.css`
- `app-core.js`
- `data-1.js`
- `data-2.js`

Current data baseline:
- 16 total records
- 14 morning-visible
- 13 evening-visible
- count targets include 1, 3, 10, 100
- morning-only, evening-only, and dual-period records exist
- evening-specific Arabic/translation overrides exist
- disputed/note/insight states exist

## 3. Phase-gate rule

Phase 1 must not begin until:
1. all three Phase-0 docs exist;
2. UI values are traceable to the current web source;
3. the duplicate-logic anomaly in `app-core.js` is acknowledged as a behavioral verification risk;
4. no Android implementation has been changed during Phase 0.

## 4. Source-integrity checks

Before future implementation work:
- confirm the native work is based on the exact intended `azkar-web` revision;
- do not edit `azkar-web` to make native implementation easier;
- do not normalize or “improve” CSS values by eye;
- do not replace fonts, colors, spacing, radii, shadows, animation durations or easing without an explicit contract revision;
- do not merge morning/evening text variants;
- do not change count targets from the data files.

## 5. Visual QA checklist

### 5.1 Global
- app shell max width = 760px
- horizontal shell padding = 10px
- bottom safe-area allowance matches contract
- background/card/surface hierarchy matches light and dark tokens
- no unexpected system background shows through

### 5.2 Topbar
- minimum height 72px
- brand icon 40x40, radius 13px
- brand title/subtitle type styles match
- settings button 42x42, radius 12px

### 5.3 Source note
- padding, border, radius and muted text match
- text wraps without clipping

### 5.4 Period tabs
- two equal columns
- 6px gap and 4px container padding
- active/inactive color and shadow states match
- 42px minimum control height

### 5.5 Progress
- card radius 14px and shadow match
- completed-item count is “done items / visible items”
- bar is 5px high
- fill percentage matches rounded completed-item ratio
- reset action is visually text-only

### 5.6 Sticky reading toolbar
- sticky offset includes safe-area inset
- three-column structure is preserved
- background transparency/blurring does not become opaque or overly blurred
- compact settings button is 36x36

### 5.7 Dhikr cards
For representative short and long records:
- default padding = 15/14/13px
- compact padding = 12px
- radius = 15px
- border and shadow match
- Arabic is RTL and right-aligned
- Arabic/translation spacing matches
- source divider is dashed
- translation divider is solid
- note treatment uses warning tint
- completed left strip is exactly 3px
- completed check circle and disputed badge appear only under their current conditions
- long Arabic text scrolls vertically inside paged card without breaking horizontal swipe behavior
- paged card max-height follows `100dvh - 172px`

### 5.8 Pager
- three-column balance preserved
- disabled states use opacity .38
- middle helper text hides at <=370px
- first/last page buttons disable correctly

### 5.9 Bottom sheets
For Contents, Settings and Insight:
- sheet is bottom-anchored
- top radius = 24px
- bottom corners are square
- width is capped at 760px
- overlay opacity/color matches
- handle = 42x4
- close button position/size matches
- sheet scroll is contained
- safe-area bottom padding is preserved
- specific max-heights are respected:
  - Contents 82dvh
  - Insight 84dvh
  - Settings 72dvh

### 5.10 Settings
- live preview is visible without excessive whitespace
- Russian grid = 3 columns normally, 2 at <=370px
- Arabic grid = 2 columns
- active font tile outline matches primary
- sliders expose exact ranges/steps
- preview caps at 24px Arabic and 15px Russian
- reader style changes only the currently defined compact behavior
- visibility toggles update card content immediately
- theme selector has auto/light/dark only

## 6. Functional QA checklist

### 6.1 Launch period
Test local time boundaries:
- 03:59 → Evening
- 04:00 → Morning
- 15:59 → Morning
- 16:00 → Evening

### 6.2 Period switching
- switching period resets active index to 0
- appropriate 14/13 record set is shown
- evening overrides are used only in evening
- current-date progress remains date-scoped

### 6.3 Counting
Test target counts:
- 1
- 3
- 10
- 100

For each:
- starts at 0 on fresh state
- increments one step
- never exceeds target
- completed state appears at target
- completed count in header increments by item, not repetition
- disabled action prevents further increment

### 6.4 Reset
- reset removes progress for all currently visible period items
- UI re-renders to incomplete state
- verify shared morning/evening items are affected because progress is keyed by item id

### 6.5 Contents
Cards mode:
- current item highlighting is correct
- selecting an item changes active index
- direction reflects whether selected index is before/after current index

List mode:
- selecting an item closes sheet
- target item is brought to the reading position
- no unrelated state is lost

### 6.6 Settings persistence
After every setting change:
- value applies immediately
- value persists after re-render
- value persists after period change
- value persists after reopening settings
- theme auto follows system scheme

### 6.7 Daily progress persistence
- same calendar date restores counts
- next calendar date uses a new progress key
- malformed stored JSON falls back safely

## 7. Gesture QA

Cards mode:
1. horizontal swipe <55px → no page change
2. clear left swipe >=55px → next item
3. clear right swipe >=55px → previous item
4. vertical gesture should scroll long card content, not change card
5. diagonal gesture that does not exceed horizontal dominance ratio 1.15 should not page
6. touch beginning within left 28px must not be consumed by card paging in the history-aware contract
7. touch cancellation clears gesture state
8. horizontal swipe should not cause the vertical “jump” that motivated the native project

List mode:
- vertical scrolling remains normal
- card horizontal paging is inactive

## 8. Back-navigation QA

The first/history-aware implementation in the golden source defines these expected scenarios:
1. open Settings → system back closes Settings
2. open Contents → system back closes Contents
3. open Insight → system back closes Insight
4. navigate card A → B → system back restores A
5. change Morning → Evening → system back can restore prior period state
6. app-level card swipe must not steal a system back gesture that begins at the left edge

### Source anomaly
The same `app-core.js` later contains duplicated older definitions that do not carry the full history behavior and use different scrolling/navigation logic.

Therefore:
- Phase 0 records both the intended history-aware behavior and the existence of the duplicate older block.
- Phase 0 does not repair the web source.
- Before Phase 1 freezes native navigation semantics, actual effective golden-master behavior must be verified from the running source/revision or the duplicate must be explicitly resolved by the project owner in a later phase.

## 9. Responsive QA

Minimum required CSS-derived width cases:

### <=370px case
Verify:
- toolbar text label hidden
- counter row becomes vertical
- count action full width
- pager helper hidden
- Russian font grid has 2 columns

### >370px case
Verify:
- toolbar label visible
- counter row horizontal
- Russian font grid has 3 columns
- pager helper visible

### Wide case
Verify:
- content never exceeds 760px
- shell remains centered

No additional breakpoint may be invented merely to “improve” layout without contract change.

## 10. Theme QA

Run each core surface in:
- Light
- Dark
- Auto with system Light
- Auto with system Dark

Verify:
- all token values map correctly
- primary action text uses white in light theme
- `.count-btn` dark-theme foreground maps to `#0f1713`
- warning/success mixed states remain legible
- overlay and sheet shadows remain consistent
- browser/native system bar treatment does not force an uncontracted accent color

## 11. Typography QA

Russian:
- Literata default must match golden hierarchy
- all six configured options must be selectable
- no synthetic font substitution should silently replace a missing bundled/available font

Arabic:
- Noto Naskh Arabic default
- all four configured options
- RTL direction
- right alignment
- weight 500 in reader body
- diacritics and Qur'anic marks must not clip vertically
- long multi-paragraph Arabic in Mu'awwidhat and last two verses of Al-Baqarah must wrap without glyph overlap

Dynamic sizes:
- Arabic 22, 32, 44
- Russian 13, 17, 25
- line height 1.25, 1.65, 2.00

Check minimum and maximum combinations for clipping.

## 12. Content/data QA

Validate all 16 ids are present exactly once.

Period filtering:
- Morning count = 14
- Evening count = 13

Evening overrides:
- `kingdom`
- `by-you`

Disputed examples:
- `bismillah-protection`
- `pleased`
- `muawwidhat`
- `perfect-words`

Morning-only examples:
- `tahlil-hundred`
- `creation-count`
- `fitrah`

Evening-only examples:
- `perfect-words`
- `baqarah-last-two`

Do not alter religious text, source text, notes or insight text as part of UI parity work.

## 13. Motion QA

Verify exact web timings:
- progress = 350ms ease
- next/prev = 620ms cubic-bezier(.16,1,.3,1)
- sheet = 300ms cubic-bezier(.16,1,.3,1)
- toggle thumb = 200ms

Verify card direction:
- next enters from +24px
- previous enters from -24px

Reduced-motion mode:
- animations disabled
- transitions disabled
- smooth scrolling disabled/converted to auto where the web CSS applies reduced-motion override

## 14. Performance/interaction quality checks for future native implementation

These are acceptance checks, not new visual features:
- no dropped touch ownership between vertical scroll and horizontal paging
- no scroll-position jump after card change
- no duplicated tap handling after recompose/re-render
- bottom-sheet content scroll remains independent from background
- changing sliders does not dismiss/recreate the sheet in a visibly disruptive way
- long Arabic items remain responsive
- state persistence must not block interaction

## 15. Phase-0 completion criteria

Phase 0 is complete when:
- `NATIVE_UI_CONTRACT.md` exists
- `NATIVE_SCREEN_MATRIX.md` exists
- `NATIVE_QA_PLAN.md` exists
- the documents are committed outside `azkar-web`
- no existing Android file was changed
- no build, APK, deployment, Render/Webflow, or screenshot-automation operation was run
- work stops before Phase 1
