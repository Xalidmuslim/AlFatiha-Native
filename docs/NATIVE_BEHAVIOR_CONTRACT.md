# Native Behavior Contract — Azkar

Phase: **0.5 forensic audit + 0.6 approved canonical behavior**

Source under audit:
- repository: `Xalidmuslim/AlFatiha-Native`
- web source branch: `azkar-web`
- files: `app-core.js`, `data-1.js`, `data-2.js`
- Phase-0 audit base commit: `d20989c4bfbd5e5fc88215d59ddaaf32ce74c323`

This document separates:
- **A — USER_APPROVED_BEHAVIOR**: behavior explicitly approved by the project owner and not inferred from broken source;
- **B — CANONICAL_BEHAVIOR**: behavior that is factually active in the current Golden Master source;
- **C — WEB_BUG**: technical defects in the web source that native must not automatically copy.

Dead/overridden code is not promoted to a native requirement.

---

## 1. Critical finding

### WEB_BUG — current `app-core.js` does not parse

The current `azkar-web/app-core.js` is syntactically invalid.

Confirmed invalid fragment:

- line 383: `matchMedia('(prefers-color-scheme: dark)').matches);`
- lines 384–385 reference `dark` without a surrounding declaration in that fragment
- line 386 is an unmatched closing brace

Static JavaScript parsing fails with:

`SyntaxError: Unexpected token ')'`

Removing only lines 383–386 makes the remaining file syntactically parseable. This proves the parse blocker is real and localized to that orphan fragment; Phase 0.5 does **not** modify it.

### CANONICAL_BEHAVIOR — current browser runtime consequence

Because JavaScript source is parsed before execution, a syntax error prevents **the entire `app-core.js` script from executing**.

Therefore, in the exact current source revision:

- no `state` object is initialized by `app-core.js`;
- no `render()` executes;
- no dhikr UI is rendered by `app-core.js`;
- no Settings/Contents/Explanation behavior is attached;
- no count buttons are attached;
- no swipe handlers are attached;
- no history initialization runs;
- no `popstate` handler is registered;
- no theme change handler is registered;
- no service-worker registration from `app-core.js` runs.

`data-1.js` and `data-2.js` are independently valid data scripts, but their arrays are not consumed by `app-core.js` while the parse error exists.

**Native must not copy “blank/non-running app” as behavior. This is WEB_BUG, not product behavior.**

---

## 2. Duplicate declaration inventory

There are **9 duplicated function names** in `app-core.js`.

| Function | Earlier declaration | Later declaration | Valid-JS overwrite result | Current exact runtime |
|---|---:|---:|---|---|
| `render` | line 72 | line 388 | later declaration wins | none; script fails parse |
| `renderCard` | 128 | 444 | later wins | none |
| `renderModal` | 141 | 457 | later wins | none |
| `renderSettings` | 167 | 483 | later wins | none |
| `fontTile` | 188 | 504 | later wins | none |
| `slider` | 192 | 508 | later wins | none |
| `toggle` | 193 | 509 | later wins | none |
| `bindEvents` | 237 | 511 | later wins | none |
| `goTo` | 343 | 567 | later wins | none |

### JavaScript resolution rule

If the parse blocker were removed **without any other code change**, duplicate function declarations in the same script scope are hoisted and the later declaration for the same name becomes the binding used throughout the script.

Therefore the earlier implementations above are **overridden/dead as callable function bodies** in the minimally parse-repaired file.

This overwrite analysis is diagnostic only. It is **not** the current canonical runtime because the current file does not parse.

---

## 3. Constants and variables

Top-level declarations:

| Declaration | Line | Duplicate top-level declaration? |
|---|---:|---|
| `const ADHKAR` | 1 | No |
| `const DEFAULT_SETTINGS` | 3 | No |
| `const ARABIC_FONTS` | 16 | No |
| `const RUSSIAN_FONTS` | 22 | No |
| `const state` | 31 | No |

Result:
- duplicate top-level constants: **0**
- duplicate top-level `let`: **0**
- duplicate top-level `var`: **0**

Repeated local names such as `list`, `item`, `key`, `value`, `dx`, `dy` occur in separate function/local scopes and are not duplicate global declarations.

---

## 4. Event-handler inventory

### Top-level listeners present in source

| Listener | Source location | Current exact runtime |
|---|---:|---|
| `window.popstate` | line 369 | not registered; script fails parse |
| system dark-mode `change` | line 576 | not registered |
| `window.load` for service worker | line 577 | not registered |

There is only one explicit top-level `popstate` registration.

There is **no confirmed double-registration of the same top-level listener** caused merely by the duplicate function declarations.

### `bindEvents` duplication

Two complete `bindEvents` function bodies exist.

If the syntax blocker alone were removed:
- the later `bindEvents` at line 511 would be the function called by `render()`;
- the earlier `bindEvents` at line 237 would not be called through the `bindEvents` name.

Therefore the duplicated function itself does **not** mean both sets of per-DOM listeners are registered simultaneously.

However, the later implementation is behaviorally different from the earlier one and conflicts with the top-level history code that remains in the file. That hybrid is a WEB_BUG risk.

---

## 5. SWIPE

## 5.1 CANONICAL_BEHAVIOR — exact current source

There is **no active swipe implementation** because `app-core.js` fails to parse.

No `touchstart`, `touchmove`, `touchend`, or `touchcancel` callback from this file is registered in the current exact source.

This is a WEB_BUG outcome and must not be copied to native.

## 5.2 Diagnostic only — implementation that would win after only removing the parse blocker

The late `bindEvents` declaration would win.

Its swipe behavior is:

### touchstart
- attached to `#cards` only when `state.viewMode === 'cards'`;
- passive listener;
- stores only:
  - `x`
  - `y`
- no gesture mode is stored;
- no left-edge flag is stored.

### touchmove
- **none** in the late implementation;
- no gesture classification while moving;
- no `preventDefault()`.

### touchend
- passive listener;
- computes:
  - `dx = endX - startX`
  - `dy = endY - startY`
- clears `state.touch`;
- rejects gesture when:
  - `abs(dx) < 55`, or
  - `abs(dx) <= abs(dy) * 1.15`
- accepted left swipe (`dx < 0`) calls next;
- accepted right swipe calls previous.

### horizontal threshold
- minimum horizontal distance: **55px**
- horizontal dominance: **strictly greater than vertical distance × 1.15**

### vertical-vs-horizontal detection
- done only at `touchend`;
- there is no live mode lock during `touchmove`.

### edge gesture
- the late implementation has **no 28px left-edge reservation**;
- the earlier overwritten implementation did have `t.clientX < 28`, but that is not the late winner.

### preventDefault
- the late implementation never calls `preventDefault()`;
- browser/document scrolling remains allowed during the gesture.

### touchcancel
- the late implementation has no `touchcancel` handler.
- the earlier overwritten implementation had one.

### animation direction
The late `goTo` sets:
- `direction = 'next'` when target index is greater;
- `direction = 'prev'` when target index is lower.

The renderer applies `slide-next` / `slide-prev`.

Phase-0 CSS contract defines:
- duration: **620ms**
- easing: `cubic-bezier(.16,1,.3,1)`
- next enters from +24px
- previous enters from -24px

### scroll position after changing dhikr
Late `goTo` behavior, if parse-fixed:
1. clamps the requested index;
2. if target equals current index, calls `scrollToReading(true)`;
3. otherwise calls `scrollToReading(false)` **before** changing the index;
4. `scrollToReading` scrolls the **document/window** so `#reading-start` is approximately 4px below the window top reference;
5. on the next animation frame, updates `activeIndex`, saves, and re-renders.

Therefore this candidate implementation deliberately resets document position toward the reading toolbar when changing cards.

### where a long dhikr scrolls
From the Phase-0 CSS contract:
- in `cards` / `.paged` mode, the `.dhikr-card` itself has a max height and `overflow-y:auto`; a long dhikr scrolls **inside the card**;
- in `list` mode, the paged max-height/overflow rule does not apply, so long content participates in the **document/page scroll**.

Again, these details describe the late implementation that would win after a minimal parse repair. They are **not current runtime behavior** while the syntax error remains.

---

## 6. ANDROID / BROWSER BACK

## 6.1 CANONICAL_BEHAVIOR — exact current source

There is **no active Back/history behavior from `app-core.js`** because the file fails to parse.

Specifically, none of these execute:
- `history.scrollRestoration='manual'`
- initial `history.replaceState(...)`
- `window.addEventListener('popstate', ...)`

No modal is opened by this script, so there is also no active modal-back behavior.

This is a WEB_BUG state and must not be copied to native.

## 6.2 Diagnostic only — effective hybrid after removing only the parse blocker

The top-level history code would remain active, but the later `bindEvents` and later `goTo` would override the history-aware earlier functions.

### Top-level side effects that would still execute
- `history.scrollRestoration = 'manual'`
- if current history state is not Azkar state: `history.replaceState(uiHistoryState(),'')`
- otherwise state fields may be restored from existing history state
- one `popstate` listener would be registered

### But user actions in the late handlers do not create history entries

Late handlers:
- Settings: set `modal='settings'`, render; **no pushState**
- Contents: set `modal='contents'`, render; **no pushState**
- Explanation: set `selectedId`, `modal='insight'`, render; **no pushState**
- Close buttons/overlay: clear modal and render directly
- period switch: changes period/index/progress and renders; **no pushState**
- card navigation: late `goTo` changes index and renders; **no pushState**
- mode switch: changes mode and renders; **no history.replaceState**
- contents jump: **no history state write**

Therefore, after only a parse repair, the history-aware architecture would be internally disconnected from the UI actions.

### Back from Settings / Contents / Explanation
No matching same-document history entry is pushed when opening them.

Typical browser/system Back would therefore navigate to the previous browser history entry rather than closing the modal through Azkar history.

### Back to previous dhikr
Card navigation does not push state in the late `goTo`.

Therefore browser/system Back does not provide a reliable “previous dhikr” history chain.

### Back on first dhikr
No special browser-history interception exists in the late UI handler path.

A right swipe at the first card, however, clamps to index 0 and late `goTo` calls `scrollToReading(true)`; that is swipe-boundary behavior, not browser Back behavior.

### Back after switching Morning/Evening
The late period handler does not push history.

Therefore period switching does not create a same-document Back target.

### popstate
The source has one top-level `popstate` callback that can restore:
- period
- viewMode
- activeIndex
- direction
- modal
- selectedId

But in the minimally parse-repaired hybrid, ordinary late UI actions do not create the corresponding history entries, so the callback is largely disconnected from those actions.

This conflict is **WEB_BUG**, not a native requirement.

---

## 7. State contract

## 7.1 CANONICAL_BEHAVIOR — exact current source

Because `app-core.js` does not execute, none of its runtime state is initialized.

## 7.2 Static state definition present in source

If the parse blocker were removed, initial state definition is:

| Field | Source definition |
|---|---|
| `period` | evening when hour >=16 or <4, else morning |
| `viewMode` | localStorage `azkar-view-mode`, fallback `cards` |
| `activeIndex` | 0 |
| `settings` | `azkar-settings` merged over defaults |
| `progress` | current-date progress object |
| `modal` | null |
| `selectedId` | null |
| `touch` | null |
| `direction` | `next` |

Persistence helper `save()` writes:
- `azkar-view-mode`
- `azkar-settings`
- current-date progress key

Progress key:
`azkar-progress-YYYY-MM-DD`

### Late-handler state transitions after minimal parse repair

These are diagnostic, not current runtime requirements:

- period button:
  - assigns period
  - resets `activeIndex=0`
  - sets `direction='next'`
  - reloads current-date progress
  - saves and renders
- mode switch:
  - assigns viewMode
  - saves
  - clears modal
  - renders
  - smooth-scrolls document to reading toolbar
- card change:
  - sets direction
  - updates activeIndex on next animation frame
  - saves and renders
- settings:
  - saved immediately
- modal:
  - set directly by late handlers; no history push
- selectedId:
  - set when opening insight
  - late modal close does not explicitly clear it
- touch:
  - late swipe stores only start x/y
  - cleared on touchend
  - no late touchcancel cleanup

---

## 8. Content audit

Both data files are valid and parse as data.

### Unique records
- total records: **16**
- unique ids: **16**
- duplicate ids: **0**

### Period visibility
- morning: **14**
- evening: **13**

### Evening-specific Arabic/translation
Both `eveningArabic` and `eveningTranslation` exist on:
- `kingdom`
- `by-you`

Count: **2**

### disputed=true
Count: **4**
- `bismillah-protection`
- `pleased`
- `muawwidhat`
- `perfect-words`

### note
Count: **10**
- `tahlil-ten`
- `tahlil-hundred`
- `kingdom`
- `by-you`
- `bismillah-protection`
- `pleased`
- `fatir`
- `muawwidhat`
- `perfect-words`
- `baqarah-last-two`

### insight
Count: **16**
- all records have `insight`

### repetition counts
| Target count | Number of records |
|---:|---:|
| 1 | 11 |
| 3 | 2 |
| 10 | 1 |
| 100 | 2 |

This content inventory is valid source data and can remain a Phase-1 input once the runtime-behavior blocker is resolved.

---

## 9. A / B / C classification

## A — USER_APPROVED_BEHAVIOR

Phase 0.5 does not infer approval from duplicated or broken JavaScript.

The following are safe project facts, not choices between duplicate implementations:
- two periods: Morning / Evening;
- two reading modes are represented in source: Cards / List;
- persisted settings and daily progress are part of the source model;
- the 16-record content inventory above is valid;
- the visual contract remains governed by the approved web Golden Master audit.

Where the source contains two conflicting interaction implementations, Phase 0.5 does **not** declare either one user-approved merely because it appears earlier or later in the file.

## B — CANONICAL_BEHAVIOR

For the exact current `azkar-web/app-core.js`:
- parse fails;
- `app-core.js` contributes no active runtime behavior.

This is the only strictly factual current browser-execution conclusion available from the exact source.

## C — WEB_BUG

Confirmed:
1. syntax-invalid orphan fragment at lines 383–386;
2. nine duplicated function names;
3. two conflicting swipe implementations;
4. history-aware earlier handlers coexist with later handlers that bypass history;
5. one top-level `popstate` architecture would remain after a minimal parse repair but late user actions would not create matching states;
6. late swipe implementation has no edge reservation, no `touchmove` mode lock, no `preventDefault`, and no `touchcancel` cleanup while the earlier overwritten implementation contains all four;
7. the file therefore cannot be treated as a reliable single canonical interaction specification without resolving the web defect or explicitly choosing intended behavior.

Not confirmed:
- no evidence that both `bindEvents` implementations are simultaneously registered;
- no evidence of duplicate top-level `popstate` registration;
- duplicate function declarations alone do not cause double DOM listeners because the later function binding would replace the earlier one in a syntactically valid script.

---

## 10. DO_NOT_COPY_WEB_BUGS

Native must **not** copy the following defects:

1. **Do not copy the syntax error / non-running app state.**
2. **Do not reproduce duplicate function architecture.**
3. **Do not combine the early history model with the late no-history UI handlers.**
4. **Do not choose the late swipe implementation merely because JavaScript declaration overwrite would make it win after a minimal repair.** That would confuse source-order accident with product intent.
5. **Do not intentionally remove Android system-back compatibility based on the broken late web handler path.**
6. **Do not copy the absence of touch-cancel cleanup as a design requirement.**
7. **Do not copy conflicting gesture ownership between horizontal paging and browser/system edge gestures without an explicit product decision.**
8. **Do not treat the current blank/non-rendering runtime as Golden Master behavior.**

---

## 11. Phase 0.6 resolution of the Phase-1 behavior blocker

Phase 0.5 concluded that Phase 1 could not safely begin from web-runtime inference alone because:

- `app-core.js` does not parse;
- duplicate interaction implementations conflict;
- swipe/back/history/scroll semantics could not be selected from source order without guessing.

Phase 0.6 resolves **that behavioral specification blocker** through explicit project-owner approval.

The authoritative native behavior is now defined in:

`docs/NATIVE_CANONICAL_BEHAVIOR.md`

In particular, Phase 0.6 now canonically defines:

- «По одному» as the default one-dhikr reader;
- stable outer layout with vertical scrolling inside the active reading area;
- new dhikr opening at reading scroll `0`;
- horizontal swipe threshold/dominance and gesture-safety requirements;
- Android system-edge Back ownership;
- Back priority: sheet → previous dhikr history → standard Android navigation;
- Contents, Settings, and Explanation as bottom sheets;
- the 620ms directional slide/fade target with reduced-motion support;
- native state and persistence semantics;
- Morning/Evening switching semantics;
- explicit WEB_BUG behaviors that must not be copied.

This does **not** execute or start Phase 1. It only removes the specific ambiguity identified by Phase 0.5.

Phase 0.6 remains documentation-only:

- no `azkar-web` changes;
- no `app-core.js` repair;
- no Android implementation changes;
- no Android code;
- no builds;
- no APK;
- no Render/Webflow/Floot.

Phase 0.5 diagnostic material above remains preserved for audit history, but it is subordinate to the Phase 0.6 approved canonical behavior for future native implementation decisions.
