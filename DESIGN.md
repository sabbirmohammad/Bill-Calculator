# DESIGN SYSTEM – WattWise

## Direction
Calm, premium, "smart-energy" feel: clean surfaces, soft depth, one bold gradient hero card, generous whitespace, rounded shapes and meaningful motion. Think fintech-grade clarity applied to energy. Support Light, Dark and System themes. Offer Material You dynamic color as an optional toggle; the brand palette below is the default.

## Color tokens (Material 3 ColorScheme + a custom extended palette)

### Light theme
| Token | Value |
| --- | --- |
| primary | #4F46E5 (Electric Indigo), onPrimary #FFFFFF |
| secondary | #0EA5A4 (Teal, "savings"), onSecondary #FFFFFF |
| tertiary / accent | #F59E0B (Amber, "energy"), onTertiary #1F1300 |
| background | #F6F7FB |
| surface | #FFFFFF |
| surfaceVariant | #ECEEF6 |
| onSurface | #0F172A |
| onSurfaceVariant | #5B6478 |
| outline | #D5D9E6 |
| success / warning / error / info | #16A34A / #F59E0B / #DC2626 / #0284C7 |

### Dark theme (true dark)
| Token | Value |
| --- | --- |
| primary | #8B93FF, onPrimary #14123D |
| secondary | #2DD4BF |
| tertiary | #FBBF24 |
| background | #0B1020 |
| surface | #131A2E |
| surfaceVariant | #1B2440 |
| onSurface | #E6E9F5 |
| onSurfaceVariant | #A3ABC2 |
| outline | #2A3556 |
| success / warning / error / info | #4ADE80 / #FBBF24 / #F87171 / #38BDF8 |

### Gradients and charts
- Hero gradient (dashboard top card): linear 135deg #4F46E5 -> #7C3AED -> #06B6D4 (light); #3730A3 -> #5B21B6 -> #0E7490 (dark); subtle noise overlay at 4% opacity.
- Chart palette (colorblind-safe, same order everywhere): #4F46E5, #0EA5A4, #F59E0B, #EC4899, #64748B.
- Usage meter colors: 0-70% teal, 70-90% amber, 90%+ red.
- Contrast: every text/background pair must meet WCAG AA (4.5:1 body, 3:1 large text).

## Typography
- Display and numbers: "Plus Jakarta Sans" or "Manrope" (SemiBold/Bold) with tabular figures for readings and money.
- Body and labels: "Inter".
- Bangla: "Noto Sans Bengali" fallback; check line height.
- Scale: Display 40/48, Headline 28/36, Title 20/28, Body 16/24, Label 12/16. The big meter number uses 56sp Bold.

## Shape, spacing, elevation
- Corner radius: cards 24dp, buttons 16dp (pill for primary CTA), inputs 14dp, bottom sheets 28dp top.
- 8dp spacing grid; screen padding 20dp; card inner padding 16-20dp.
- Prefer tonal elevation and 1dp outlines over heavy shadows. Glass-style blur only on the bottom bar and sheets, with a solid fallback.

## Motion
- Count-up animation on numbers, 600ms ease-out; chart lines draw in 800ms; shared-element transition from a history row to its detail.
- Spring physics for cards (damping 0.8). Haptic tick when saving a reading. Skeleton shimmer for loading. Respect "reduce motion".
- Success moment after saving a reading: subtle check animation + "You used 7.4 units today, 8% less than yesterday".

## Iconography and components
Material Symbols Rounded, 24dp, consistent stroke feel. Build reusable components: StatCard, GradientHeroCard, UsageRing (circular progress), TrendChip (up/down arrow + percent), SectionHeader, ReadingListItem, TariffSlabRow, EmptyState (with illustration), ConfirmDialog, NumberPadField, SegmentedTabs (Day/Week/Month/Year), BottomSheet, Snackbar with undo.

## Navigation
Bottom navigation bar with 4 tabs: Home, Analytics, History, Settings, plus an extended FAB "+ Add reading" on Home and History. Use a NavigationRail on tablets and foldables (WindowSizeClass).

## Accessibility (design side)
- Touch targets at least 48dp; support font scale up to 200% without clipping.
- Never rely on color alone: pair status colors with icons or labels.
- Charts expose a text summary and a "view as table" option.
