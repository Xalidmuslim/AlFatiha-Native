# Native Screen Matrix — Azkar

Source: current `azkar-web` golden master. This matrix enumerates existing screens/states only; it does not add new native flows.

## 1. Primary surfaces

| ID | Surface/state | Entry | Required content | Main actions | Exit/back behavior to preserve |
|---|---|---|---|---|---|
| S01 | Main — Morning — Cards | App launch when local hour is 04:00–15:59, or tap “Утро” | Topbar, source note, period tabs, progress card, sticky reading toolbar, one dhikr card, pager, footer | Settings, contents, reset, count, insight, prev/next, horizontal swipe | Browser history-aware implementation records period/card changes |
| S02 | Main — Evening — Cards | App launch when local hour is 16:00–03:59, or tap “Вечер” | Same structure as S01 with evening-filtered data and evening text variants where present | Same as S01 | Same as S01 |
| S03 | Main — Morning — List | Choose “Список” from Contents while morning active | All 14 morning-visible dhikrs, no pager | Settings, contents, reset, count, insight, vertical scroll | View mode is persisted |
| S04 | Main — Evening — List | Choose “Список” from Contents while evening active | All 13 evening-visible dhikrs, no pager | Same as S03 | View mode is persisted |
| S05 | Contents bottom sheet — Cards mode | Reading toolbar → “Содержание” | Header, mode switch, one row per visible item, active item highlighting, completion checkmarks | Switch cards/list, jump to item, close | History-aware block pushes modal state; close uses back when applicable |
| S06 | Contents bottom sheet — List mode | Same, while list mode active | Same list; no “active current card” state unless cards mode is selected | Switch mode, jump, close | Jump in list mode renders then smooth-scrolls target into view |
| S07 | Settings bottom sheet | Topbar or reading toolbar settings button | Live preview, Russian fonts, Arabic fonts, three sliders, reader style, 3 visibility toggles, theme selector | Change font/size/line-height/style/toggles/theme, close | Changes save immediately |
| S08 | Insight bottom sheet | “Разъяснение · история · слова учёных” | Sheet header + meaning + optional context + scholar notes + references | Close | Selected item id is part of modal state in history-aware block |

## 2. Card-state matrix

Every main surface must support these existing card states.

| ID | Card state | Trigger/data condition | Visual contract |
|---|---|---|---|
| C01 | Unread, count=1 | progress < 1 and item.count=1 | Counter `0 / 1`; action label “Прочитано” |
| C02 | Unread, count>1 | progress < target | Numeric current count + target; action “+1 · осталось N” |
| C03 | Completed | progress >= item.count | Done border, 3px left success strip, circular ✓, “Выполнено”, disabled “✓ Готово” |
| C04 | Disputed | item.disputed=true and notes visible | Warning badge “есть разногласие” |
| C05 | Disputed hidden | item.disputed=true and notes disabled | Warning badge absent because badge is gated by `showNotes` |
| C06 | With note | item.note exists and notes visible | Warning-tinted note box |
| C07 | Note hidden | notes disabled | Note box absent |
| C08 | With insight | item.insight exists | Full-width explanation action present |
| C09 | Translation visible | showTranslation=true | Translation block with solid top border |
| C10 | Translation hidden | showTranslation=false | Translation block absent |
| C11 | Source visible | showSources=true | Dashed source row |
| C12 | Source hidden | showSources=false | Source row absent |
| C13 | Morning/evening variant | item has eveningArabic/eveningTranslation and evening period active | Use evening-specific Arabic + translation |
| C14 | Default text variant | morning period or no evening override | Use `arabic` + `translation` |

## 3. Data coverage

Current data set: 16 unique dhikr records.

### Part 1
| ID | Title | Period | Count | Disputed | Note | Insight | Evening text override |
|---|---|---|---:|---|---|---|---|
| sayyid-istighfar | Главная мольба о прощении | Morning + Evening | 1 | No | No | Yes | No |
| tahlil-ten | Тахлиль — 10 раз | Morning + Evening | 10 | No | Yes | Yes | No |
| tahlil-hundred | Тахлиль — 100 раз в начале дня | Morning only | 100 | No | Yes | Yes | No |
| tasbih-hundred | «Субхана-Ллахи ва бихамдихи» | Morning + Evening | 100 | No | No | Yes | No |
| kingdom | Утро и вечер принадлежат Аллаху | Morning + Evening | 1 | No | Yes | Yes | Yes |
| by-you | С Тобой мы встретили утро | Morning + Evening | 1 | No | Yes | Yes | Yes |
| bismillah-protection | С именем Аллаха — защита от вреда | Morning + Evening | 1 | Yes | Yes | Yes | No |
| afiyah | Просьба о благополучии и защите | Morning + Evening | 1 | No | No | Yes | No |

### Part 2
| ID | Title | Period | Count | Disputed | Note | Insight | Evening text override |
|---|---|---|---:|---|---|---|---|
| pleased | Доволен Аллахом как Господом | Morning + Evening | 1 | Yes | Yes | Yes | No |
| hayy-qayyum | О Живой, О Вседержитель | Morning + Evening | 1 | No | No | Yes | No |
| fatir | Творец небес и земли | Morning + Evening | 1 | No | Yes | Yes | No |
| muawwidhat | Аль-Ихляс, Аль-Фаляк и Ан-Нас | Morning + Evening | 3 | Yes | Yes | Yes | No |
| creation-count | По числу творений Аллаха | Morning only | 3 | No | No | Yes | No |
| fitrah | На естественной религии ислама | Morning only | 1 | No | No | Yes | No |
| perfect-words | Прибегаю к совершенным словам Аллаха | Evening only | 1 | Yes | Yes | Yes | No |
| baqarah-last-two | Последние два аята Аль-Бакара | Evening only | 1 | No | Yes | Yes | No |

Resulting visible counts:
- Morning: 14
- Evening: 13

## 4. Settings-state matrix

| ID | Setting | Default | Existing options/range | Immediate effect |
|---|---|---|---|---|
| ST01 | Arabic font | Noto Naskh Arabic | Noto Naskh, Noto Sans Arabic, Amiri, Scheherazade New | Arabic body + preview |
| ST02 | Russian font | Literata | Literata, PT Serif, Inter, Manrope, Android Sans, Android Serif | Reader/translation typography + preview |
| ST03 | Arabic size | 32px | 22–44, step 1 | CSS variable; preview capped at 24px |
| ST04 | Russian size | 17px | 13–25, step 1 | CSS variable; preview capped at 15px |
| ST05 | Line height | 1.65 | 1.25–2.00, step .05 | Arabic + Russian reader line-height and preview |
| ST06 | Reader style | book | book / compact | Compact card padding becomes 12px |
| ST07 | Translation | on | on/off | Translation blocks render/hide |
| ST08 | Sources | on | on/off | Source rows render/hide |
| ST09 | Notes | on | on/off | Note boxes and disputed badges render/hide |
| ST10 | Theme | auto | auto / light / dark | Root dark class and theme-color |
| ST11 | View mode | cards | cards / list | Persisted separately as `azkar-view-mode` |

## 5. Progress-state matrix

Progress storage key is date-scoped:
`azkar-progress-YYYY-MM-DD`.

| ID | State | Expected behavior |
|---|---|---|
| P01 | Fresh date / no progress | All visible items start at 0 |
| P02 | Partial item | Current count persists up to target |
| P03 | Completed item | Count is clamped to target; completed visuals appear |
| P04 | Multiple completed items | Progress header shows completed item count, not total repetitions |
| P05 | Progress bar | Width = rounded percentage of completed visible items |
| P06 | Reset | Deletes progress entries for currently visible period items, saves, re-renders |
| P07 | Switch period | Active index resets to 0; same date storage object is reloaded |
| P08 | New calendar date | Different storage key; previous date does not populate the new date |

## 6. Navigation/gesture states

| ID | Interaction | Existing web behavior to preserve/verify |
|---|---|---|
| N01 | Pager “Назад” | Previous card; disabled at first item |
| N02 | Pager “Далее” | Next card; disabled at last item |
| N03 | Swipe left | Next card if horizontal threshold passes |
| N04 | Swipe right | Previous card if horizontal threshold passes |
| N05 | Vertical swipe inside paged card | Scroll card vertically |
| N06 | Left-edge touch (<28px) in primary history-aware block | Reserved; does not trigger card swipe |
| N07 | Contents jump in cards mode | Sets active card and direction |
| N08 | Contents jump in list mode | Closes sheet, renders, smooth-scrolls selected card |
| N09 | Open modal | History-aware block pushes state |
| N10 | Back from modal | History-aware block backs out of modal before leaving prior UI state |
| N11 | Back after card navigation | History-aware block can restore prior active index/direction |
| N12 | System color-scheme change in auto theme | Later block has a listener that re-renders when auto is active |

## 7. Theme matrix

Every primary surface and bottom sheet must be checked in:
- Light
- Dark
- Auto → system Light
- Auto → system Dark

No custom colors may be introduced in native implementation without a future contract change.

## 8. Width/layout matrix

CSS defines one explicit responsive breakpoint.

| Width class | Expected web rule |
|---|---|
| >370px | Full toolbar labels; horizontal counter row; 3-column Russian font grid; pager helper visible |
| <=370px | Toolbar label spans hidden; counter row becomes vertical; count button 100% width; pager helper hidden; Russian font grid becomes 2 columns |
| Any width up to container maximum | Shell remains max 760px and centered |
| Safe-area devices | Shell bottom padding, sticky toolbar top, and sheet body bottoms use safe-area insets |

## 9. Motion matrix

| ID | Transition | Existing value |
|---|---|---|
| M01 | Progress fill | 350ms ease |
| M02 | Next card | 620ms cubic-bezier(.16,1,.3,1), +24px → 0, opacity .62 → 1, scale .994 → 1 |
| M03 | Previous card | 620ms cubic-bezier(.16,1,.3,1), -24px → 0, opacity .62 → 1, scale .994 → 1 |
| M04 | Bottom sheet entry | 300ms cubic-bezier(.16,1,.3,1), +18px Y → 0, opacity .7 → 1 |
| M05 | Toggle thumb | 200ms |
| M06 | Reduced motion | All animation/transition disabled; scroll behavior auto |

## 10. Phase-0 source risk to carry forward

`app-core.js` contains duplicated implementations of core rendering and event functions, with a newer history-aware/back-aware block followed by an older duplicated block. There is also a fragment around the duplicate boundary that must be treated as a golden-master source anomaly.

Phase 0 does not modify it. Before native Phase 1 behavior is frozen, the effective browser behavior must be verified against the actual golden master rather than selecting one duplicate implementation by assumption.
