# Native Canonical Behavior — Azkar

Phase: **0.6 — User-Approved Canonical Behavior**

Repository: `Xalidmuslim/AlFatiha-Native`  
Web Golden Master branch: `azkar-web`  
Phase 0 commit: `d20989c4bfbd5e5fc88215d59ddaaf32ce74c323`  
Phase 0.5 commit: `f312c7acc97ab4b741430a5fed72d7826bc6868b`

## Authority and precedence

Phase 0.5 established that the current `azkar-web/app-core.js` is syntactically invalid and contains duplicated/conflicting interaction implementations.

This document records the **project-owner-approved native behavior** and is authoritative for native interaction semantics whenever it conflicts with broken, duplicated, overridden, dead, or ambiguous web JavaScript.

Priority order for native behavior:

1. **This Phase 0.6 document — USER_APPROVED_CANONICAL_BEHAVIOR**
2. Valid non-conflicting structural/content findings from Phase 0 and Phase 0.5
3. Web implementation details only where they do not conflict with this document
4. Broken/duplicated web behavior — never a native requirement

Phase 0.6 does **not** modify web or Android implementation and does **not** start Phase 1.

---

## 1. Single-item mode — «По одному»

Default reading mode:

- exactly one dhikr is presented at a time;
- horizontal swipe changes dhikr;
- «Назад» and «Далее» perform the same previous/next navigation;
- position indicator is shown as `X из N`.

### Required top-of-item behavior

Every newly opened dhikr must begin at its first lines.

Native must not:

- carry the previous dhikr's vertical scroll position into the new dhikr;
- initially render the middle of a newly opened dhikr and then visibly scroll upward;
- cause a noticeable vertical `document/window`-style jump during dhikr navigation.

Canonical result:

- previous dhikr reading position and next dhikr initial position are independent;
- a newly selected dhikr opens at reading-area scroll position `0`.

---

## 2. Scroll ownership

### «По одному»

The outer application screen remains visually stable.

Required behavior:

- sticky reader toolbar remains in place;
- a long dhikr scrolls vertically inside its own reading area;
- changing to another dhikr resets that new dhikr's reading-area vertical scroll to `0`;
- outer page/root content must not jump vertically as a side effect of card navigation.

Gesture ownership must preserve vertical reading inside the active reading area.

### «Список»

Required behavior:

- normal vertical scrolling of the dhikr list;
- selection through «Содержание» smoothly scrolls to the selected dhikr;
- list scrolling is not converted into paged-card behavior.

---

## 3. Horizontal swipe

Canonical gesture parameters:

- touch/slop threshold: approximately **48–56dp**;
- target threshold: approximately **55dp** is acceptable;
- horizontal movement must clearly dominate vertical movement;
- target dominance ratio: approximately **1.15**.

A swipe qualifies as horizontal only when the horizontal component clearly dominates the vertical component.

Required protections:

- a small diagonal vertical reading gesture must not change dhikr;
- vertical reading of long text must not accidentally trigger horizontal paging;
- once a horizontal swipe is confirmed, vertical reading scroll must no longer move for that gesture;
- the pager/reader owns the confirmed horizontal gesture;
- after successful paging, the newly opened dhikr begins at vertical scroll position `0`.

Native gesture handling must support:

- gesture cancellation;
- multi-touch safety;
- no double navigation from one gesture;
- no duplicate previous/next event after a single completed swipe.

This contract defines behavior, not a mandatory Android API implementation detail.

---

## 4. Android system edge Back

The Android left-edge system Back gesture must **not** be repurposed as dhikr paging.

Required native integration:

- use Android system Back;
- support predictive Back where available;
- use internal Back handling only for application navigation state that must be consumed before system navigation;
- do not add a custom left-edge swipe recognizer that conflicts with system Back.

A normal content swipe may navigate dhikr only when it is a reader gesture, not an Android system-edge Back gesture.

---

## 5. Canonical Back order

When Android system Back is invoked, resolve it in this order.

### Priority 1 — close an open bottom sheet

If any of these is open:

- Settings;
- Contents;
- Explanation;

then Back closes **only that sheet**.

It must return the user to the same reading context underneath.

### Priority 2 — previous dhikr

If:

- the user is on dhikr #2 or later; and
- the current dhikr was reached through sequential reading/navigation history;

then Back returns to the previous dhikr.

The restored dhikr is the previous navigation destination, not an arbitrary period/mode reset.

### Priority 3 — first dhikr

If the user is on the first dhikr and no sheet is open:

- use normal Android navigation behavior.

### Back must not

- unexpectedly close the app from the middle of reading when an internal previous-dhikr history entry exists;
- reset progress;
- reset reader settings;
- switch Morning/Evening unless an actual internal navigation history entry requires that restoration.

Period changes must not be invented as Back destinations when no corresponding history entry exists.

---

## 6. Contents — «Содержание»

«Содержание» must remain available while reading.

It opens as a bottom sheet.

Each visible dhikr row contains:

- number;
- title;
- repetition count;
- completed / not completed state;
- current-dhikr indication where applicable;
- direct navigation action.

When a dhikr is selected:

1. Contents sheet closes;
2. selected dhikr opens;
3. selected dhikr starts from its first line;
4. previous dhikr scroll position is not applied to the selected dhikr.

### List-mode navigation

In «Список» mode, selecting an item from Contents smoothly scrolls the list to that dhikr.

---

## 7. Reader Settings

Reader settings must be accessible directly while reading.

Settings open as a compact bottom sheet.

Required behavior:

- changes are previewed/applied immediately;
- current dhikr is preserved;
- opening Settings does not navigate away from the current dhikr;
- Back closes Settings and returns to the same dhikr at the same reading position.

Canonical settings:

- Arabic font;
- Russian font;
- Arabic text size;
- Russian text size;
- line-height;
- «Книжный» / «Компактный» reader style;
- translation visibility;
- source visibility;
- notes visibility;
- Light / Dark / System theme.

Reader settings are persistent state.

---

## 8. Explanation — «Разъяснение»

All **16** dhikr records have an Explanation entry.

Explanation opens as a bottom sheet.

Canonical sections:

- «Смысл»;
- «Связанный случай» — only when supported by an authentic/reliable transmitted report;
- «Примечания учёных»;
- «Источники разбора».

Back behavior:

- Back closes Explanation only;
- reading returns to the same dhikr and preserves the underlying reading context.

The application must not fabricate a «Связанный случай» section when reliable evidence is absent.

---

## 9. Dhikr transition animation

Canonical transition target:

- duration range: **550–650ms**;
- target duration: **620ms**;
- easing: smooth ease-out;
- target curve: `cubic-bezier(0.16, 1, 0.3, 1)`.

### Next dhikr

- subtle slide from right to left;
- subtle fade.

### Previous dhikr

- mirrored subtle slide from left to right;
- subtle fade.

Must not use:

- abrupt 200–300ms paging animation;
- bounce;
- visibly noticeable scale/zoom;
- vertical jump.

### Reduced Motion

Respect the system Reduce Motion / reduced-animation preference.

When reduced motion is enabled, navigation remains functionally identical without requiring the full transition animation.

---

## 10. Native state contract

Canonical native state includes:

- `period`;
- `viewMode`;
- `activeDhikrIndex` and/or stable `activeDhikrId`;
- `progress`;
- `readerSettings`;
- `activeSheet`;
- `selectedExplanation`;
- `readingScrollPosition` where appropriate.

### Persist

Persist:

- reader settings;
- selected view mode;
- daily progress.

### Daily progress

Daily progress is date-scoped.

Progress from one calendar date must not populate another date automatically.

### Reading scroll state

Do not persist one dhikr's vertical reading position and then apply it to a different dhikr.

Where reading position is preserved for an underlying dhikr while a temporary sheet is open, that position belongs only to that same dhikr/session context.

---

## 11. Period — «Утро» / «Вечер»

Confirmed content inventory:

- **16 unique dhikr records** total;
- **14** visible in Morning;
- **13** visible in Evening.

Confirmed evening-specific variants:

- `kingdom`;
- `by-you`.

When switching period:

1. select the first dhikr of the newly selected period;
2. set its reading scroll to top;
3. preserve reader settings;
4. preserve progress belonging to the other period/date;
5. do not destroy the other period's progress.

Period switching is an explicit state transition and must not accidentally inherit the prior period's active index/scroll position.

---

## 12. Do not copy web bugs

Native must categorically **not** reproduce:

- the current `app-core.js` `SyntaxError`;
- duplicated `render`;
- duplicated `renderCard`;
- duplicated `renderModal`;
- duplicated `renderSettings`;
- duplicated `bindEvents`;
- duplicated `goTo`;
- conflicting history implementations;
- missing `touchcancel` handling;
- the late no-history handler path as canonical behavior;
- document/window vertical jumps during dhikr navigation;
- accidental gesture ownership conflicts between reader paging and Android system Back.

Source-order overwrite in broken JavaScript is not product intent.

---

## 13. Phase 0.5 ambiguity resolution

Phase 0.5 blocked Phase 1 because native swipe/back/history/scroll behavior could not be safely inferred from the broken and conflicting web JavaScript.

Phase 0.6 resolves that ambiguity by explicit project-owner approval.

The following decisions are now authoritative:

- paged reader uses stable outer layout and internal reading-area vertical scroll;
- new dhikr always opens at top;
- horizontal swipe uses slop + dominance gating and must not hijack vertical reading;
- Android system edge Back remains system Back;
- sheet close has highest Back priority;
- previous-dhikr navigation history is second Back priority;
- first-dhikr Back falls through to standard Android navigation;
- Settings/Contents/Explanation are bottom sheets;
- 620ms ease-out directional slide/fade is the target transition;
- daily progress is date-scoped;
- settings and view mode persist;
- period switches reset active dhikr to the first visible item without destroying other-period progress.

This resolves the behavioral specification blocker only.

**Phase 1 is not executed by this document and must not begin as part of Phase 0.6.**

---

## 14. Phase 0.6 completion boundary

Phase 0.6 is documentation-only.

Allowed changes in this phase:

- create this canonical behavior document;
- update behavior-contract documentation to point to this approved specification.

Explicitly out of scope:

- modifying `azkar-web`;
- repairing `app-core.js`;
- modifying Android implementation;
- writing new Android code;
- running builds;
- generating APKs;
- using Render, Webflow, or Floot;
- starting Phase 1.
