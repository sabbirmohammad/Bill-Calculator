# VoltTrack: FIX & POLISH PROMPT

You are an autonomous senior Android engineer + product designer working on the existing VoltTrack app (Kotlin, Compose, Material 3, Room, DataStore, Hilt). Read @docs (01-05 and 03_UI_UX_DESIGN_SYSTEM_V2) again: they remain the source of truth for architecture and design tokens.

GOAL: Fix every issue below, end to end, without asking me for confirmation between issues. For EACH issue: (a) find the ROOT CAUSE in the code first and state it in PROGRESS.md, (b) fix it properly (no workarounds, no hard-coded values, use design tokens), (c) add or update tests, (d) verify on the emulator.
Do not change unrelated behavior. Keep the center Add button navigation as is.

======================================================
ISSUE 1: Cold start shows an EMPTY app for a moment
======================================================
Symptom: after fully closing and reopening, the splash is very short, then the UI briefly looks like it has no data, then data appears.
Required:
- Root cause is likely StateFlow/Flow initial values (emptyList/zero) being rendered as real "empty" state before Room/DataStore emit.
- Every UiState must have an explicit Loading state (sealed: Loading / Content / Empty / Error). Initial state = Loading. NEVER render the empty state or zeros until the first real emission arrives.
- Use the Android 12+ SplashScreen API with setKeepOnScreenCondition { !appReady }. appReady = first DataStore preferences read (theme, language, onboarding, lock) + first dashboard data emission completed. Add a hard timeout (e.g. 1500 ms) so the splash can never hang.
- Read theme/language/lock preference BEFORE the first frame so there is no light-to-dark or language flash.
- Show skeleton shimmer placeholders (matching card shapes) for any remaining load, never an empty state.
- Verify: cold start 10 times on emulator (adb shell am force-stop, then launch); record screenshots; no empty/zero flash. Measure with `adb shell am start -W`.

======================================================
ISSUE 2: Dashboard hero card (current billing cycle): odd white line under the progress bar
======================================================
- Find the cause (likely Material3 LinearProgressIndicator track/stop-indicator or gap, a Divider, a card border/clip issue, or gradient not covering the bottom edge).
- Replace with a custom progress bar (Box + clip, 6dp tall, rounded, track = white 24%, fill = white, animated) with no stop dot and no extra line.
- Ensure the gradient and corner clip cover the entire card, with no hairline at the bottom on any density. Check light, dark, 200% font.

======================================================
ISSUE 3: The three cards under the hero (Today / Yesterday / Average daily): text wraps, not responsive
======================================================
- Redesign as ONE clean "Usage summary" component with three equal-weight columns inside a single card (or a refined 3-tile row) separated by subtle 1dp vertical dividers.
- Each column: icon well (small), label (labelMedium, maxLines=1), value (numberMedium, maxLines=1, tabular), unit suffix (labelMedium) on the same baseline, and a small TrendChip (icon + percent) where relevant.
- Use Modifier.weight(1f) and no fixed widths. Numbers must NEVER wrap: use auto-size / shrink-to-fit down to a minimum, compact formatting (e.g. 1.2k) if needed.
- Responsive: with BoxWithConstraints/WindowSizeClass, if width is too small or fontScale > 1.3, stack to a vertical list of 3 rows instead of squeezing.
- Follow spacing tokens (card padding 16dp, gaps 12dp). Test at fontScale 1.0, 1.5, 2.0 and a 320dp-wide screen.

======================================================
ISSUE 4: "Monthly Budget & Outlook" section is text-heavy and not modern
======================================================
Redesign with less text and more visual clarity:
- Left: a UsageRing (animated arc, 120dp) showing % of budget used, with the percent in the center and a status chip (On track / Watch out / Over budget) using color AND icon.
- Right: three compact stat rows with icon wells: Spent, Projected, Limit. Label tiny (labelMedium, muted), value bold (numberMedium). Remove long sentences and repeated labels.
- Below: one slim segmented bar showing spent vs projected vs limit with a limit marker, plus a single-line insight ("You may exceed your budget by ৳420 on Nov 27").
- Ring color rules: 0-70% teal, 70-90% amber, 90%+ red.
- Fully responsive (stacks vertically on narrow width / large font). No text overflow or wrapping glitches.

======================================================
ISSUE 5: History page filters
======================================================
- REMOVE the search bar and the filter chips (All, High use, Estimated, Reset).
- ADD a Year and Month filter: a year selector (stepper/dropdown with only years that have data + current year) and a horizontally scrollable month chip row (Jan-Dec, months without data dimmed but selectable).
- DEFAULT: current year and current month selected automatically. Selection survives rotation and process death (SavedStateHandle).
- The list shows only the selected month's readings. Above the list, a small month summary card: total units, total cost, number of readings, average per day.
- Empty month = friendly EmptyState with a CTA to add a reading. Keep swipe edit/delete + Undo behavior.
- Month names and numbers must follow the selected language / Bengali digits setting.

======================================================
ISSUE 6: Settings > Display & Language: options have NO effect
======================================================
Audit and make ALL work instantly, with no app restart needed:
- Theme (System / Light / Dark): persisted in DataStore, collected at the root (MainActivity setContent) and applied to VoltTrackTheme. Verify the whole app recolors, including the status bar and nav bar icons.
- Language (English / Bangla): use the per-app language API (AppCompatDelegate.setApplicationLocales / LocaleManager) + values-bn/strings.xml. Audit ALL screens for hard-coded strings and move them to string resources with complete Bangla translations (including notifications, dialogs, charts, month and weekday names, validation errors).
- Bengali digits: create ONE central formatter (NumberFormatter / DateFormatter / CurrencyFormatter) that maps digits to Bangla when enabled and respects the language. Replace EVERY direct String.format / toString / interpolation of a number, date, time or money in UI code, charts, widgets and notifications. Add a lint/detekt rule or a unit test that fails if a Text() shows a raw number.
- Provide a live preview line in the setting (e.g. "১২,৩৪৫.৬ kWh").
- Verify by toggling each setting on the emulator and capturing before/after screenshots of Dashboard, History, Analytics, Settings.

======================================================
ISSUE 7: Daily reminder
======================================================
- Time display and picker must use 12-hour format (hh:mm AM/PM, respecting language and Bengali digits). Store internally as LocalTime (24h).
- VERIFY the reminder actually fires: WorkManager or AlarmManager (choose the more reliable for a daily time-of-day reminder; setExactAndAllowWhileIdle if exact alarms are allowed, otherwise inexact fallback), notification channel created, POST_NOTIFICATIONS permission requested with a clear rationale (Android 13+), permission-denied state shown in Settings with a button to open system settings.
- Reschedule after: device reboot, time change, timezone change, app update, setting change. Cancel when disabled.
- Skip the notification if today's reading is already recorded.
- Add a "Send test notification" button (debug build only) and verify it. Test with `adb shell dumpsys jobscheduler` / `dumpsys alarm` and WorkManager test utilities. Document results in PROGRESS.md.

======================================================
ISSUE 8: Billing & Tariff section: redesign and make it safe
======================================================
Design:
- Modern grouped layout: a summary card at the top (active plan name, currency, VAT, fixed/demand charge as small chips), then a slab list built with TariffSlabRow components (range, rate), clear Add slab button, drag-free simple edit via bottom sheet. Follow the V2 design tokens and spacing.
- REMOVE the "Live calculation preview" block completely (and its code if unused).
Safety (tariff and rates are critical/confidential):
- Editing is a draft: changes are not applied until the user taps Save.
- Strict validation: no overlapping or missing ranges, increasing order, rates >= 0, VAT/rebate 0-100, only 1 open-ended last slab. Show inline errors.
- On Save show a confirmation dialog with a clear diff (old vs new for each changed field) and the effect scope. If App Lock is enabled, require biometric/PIN re-authentication before saving.
- Save atomically in one Room transaction. Keep tariff versions with an effectiveFrom date (default: start of current billing cycle) so past bills are not silently changed; offer a choice: "Apply from this billing cycle" or "Apply from today". Keep a change history entry (date, what changed) and allow Revert to the previous version.
- Unsaved-changes guard on back press. Never log tariff values (no Log.d with rates).
- Unit tests for validation, versioning and cost calculation with versions.

======================================================
ISSUE 9: Security & Privacy: make sure every option really works
======================================================
Audit each control one by one: App lock (biometric + PIN fallback), lock timeout, lock on background, privacy mode (hide amounts/units on Dashboard, History, Analytics, widgets, notifications), block screenshots (FLAG_SECURE, also hides recents thumbnail), encrypted database toggle, encrypted backup/restore, Clear all data (double confirm + auth).
- For each: write down expected behavior, test it on the emulator, fix if broken. Setting changes must take effect immediately.
- Any option that cannot be made to work reliably must be hidden or marked "Coming soon", never shown as functional when it is not.
- Add instrumented tests for lock flow, timeout, privacy mode, FLAG_SECURE and clear data. Verify nothing sensitive is logged.

======================================================
ISSUE 10: About: version number not updating automatically
======================================================
- Read versionName/versionCode at runtime from PackageManager/BuildConfig (never hard-code). Also show build type and build date if available.
- Configure Gradle so versionName/versionCode come from one source (e.g. gradle.properties or git tag/commit count) and increase with each release. Show "Version 1.2.0 (build 15)".

======================================================
GLOBAL CONSISTENCY PASS (do this after fixing the issues)
======================================================
- Scan all screens for hard-coded colors/dp/sp/text and replace with tokens/resources.
- Make sure spacing (20dp screen padding, 12dp card gaps, 28dp section gaps), type scale and card styles are identical across Dashboard, Analytics, History, Settings.
- Check dark mode parity, 200% font scale, small screens (320dp), tablet layout, TalkBack labels, and that all loading/empty/error states exist.
- Fix any crash, ANR, memory leak or logcat error seen during testing.

======================================================
WORKFLOW (mandatory loop per issue)
======================================================
1. Root cause (write in PROGRESS.md) -> 2. Fix -> 3. ./gradlew assembleDebug -> 4. lintDebug detekt ktlintCheck -> 5. testDebugUnitTest -> 6. connectedDebugAndroidTest on emulator -> 7. Install, drive the flow manually on the emulator, take screenshots, check logcat -> 8. Commit ("fix: <issue>"). Never continue while something is red; if stuck after 5 attempts, record it in BLOCKERS.md and continue.

FINAL DELIVERABLES
- FIX_REPORT.md: for each of the 10 issues: root cause, fix summary, files changed, tests added, before/after screenshots, status (Fixed / Partially / Blocked).
- A manual QA checklist for me (steps to verify each issue on my phone).
- Final ./gradlew assembleRelease must succeed.
