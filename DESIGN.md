# 03 · UI / UX DESIGN SYSTEM (V2, replaces previous file 03)

> Implement in `core/designsystem`. **No hard-coded colors, sizes, fonts, or paddings inside screens.** Every value comes from the tokens below. If a value is missing, add a token first.

---

## 1. DESIGN DIRECTION

**Calm, premium, smart-energy.** Fintech-grade clarity applied to electricity.
- Clean surfaces, soft depth, **one bold gradient hero per screen**, everything else quiet.
- **Numbers are the heroes.** Big, tabular, high-contrast. Labels are small, muted, and never compete.
- Bento-style stat grids, generous whitespace, rounded shapes, meaningful motion.
- Light, Dark (true dark), System. Material You dynamic color is an optional toggle (default OFF).
- Rule of three: max 3 visual levels per card (value, label, hint).

---

## 2. COLOR TOKENS

### Light
| Token | Value |
|---|---|
| primary / onPrimary | `#4F46E5` / `#FFFFFF` |
| primaryContainer / onPrimaryContainer | `#E4E3FF` / `#1B1670` |
| secondary / onSecondary | `#0EA5A4` / `#FFFFFF` |
| secondaryContainer / onSecondaryContainer | `#D3F5F3` / `#053F3E` |
| tertiary / onTertiary | `#F59E0B` / `#1F1300` |
| tertiaryContainer / onTertiaryContainer | `#FEEFC7` / `#3D2600` |
| background | `#F6F7FB` |
| surface | `#FFFFFF` |
| surfaceContainerLow | `#FAFBFE` |
| surfaceContainer | `#F1F3FA` |
| surfaceVariant | `#ECEEF6` |
| onSurface | `#0F172A` |
| onSurfaceVariant | `#5B6478` |
| outline / outlineVariant | `#D5D9E6` / `#E7EAF3` |
| success / warning / error / info | `#16A34A` / `#F59E0B` / `#DC2626` / `#0284C7` |
| scrim | `#0F172A` @ 40% |

### Dark (true dark)
| Token | Value |
|---|---|
| primary / onPrimary | `#8B93FF` / `#14123D` |
| primaryContainer / onPrimaryContainer | `#2C2A8F` / `#E4E3FF` |
| secondary / onSecondary | `#2DD4BF` / `#00201D` |
| secondaryContainer | `#0F4F4B` |
| tertiary / onTertiary | `#FBBF24` / `#2B1A00` |
| tertiaryContainer | `#5C3C00` |
| background | `#0B1020` |
| surface | `#131A2E` |
| surfaceContainerLow | `#0F1527` |
| surfaceContainer | `#18213A` |
| surfaceVariant | `#1B2440` |
| onSurface | `#E6E9F5` |
| onSurfaceVariant | `#A3ABC2` |
| outline / outlineVariant | `#2A3556` / `#202A47` |
| success / warning / error / info | `#4ADE80` / `#FBBF24` / `#F87171` / `#38BDF8` |

### Gradients
- **Hero:** linear 135° `#4F46E5 → #7C3AED → #06B6D4` (light) · `#3730A3 → #5B21B6 → #0E7490` (dark). Noise overlay 4%. Text on hero is always white (`#FFFFFF` / secondary text white 78%).
- **Soft accent (chips, icon wells):** primaryContainer → surface, 180°.
- **Success moment:** `#16A34A → #0EA5A4`.

### Data colors
- Chart palette (same order everywhere, colorblind-safe): `#4F46E5, #0EA5A4, #F59E0B, #EC4899, #64748B`.
- Usage meter: 0–70% teal `#0EA5A4` · 70–90% amber `#F59E0B` · 90%+ red `#DC2626`. Always paired with an icon/label (never color alone).
- Contrast: all text pairs ≥ 4.5:1 body, ≥ 3:1 large text (WCAG AA). Add a unit test for token contrast.

---

## 3. TYPOGRAPHY (exact scale)

**Fonts:** Numbers & display → **Plus Jakarta Sans** (or Manrope). Body & labels → **Inter**. Bangla → **Noto Sans Bengali** (line height +4sp, no negative tracking). Always enable tabular figures (`fontFeatureSettings = "tnum"`) on every reading, unit, and money value.

| Style | Size / Line | Weight | Tracking | Use |
|---|---|---|---|---|
| `numberHero` | 56 / 60 | Bold | −1.0sp | Hero meter / units number |
| `numberLarge` | 32 / 38 | Bold | −0.5sp | Big stat values, cost totals |
| `numberMedium` | 22 / 28 | SemiBold | −0.2sp | Stat-card values, list amounts |
| `displayMedium` | 40 / 48 | Bold | −0.5sp | Onboarding titles |
| `headlineMedium` | 28 / 36 | SemiBold | −0.25sp | Screen titles (large app bar) |
| `titleLarge` | 20 / 28 | SemiBold | 0 | Section headers, sheet titles |
| `titleMedium` | 16 / 24 | SemiBold | 0.1sp | Card titles, list titles |
| `bodyLarge` | 16 / 24 | Regular | 0 | Primary body |
| `bodyMedium` | 14 / 20 | Regular | 0.1sp | Descriptions, secondary text |
| `labelLarge` | 14 / 20 | Medium | 0.1sp | Buttons, chips, tabs |
| `labelMedium` | 12 / 16 | Medium | 0.3sp | Captions, units, delta chips |
| `labelSmall` | 12 / 16 | Medium | 0.4sp | Nav labels, chart axis (**never below 12sp**) |

Rules:
- Max 3 type styles per card. Value > label > hint, in that order of size.
- Unit suffix ("kWh", "৳") uses `labelMedium`, baseline-aligned, `onSurfaceVariant`, 4dp gap from the number.
- Max line length 60–70 chars for body text. Truncate with ellipsis, never shrink font to fit.
- Must work at 200% font scale: use `heightIn(min=)` not fixed heights, allow wrapping, use `FlowRow` for chip rows.

---

## 4. SPACING, SIZING & GAP RULES

**Grid: 4dp base, 8dp rhythm.** Tokens: `xxs=4, xs=8, sm=12, md=16, lg=20, xl=24, xxl=32, xxxl=40, huge=48`.

| Context | Value |
|---|---|
| Screen horizontal padding | **20dp** |
| Top of content under app bar | 8dp |
| Gap between **sections** | **28dp** |
| Section header → its content | 12dp |
| Gap between **cards** (vertical & horizontal in grids) | **12dp** |
| Card inner padding | 16dp (standard) · 20dp (hero & large) |
| Inside a card: title → value | 8dp · value → hint 4dp |
| Icon container | 40dp square, radius 12dp, icon 20–24dp |
| Icon ↔ text gap | 12dp |
| List item min height | 64dp, vertical padding 12dp, divider inset 72dp |
| Chip height | 32dp, horizontal padding 12dp, gap 8dp |
| Button height | 52dp (primary), 44dp (compact), horizontal padding 24dp |
| Text field height | 56dp, radius 14dp |
| Touch target | ≥ 48×48dp always |
| Bottom nav bar | 72dp + system inset, 5 equal slots |
| Center Add button | 60dp circle, raised 16dp above the bar, 76dp notch cutout |
| Scroll bottom padding | 104dp + insets (so the raised button never covers the last item) |
| Max content width | 640dp centered on medium+ screens |

**Shapes:** cards 24dp · hero 28dp · buttons 16dp (primary CTA = pill) · inputs 14dp · chips 10dp · bottom sheet 28dp top corners · icon wells 12dp · chart bars: top corners 6dp.
**Elevation:** tonal elevation + 1dp `outlineVariant` border. Shadow only for FAB and floating sheets: `0 8dp 24dp primary@18%`. No heavy shadows.
**Glass:** blur 24dp + 80% surface alpha on bottom bar and bottom sheets only, with a solid fallback below API 31.

---

## 5. COMPONENT SPECS (build all, preview light + dark, plus 200% font)

1. **GradientHeroCard** (radius 28, padding 20, min height 200): top row = label (`labelMedium`, white 78%) + TrendChip; center = `numberHero` units + unit suffix; below = cost line (`titleMedium`); bottom = cycle progress bar (6dp tall, white 24% track, white fill) + "Day 11 of 30" (`labelMedium`). Optional sparkline in the background at 20% opacity.
2. **StatCard** (bento tile, radius 24, padding 16): icon well (40dp) top-left, label (`labelMedium`), value (`numberMedium`), delta (`labelMedium` with arrow icon). Used in a **2-column grid, 12dp gaps**.
3. **UsageRing**: 120dp default, 12dp stroke, rounded caps, track `surfaceVariant`, color from usage meter rules, center shows percent (`numberMedium`) + "of budget" (`labelMedium`). Animated sweep 800ms.
4. **TrendChip**: height 24dp, radius 12dp, arrow icon 14dp + percent (`labelMedium`). Green down arrow = less usage (good); red up arrow = more usage. Always icon + text.
5. **SectionHeader**: `titleLarge` left, optional text button right (`labelLarge`, primary), 48dp touch height.
6. **ReadingListItem**: leading date block (48dp rounded square: day `numberMedium`, month `labelSmall`), title = meter value (`titleMedium`), subtitle = "+8.4 units" (`bodyMedium`, muted), trailing = cost (`numberMedium`) + badges (Estimated / Note / Photo as 20dp icons). Swipe actions with 24dp radius backgrounds.
7. **TariffSlabRow**: slab range, rate, units in slab, subtotal; right-aligned tabular numbers; thin 1dp divider; total row in `titleMedium` with primaryContainer background.
8. **NumberPadField** (add-reading input): large `numberHero` entry, digit-by-digit display with blinking caret, decimal one place, custom 3×4 number pad (key 64dp tall, radius 20dp, haptic tick). Under it: live helper "= 8.4 units · ≈ ৳ 78" (`titleMedium`, teal). Error state: red text + icon + suggested fix.
9. **SegmentedTabs** (Day / Week / Month / Year): height 44dp, radius 14dp container `surfaceContainer`, selected pill with primary background and spring slide animation.
10. **EmptyState**: 160dp illustration, `titleLarge`, `bodyMedium` muted, primary CTA, 24dp gaps.
11. **ConfirmDialog / BottomSheet / Snackbar (with Undo)**: sheet has 4×36dp drag handle, 20dp padding, 16dp gaps; Snackbar floats 16dp above the nav bar, radius 16dp.
12. **Skeleton shimmer** matching each component's shape (no spinners on main screens).
13. **InsightCard**: icon well + one friendly sentence (`bodyLarge`) + dismiss; tinted with tertiaryContainer / secondaryContainer.
14. **ChartCard**: title, SegmentedTabs, chart, and a "View as table" text button; chart exposes a semantic text summary for TalkBack.

**Chart style:** no axis lines, dashed 1dp grid at 8% opacity, labels `labelSmall` muted, rounded bars, gradient fill under lines (primary 24% → 0%), selected bar highlighted + floating tooltip (radius 12, `numberMedium`), budget/average line dashed amber with label.

---

## 6. LAYOUT PER SCREEN (use the spacing tokens above)

**Dashboard (top → bottom)**
1. Greeting row: avatar/bolt 40dp, "Good evening" (`bodyMedium`, muted) + date (`titleMedium`), bell icon (48dp target).
2. 16dp → **Today status** pill card (record reminder or today's usage).
3. 12dp → **GradientHeroCard**.
4. 28dp → **Bento grid 2×2** (Avg daily, Peak day, Projected units, Projected bill), 12dp gaps.
5. 28dp → **Budget card**: UsageRing left, remaining amount + days left right.
6. 28dp → SectionHeader "Last 7 days" → ChartCard (mini bars, 160dp tall).
7. 28dp → SectionHeader "Insights" → horizontal pager of InsightCards (card width = screen − 40dp − 16dp peek).
8. 28dp → SectionHeader "Recent" → 3 ReadingListItems in one card.
9. No separate FAB. "Add reading" is the center button of the bottom bar (see §7).

**Add Reading**: top bar close; date chip row; previous reading helper (`bodyMedium`); NumberPadField; live preview card; gap banner (if any); note + photo row; full-width pill Save button 52dp pinned above the keypad with 16dp margin.

**Analytics**: large title; SegmentedTabs; summary row (3 StatCards, scrollable); usage bar chart card; trend line card; cost donut card (donut 160dp + legend in 2 columns); weekday heat strip; compare card; highlights; forecast card. Cards separated by 12dp, sections by 28dp.

**History**: search field 56dp, filter chips row (8dp gaps, horizontally scrollable); month sticky headers (`titleMedium` + month totals in `labelMedium`); items in rounded group cards per month; tablet = two-pane list/detail.

**Settings**: large title; grouped cards (`surfaceContainerLow`, radius 24); rows 56dp min with icon well, title (`bodyLarge`), subtitle (`bodyMedium` muted), trailing switch/chevron; 8dp between groups' headers and cards, 24dp between groups.

**Lock, Onboarding, Bill Estimator**: follow the same tokens; onboarding uses `displayMedium`, one illustration per page, pill CTA, page indicator dots (8dp, 8dp gaps).

---

## 7. NAVIGATION & ADAPTIVE

- **Compact (<600dp): bottom bar with a CENTER ADD BUTTON.** Layout of 5 slots, left to right: **Home · Analytics · [ + Add ] · History · Settings**.
  - **Bar:** 72dp tall, `surface` at 80% alpha with 24dp blur (solid fallback below API 31), top corners 28dp, 1dp `outlineVariant` top border. Respect the gesture-nav inset.
  - **Notch:** a smooth concave cutout (cubic curve, 76dp wide, 10dp deep) in the bar behind the center button, drawn with a custom `Shape`/`Path`. Fallback if the path is too complex: a 6dp ring in the `background` color around the button.
  - **Center button:** 60dp circle, hero gradient (`#4F46E5 → #7C3AED`), white "add" icon 28dp (or bolt with plus), raised 16dp above the bar, shadow `0 8dp 20dp primary@30%`. No label; `contentDescription = "Add reading"`; touch target 64dp.
  - **Press:** scale 0.94 with spring + haptic tick; a soft glow pulse once per day when today's reading is missing (stop with reduce motion).
  - **Action:** opens Add Reading with a container-transform from the button (full-screen sheet).
  - **Side tabs:** 4 tabs, 2 on each side of the notch, equal width. Selected = filled icon 24dp + `primaryContainer` pill indicator (56×32dp) + label (`labelSmall`); unselected = outlined icon + muted label. Labels always visible.
  - **Always visible** on Home, Analytics, History and Settings. Hide the bar (and the button) on Add Reading, Lock, Onboarding and full-screen detail screens.
  - **No other FAB** anywhere on compact screens (remove the old "+ Add reading" extended FAB from Home and History).
- **Medium (600–840dp):** NavigationRail (80dp) with the same Add button placed at the top of the rail (56dp gradient circle) above the 4 tabs; content max 640–720dp centered.
- **Expanded (≥840dp / foldables):** rail + two-pane (History list | detail; Analytics 2-column cards).
- Use `WindowSizeClass`, edge-to-edge with proper insets, predictive back.

---

## 8. MOTION

- Count-up numbers 600ms ease-out; chart lines draw 800ms; bars grow with 40ms stagger.
- Cards: spring (damping 0.8, stiffness medium-low); press scale 0.98.
- Shared-element: history row → detail; FAB → Add Reading (container transform).
- Tab switch: fade-through 250ms.
- Save moment: haptic tick + check animation + toast card "You used 7.4 units today, 8% less than yesterday" (use TrendChip colors).
- Skeleton shimmer 1.2s loop. **Respect "reduce motion"** (disable parallax/stagger/count-up, keep simple fades).

---

## 9. ICONOGRAPHY
Material Symbols Rounded, weight 400, 24dp (20dp inside wells, 14dp inside chips). Filled variant for selected nav items only. App icon: rounded square, hero gradient, white bolt with amber spark.

---

## 10. ACCESSIBILITY
- Touch targets ≥ 48dp; works up to 200% font scale with no clipping (test with `fontScale = 2.0` previews).
- Never color alone: status = color + icon + text.
- Charts expose a text summary and a "View as table" option.
- TalkBack: logical order, `contentDescription` on all icons, merged semantics for cards ("Average daily, 8.4 units, 5% less than last week").
- Dark mode parity, contrast unit tests, reduce-motion support.

---

## 11. DELIVERABLES FOR THIS STEP
1. `Color.kt`, `Type.kt` (with font resources + tnum), `Shape.kt`, `Spacing.kt` (`object Dimens`), `Elevation.kt`, `Motion.kt`, `VoltTrackTheme.kt` (light/dark/dynamic switch).
2. All components in §5 as stateless composables with `@Preview` (light, dark, 200% font, Bangla).
3. A debug-only **Design Gallery screen** showing tokens, type scale, spacing, and every component.
4. Then **refactor existing screens** to use only these tokens; fix any hard-coded values; screenshot-test Dashboard, Add Reading, Analytics, History, Settings in light and dark.
