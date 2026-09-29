# Native UI Contract — Azkar

Source of truth: `azkar-web` branch, files `index.html`, `styles.css`, `app-core.js`, `data-1.js`, `data-2.js`.

This document is a transcription of the current web golden master. It does not introduce new visual decisions.

## 1. Global shell

- Viewport: `width=device-width, initial-scale=1, viewport-fit=cover`.
- Light browser/theme color: `#f4f0e7`.
- Dark browser/theme color: `#111613`.
- Root background follows `--bg`; body margin is `0`.
- Body font: `var(--reader-font)`.
- Font smoothing: `-webkit-font-smoothing: antialiased`.
- Main container `.app-shell`:
  - width: `min(100%, 760px)`
  - centered with `margin:auto`
  - padding: `0 10px calc(44px + env(safe-area-inset-bottom))`

## 2. Color tokens

### Light
| Token | Value |
|---|---|
| `--bg` | `#f4f0e7` |
| `--fg` | `#171c19` |
| `--card` | `#fbf8f1` |
| `--surface` | `#eee8dc` |
| `--muted` | `#646a66` |
| `--border` | `#d8d0c1` |
| `--primary` | `#1f5c48` |
| `--accent` | `#dce8e1` |
| `--warning` | `#9d6a20` |
| `--success` | `#2f6b52` |
| `--shadow` | `0 2px 4px rgba(24,31,27,.05), 0 14px 32px rgba(24,31,27,.06)` |

### Dark
| Token | Value |
|---|---|
| `--bg` | `#111613` |
| `--fg` | `#edf2ee` |
| `--card` | `#1a211c` |
| `--surface` | `#222a24` |
| `--muted` | `#aab5ad` |
| `--border` | `#323c35` |
| `--primary` | `#7fb99e` |
| `--accent` | `#24382e` |
| `--warning` | `#c99b58` |
| `--success` | `#74ad90` |
| `--shadow` | `0 6px 22px rgba(0,0,0,.28)` |

Derived colors use CSS `color-mix` exactly as follows:
- done card border: success 40% + border
- done marker/outline: success 35% + border
- done marker background: success 10% + card
- note border: warning 26% + border
- note background: warning 6% + surface
- sticky toolbar background: card 94% + transparent
- active segmented/list button background: primary 5% + card
- bottom-sheet handle: fg 60% + transparent

Static colors:
- primary action text in light theme: `#fff`
- dark `.count-btn` text: `#0f1713`
- overlay: `rgba(9,12,10,.44)`
- bottom-sheet shadow: `0 -12px 40px rgba(0,0,0,.18)`
- active period shadow: `0 1px 4px rgba(0,0,0,.06)`

## 3. Typography

Fonts loaded by the web golden master:
- Amiri 400/700
- Inter 400/500/600/700
- Literata optical sizes 7..72, weights 400/600/700
- Manrope 400/500/600/700
- Noto Naskh Arabic 400/500/600/700
- Noto Sans Arabic 400/500/600/700
- PT Serif 400/700
- Scheherazade New 400/500/600/700

Default runtime settings:
- Arabic font: `"Noto Naskh Arabic", serif`
- Russian/reader font: `"Literata", serif`
- Arabic size: `32px`
- Russian size: `17px`
- reader line-height: `1.65`
- reader style: `book`

Russian font options exposed in Settings:
1. Literata — “Книжный”
2. PT Serif — “Классика”
3. Inter — “Современный”
4. Manrope — “Компактный”
5. system-ui — “Android Sans”
6. serif — “Android Serif”

Arabic font options exposed in Settings:
1. Noto Naskh Arabic — “Чёткий”
2. Noto Sans Arabic — “Очень читаемый”
3. Amiri — “Классический”
4. Scheherazade New — “Мягкий”

### Fixed text styles
- Brand title: Literata, 700, `23px/1.05`
- Brand subtitle: Inter, 500, `10px/1`, uppercase, letter-spacing `.11em`
- Source note: Inter, 400, `11px/1.45`
- Period button: Inter, 600, `13px`
- Progress label: Inter, 500, `11px`; progress count `13px`
- Reset text button: Inter, 600, `10px`
- Toolbar button: Inter, 600, `10px`
- Toolbar position: Inter, 500, `10px`
- Card number: ui-monospace, 500, `10px`
- Card heading: reader font, 700, `15px/1.3`
- Dispute badge: Inter, 600, `9px`
- Arabic body: selected Arabic font, selected size, weight 500, selected line-height, RTL
- Translation: selected reader font, selected size, selected line-height
- Source row: Inter, 400, `10px/1.45`; label inherits primary
- Note box: Inter, 400, `10px/1.45`
- Explain button: Inter, 600, `10px/1.2`
- Counter: Inter, 500, `12px`; numeric value `20px`
- Count button: reader font, 600, `14px`
- Pager button: Inter, 600, `12px`
- Pager center: Inter, 500, `9px`; active number `14px`; helper `7px`
- Footer: Inter, 400, `9px/1.5`
- Sheet eyebrow: Inter, 600, `8px`, letter-spacing `.08em`
- Sheet title: Literata, 700, `18px/1.2`
- Sheet subtitle: Inter, 400, `10px/1.35`
- Contents title: Inter, 600, `11px/1.25`
- Contents subtitle: Inter, 400, `8px`
- Insight/settings section heading: Inter, 700, `11px`
- Insight body: reader font, 400, `13px/1.58`
- Insight references: Inter, 500, `9px/1.35`
- Settings font tile title: `11px/1.1`
- Settings font tile subtitle: Inter, 400, `7px/1.1`
- Settings font tile sample: `10px/1.05`; Arabic sample `17px`
- Slider/control labels: Inter, 500, `8px`; bold value `9px`
- Toggle text: Inter, 600, `8px`
- Theme row title: Inter, 600, `9px`; subtitle Inter, 400, `7px`
- Theme select: Inter, 500, `9px`

## 4. Spacing, padding, radius, borders and shadows

### Top bar
- min-height: `72px`
- horizontal/vertical padding: `10px 2px`
- brand gap: `10px`
- brand icon: `40x40`, border `1px solid var(--border)`, radius `13px`
- settings icon button: `42x42`, border 1px, radius `12px`
- compact icon button: `36x36`

### Source note
- padding: `10px 11px`
- border: 1px
- radius: `12px`

### Period tabs
- margin: `12px 0 10px`
- internal padding: `4px`
- gap: `6px`
- border: 1px
- radius: `12px`
- child min-height: `42px`
- child radius: `9px`

### Progress card
- padding: `10px 12px`
- margin-bottom: `10px`
- border: 1px
- radius: `14px`
- shadow: `var(--shadow)`
- header margin-bottom: `9px`
- track height: `5px`; radius `999px`

### Reading toolbar
- sticky top: `calc(5px + env(safe-area-inset-top))`
- z-index: `20`
- columns: `auto 1fr auto`
- gap: `8px`
- padding: `6px`
- margin-bottom: `10px`
- border: 1px
- radius: `13px`
- shadow: `var(--shadow)`
- backdrop blur: `12px`
- toolbar button min-height: `36px`, radius `9px`, horizontal padding `10px`

### Card list
- gap: `10px`
- `overflow-anchor:none`

### Dhikr card
- default padding: `15px 14px 13px`
- compact padding: `12px`
- border: 1px
- radius: `15px`
- shadow: `var(--shadow)`
- scroll-margin-top: `62px`
- paged mode max-height: `calc(100dvh - 172px)`
- paged mode vertical overflow: auto
- paged mode uses stable scrollbar gutter and momentum scrolling
- completed card has a left 3px success strip

### Card header
- grid columns: `auto minmax(0,1fr) auto`
- gap: `9px`
- number chip: `29x29`, radius `8px`, border 1px
- heading top margin: `2px`
- dispute badge top margin: `5px`, padding `3px 7px`, radius `999px`
- done marker: `28x28`, radius 50%, border 1px

### Arabic/translation/source/note
- Arabic margin: `18px 0 14px`
- translation padding-top: `12px`, top border 1px
- source row margin-top: `12px`, padding-top `10px`, dashed top border 1px, grid gap `8px`
- note margin-top: `9px`, padding `9px 10px`, gap `7px`, radius `10px`, border 1px

### Explain/counter/actions
- explain button: margin-top `10px`, min-height `40px`, radius `10px`, border 1px
- counter row: margin-top `12px`, gap `12px`
- count button: min-width `150px`, min-height `48px`, radius `14px`, horizontal padding `18px`
- disabled count button uses surface/muted colors

### Pager
- grid columns: `1fr auto 1fr`
- gap: `8px`
- margin-top: `10px`
- padding: `8px`
- border: 1px
- radius: `13px`
- child min-height: `44px`, radius `11px`
- disabled opacity: `.38`
- center minimum width: `70px`

### Footer
- padding: `18px 8px 4px`

## 5. Bottom sheets

Common overlay:
- fixed, `inset:0`
- z-index: `100`
- dark translucent background `rgba(9,12,10,.44)`
- sheet anchored to bottom and centered horizontally

Common sheet:
- width: `min(100%,760px)`
- max-height: `82dvh`
- overflow: auto
- background: card
- border: 1px, bottom border removed
- top radii: `24px 24px`
- shadow: `0 -12px 40px rgba(0,0,0,.18)`
- overscroll contained
- entry animation: `sheetUp 300ms cubic-bezier(.16,1,.3,1)`

Handle:
- `42x4`
- margin: `8px auto 0`
- radius: `999px`

Close button:
- absolute top `34px`, right `14px`
- `30x30`
- no border/background
- font-size `27px`, line-height 1

Sheet header:
- padding: `9px 16px 7px 16px`
- compact settings header adds `padding-top:7px`

Specific max-heights:
- contents: common `82dvh`
- insight: `84dvh`
- settings: `72dvh`

### Contents sheet
Mode switch:
- sticky top 0, z-index 2
- two equal columns
- gap 5px
- margin `0 13px 7px`
- padding 5px
- border 1px
- radius 11px
- backdrop blur 10px
- buttons min-height 36px, radius 8px

Contents list:
- gap 5px
- padding `0 13px calc(14px + env(safe-area-inset-bottom))`

Contents item:
- min-height 50px
- columns: `auto minmax(0,1fr) auto`
- gap 9px
- padding `7px 8px`
- border 1px
- radius 10px
- active state uses primary border + inset 1px primary outline + primary 5% background

### Insight sheet
- body gap: 8px
- padding: `0 14px calc(18px + env(safe-area-inset-bottom))`
- section padding: 11px
- section border: 1px
- radius: 12px
- section background: surface
- scholar row: columns 24px + 1fr, gap 8px, margin-top 7px
- scholar index: `24x24`, radius 7px, border 1px
- references use vertical flex with 5px gap

### Settings sheet
- body gap: 8px
- padding: `0 13px calc(14px + env(safe-area-inset-bottom))`
- live preview: gap 3px, padding `6px 8px`, border 1px, radius 10px
- generic settings section gap: 6px
- font grid gap: 5px
- Russian font grid: 3 columns
- Arabic font grid: 2 columns
- font tile: min-height 62px, gap 1px, padding `6px 7px`, border 1px, radius 9px
- slider grid: 2 equal columns
- control: gap 6px, padding `7px 8px`, border 1px, radius 10px
- style grid: 2 equal columns, gap 5px
- toggle grid: 3 equal columns, gap 5px, padding 6px, border 1px, radius 10px
- toggle item: min-height 42px, gap 5px, padding `5px 6px`, border 1px, radius 8px
- switch track: `30x17`, radius 999px
- switch thumb: `13x13`, top/left 2px; checked left 15px
- theme row: spans full grid width; gap 10px; padding `5px 7px`; border 1px; radius 8px
- theme select: max-width 120px; padding 6px; border 1px; radius 8px

## 6. Cards and states

Each card may contain:
1. sequential two-digit number
2. title
3. optional “есть разногласие” badge
4. optional completed check marker
5. Arabic text
6. optional translation
7. optional source
8. optional note
9. optional “Разъяснение · история · слова учёных” action
10. counter/progress action

Completed state:
- card border is success-mixed
- left 3px success strip
- done circle shown
- counter label becomes “Выполнено”
- count button disabled and labelled “✓ Готово”

Count button:
- if target count = 1: “Прочитано”
- otherwise: “+1 · осталось N”

## 7. Toolbar and navigation

Top toolbar:
- brand on left, settings icon on right

Reading toolbar:
- sticky
- “Содержание” on left
- current position in center
- compact settings button on right

View modes:
- `cards`: one dhikr at a time + pager + horizontal swipe
- `list`: all visible dhikrs in one vertical list, pager hidden

Horizontal swipe contract in current primary history-aware block:
- touch start on the left `28px` edge is reserved and not used for page swipe
- gesture classification begins after >10px movement
- horizontal if `abs(dx) > abs(dy) * 1.15`
- while classified horizontal, vertical browser scrolling is prevented
- page change threshold: `55px`
- left swipe = next; right swipe = previous
- touch cancel clears gesture state

History/back contract present in the first implementation block:
- opening a modal pushes history state
- changing period pushes history state
- moving to another card pushes history state
- back/popstate restores period, viewMode, activeIndex, modal and selected insight id

Important source condition: `app-core.js` also contains a later duplicated older implementation of rendering/event logic. Phase 0 does not resolve which duplicate should be removed; native work must not silently invent behavior. This must be verified before Phase 1.

## 8. Settings behavior

Persistent settings:
- Arabic font
- Russian font
- Arabic size
- Russian size
- line-height
- reader style
- show translation
- show sources
- show notes
- theme

Slider ranges:
- Arabic size: 22..44, step 1
- Russian size: 13..25, step 1
- line height: 1.25..2.00, step 0.05

Live preview caps:
- Arabic preview size = min(selected Arabic size, 24px)
- Russian preview size = min(selected Russian size, 15px)

Theme:
- values: auto / light / dark
- auto follows `prefers-color-scheme`
- light applies light tokens
- dark applies dark tokens
- CSS theme transition is not defined

Reader style:
- book = default card padding
- compact = card padding 12px

Visibility toggles:
- translation
- sources
- notes

## 9. Animations and motion

- progress width transition: `350ms ease`
- card next animation: `620ms cubic-bezier(.16,1,.3,1)`
  - from opacity .62, translateX(+24px), scale(.994)
- card previous animation: `620ms cubic-bezier(.16,1,.3,1)`
  - from opacity .62, translateX(-24px), scale(.994)
- bottom sheet entry: `300ms cubic-bezier(.16,1,.3,1)`
  - from translateY(18px), opacity .7
- toggle thumb: `200ms`
- list jump uses JS `scrollIntoView({behavior:'smooth'})`
- `html` itself has `scroll-behavior:auto`

Reduced motion:
- under `prefers-reduced-motion: reduce`, all animations/transitions are disabled and scroll behavior is forced to auto

## 10. Responsive rule

At `max-width:370px`:
- toolbar button label span is hidden
- counter row becomes vertical/stretch
- count button width becomes 100%
- pager helper text is hidden
- Russian font grid changes from 3 columns to 2

No other CSS breakpoint is defined in the golden master.

## 11. Data-driven visual cases

Golden master data contains 16 records total.
- Morning-visible records: 14
- Evening-visible records: 13
- Some items are morning-only or evening-only.
- Some items carry `disputed:true`, which shows the warning badge only when notes are enabled.
- Items may have `note`, `insight`, evening-specific Arabic/translation, and varying count targets (1, 3, 10, 100).

Native UI must render these states from data rather than hard-coding isolated screens.
