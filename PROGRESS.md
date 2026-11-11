# PowerUnit (WattWise) – Implementation Progress

## Project Overview
Offline-first, privacy-focused Android electricity tracker and bill calculator tailored for households in Bangladesh (Asia/Dhaka, BDT ৳, English + Bangla).

## Phase Matrix

| Phase | Description | Status | Build | Lint / Detekt / Ktlint | Unit Tests | UI Tests | Runtime Verif | Git Commit |
|---|---|---|---|---|---|---|---|---|
| **Phase 1** | Foundation: Gradle, Hilt, Room+SQLCipher, Design System, Nav Shell | Completed | Passed | Passed (0 warnings) | Passed (11/11) | Passed (3/3) | Verified on Emulator | `phase 1: foundation, gradle, hilt, room with sqlcipher, design system, and nav shell` |
| **Phase 2** | Core Domain: Models, Repositories, 8 Calculation Use Cases (100% tests) | Completed | Passed | Passed (0 warnings) | Passed (32/32) | Passed (3/3) | Verified on Emulator | `phase 2: core domain models, repositories, and calculation use cases with 100% unit test coverage` |
| **Phase 3** | Reading Flow: Onboarding, Add/Edit Reading, History List, Search/Filter | Completed | Passed | Passed (0 warnings) | Passed (43/43) | Passed (5/5) | Verified on Emulator | `phase 3: onboarding, add/edit reading flow, and history list with search and filters` |
| **Phase 4** | Dashboard & Bill: Hero Card, Stats, Forecast, Budget, Bill Breakdown | Completed | Passed | Passed (0 warnings) | Passed (47/47) | Passed (6/6) | Verified on Emulator | `phase 4: dashboard with live cycle metrics and bill breakdown with what-if simulator` |
| **Phase 5** | Analytics: Custom Compose Charts, Heatmap, Slab Donut, Anomalies | Completed | Passed | Passed (0 warnings) | Passed (52/52) | Passed (7/7) | Verified on Emulator | `phase 5: analytics custom compose charts, heatmap, slab donut, and anomaly detection` |
| **Phase 6** | Settings, Tariff Editor, WorkManager, Glance Widget, App Lock | Completed | Passed | Passed (0 warnings) | Passed (60/60) | Passed (8/8) | Verified on Emulator | `phase 6: settings, tariff editor, workmanager reminders, glance widget, and security lock` |
| **Phase 7** | Backup/Restore, CSV/PDF Export, Localization (EN/BN), A11y Pass | Completed | Passed | Passed (0 warnings) | Passed (67/67) | Passed (9/9) | Verified on Emulator | `phase 7: encrypted backup and restore, csv and pdf export, full bangla localization, and accessibility pass` |

## Execution Log & Verification History
- **Phase 1**:
  - Gradle setup with Version Catalog, Kotlin 2.3.20, AGP 9.0.1, Hilt 2.60.1, Room 2.8.5, SQLCipher 4.6.1.
  - Setup detekt, ktlint, and Android lint.
  - Design system tokens implemented: light/dark palettes, typography, shapes, and reusable components (`StatCard`, `GradientHeroCard`, `UsageRing`, `TrendChip`, `SectionHeader`, `EmptyState`).
  - Navigation shell with 4 tabs (Home, Analytics, History, Settings), NavigationRail for tablets, and Extended FAB "+ Add reading".
  - Room database with SQLCipher encryption (`SupportOpenHelperFactory`) and entities: `MeterEntity`, `ReadingEntity`, `TariffEntity`, `TariffSlabEntity`, `BudgetEntity`, `RechargeEntity`.
  - All build, lint, detekt, unit tests, and connected UI tests passed.
- **Phase 2**:
  - Pure Kotlin domain models (`Meter`, `Reading`, `DailyUsage`, `Tariff`, `TariffSlab`, `BillBreakdown`, `CycleUsage`, `MonthEndForecast`, `PeriodComparison`, `PrepaidStatus`, `ReadingValidationResult`).
  - Clean repository abstractions (`MeterRepository`, `ReadingRepository`, `TariffRepository`, `BudgetRepository`, `RechargeRepository`) and Room implementations in data layer.
  - 8 Calculation Use Cases: `CalculateDailyUsageUseCase` (gap spreading, meter reset, multiplier), `CalculateCycleUsageUseCase` (cycle start day 1..28, days elapsed/left), `CalculateBillUseCase` (progressive slab billing with transparent receipt itemization), `ForecastMonthEndUseCase` (weighted 7-day 0.6 + cycle 0.4 range), `CompareWithPreviousUseCase` (% change vs previous), `DetectAnomaliesUseCase` (> mean + 2 std dev), `CalculateMetricsUseCase` (cost per day/unit, CO2 kg), `CalculatePrepaidStatusUseCase` (days remaining, recharge alert), `ValidateReadingUseCase` (validations and warnings).
  - 32/32 unit tests passed including worked Bangladesh tariff examples and zero Android dependency assertion.
  - `./gradlew assembleDebug`: SUCCESSFUL.
  - `./gradlew lintDebug detekt ktlintCheck`: SUCCESSFUL (0 errors/warnings).
  - `./gradlew testDebugUnitTest`: SUCCESSFUL (32 tests passed).
  - `./gradlew connectedDebugAndroidTest`: SUCCESSFUL (3 tests passed).
  - Runtime verification on emulator `medium_phone`: Clean launch, screenshot recorded at `screenshots/phase2_verified.png`.
- **Phase 3**:
  - 3-step Onboarding flow: Welcome & Privacy promise, Meter configuration (Name, Type, Start reading, Billing cycle day), and Quick guide.
  - Add & Edit Reading flow: Big numeric input in `MeterDisplayLarge` (JetBrains Mono / monospace), live difference badge (`+X.X kWh since last reading`), date/time display, meter reset/replaced toggle, optional notes, and validation warnings with confirmation dialog.
  - Consumption History screen: Search by date, note, or reading value; filter chips (All, Resets, Estimated, High Usage); grouped month cards with total units and estimated bills; click-to-view detail dialog with edit action; delete and undo via snackbar.
  - SQLCipher automatic native library loading (`System.loadLibrary("sqlcipher")`) and synchronous default Bangladesh tariff & meter seeding.
  - Unit tests: 43 unit tests passed (including `OnboardingViewModelTest`, `AddReadingViewModelTest`, and `HistoryViewModelTest`).
  - Static checks: `./gradlew lintDebug detekt ktlintCheck` passed with 0 errors and 0 warnings.
  - Connected UI tests: 5 tests passed on emulator (`NavigationShellTest` & `ReadingFlowTest`).
  - Runtime verification: Verified on emulator, saved readings, checked history list and details dialog. Screenshots: `screenshots/phase3_home.png`, `screenshots/phase3_add_reading.png`, `screenshots/phase3_history.png`, `screenshots/phase3_reading_detail.png`.
- **Phase 4**:
  - Live Home Dashboard: Dynamic greeting, active meter badge, `GradientHeroCard` showing current billing cycle units, estimated bill in BDT, budget % ring, and month-end projection.
  - Quick Stats 2x2 grid: Today's usage with % trend chip vs yesterday, yesterday's usage, cycle total, and daily average.
  - Month-End Forecast Card: Projected units and bill range (low/expected/high) computed via weighted 7-day run rate + cycle average.
  - Budget Tracking Card: Monthly spending limit, progress bar with color-coded warning (amber > 80%, red > 100%), and over-budget status.
  - 14-Day Usage mini bar chart: Visual bars with primary gradient and weekday labels.
  - Recent readings list with quick tap to view details or navigate to full History.
  - Detailed Bill Breakdown Screen: Hero bill card with Mark Paid toggle, tiered slab itemization table (units in slab, rate, amount), official utility receipt itemization (Energy charges, Demand charge, Service charge, 5% VAT, Total payable).
  - What-If Bill Simulator: Interactive slider (0 to 800 kWh) with instant real-time recalculation of bill breakdown and delta comparison vs current bill.
  - Unit tests: 47 unit tests passed (including `DashboardViewModelTest` and `BillViewModelTest`).
  - Static checks: `./gradlew lintDebug detekt ktlintCheck` passed with 0 errors and 0 warnings.
  - Connected UI tests: 6 tests passed on emulator (including `DashboardAndBillTest`).
  - Runtime verification: Verified on emulator, hero card tap navigation, what-if simulator slider interaction. Screenshots: `screenshots/phase4_dashboard.png`, `screenshots/phase4_bill_breakdown.png`, `screenshots/phase4_what_if.png`. Logcat verified 0 crashes/exceptions.
- **Phase 5**:
  - Custom Compose Canvas charts built from scratch with zero heavy third-party chart dependencies.
  - Usage & Cost trend chart (`UsageBarAndLineChart`): Animated vertical usage bars (kWh) with rounded corners, overlay cost polyline (৳) in tertiary color, dashed gridlines, tap/drag interactive tooltip inspect, and accessible table view toggle.
  - Period selector: Segmented Day, Week, Month, and Year tabs with instant dynamic aggregation and recalculation.
  - Summary metric tiles: 4-card grid showing Total Usage, Total Estimated Cost, Daily Average, and Peak Day.
  - Period comparison card: Current period vs prior period with `TrendChip` % change indicator.
  - Weekday Pattern Heatmap (`WeekdayHeatmap`): 7-column Monday..Sunday layout with color intensity mapped to consumption load.
  - Tariff Slab Donut chart (`SlabDonutChart`): Multi-tier circular arc visualization with center total units and legend showing cost percentage per slab.
  - Statistical anomaly detection: Integrated with `DetectAnomaliesUseCase` (> 2σ above mean) showing anomalous dates and consumption spikes.
  - Unit tests: 52/52 unit tests passed (including `AnalyticsViewModelTest`).
  - Static checks: `./gradlew lintDebug detekt ktlintCheck` passed with 0 errors and 0 warnings.
  - Connected UI tests: 7/7 tests passed on emulator (including `AnalyticsScreenTest`).
  - Runtime verification: Verified on emulator, switched periods, toggled table view, captured screenshots: `screenshots/phase5_analytics_chart.png`, `screenshots/phase5_analytics_heatmap.png`, `screenshots/phase5_analytics_donut.png`. Logcat verified 0 crashes/exceptions.
- **Phase 6**:
  - Full Settings screen: Meter profile, Billing preferences with Cycle Start Day selector, Daily Reminders with time configuration, Display & Language (Theme mode, English / Bangla, Bengali numerals), Security (App Lock, Hide amounts on home, Screenshot protection via `FLAG_SECURE`), and Data reset with confirmation dialog.
  - Interactive Tariff Editor screen: Progressive slabs table with editable fromUnit, toUnit, rate/unit, Add/Delete slab actions, Fixed charges (Demand & Service charges, VAT %), and live preview calculation on test units with immediate total payable breakdown.
  - WorkManager scheduled workers:
    - `DailyReminderWorker`: High-priority reminder notification at configured daily time (21:00) with deep link to Add Reading flow.
    - `BudgetAlertWorker`: Periodic background check against active budget threshold (80% warning / 100% exceeded).
    - `WorkManagerScheduler`: Unified manager for scheduling and cancelling periodic workers.
  - Jetpack Glance AppWidget: `WattWiseGlanceWidget` and `WattWiseWidgetReceiver` registered in `AndroidManifest.xml` with widget info XML, providing quick glance at electricity tracker and "+ Add Reading" one-tap launcher.
  - Security hardening: Screenshot protection toggle dynamically updating `FLAG_SECURE` in `MainActivity`.
  - Unit tests: 60/60 unit tests passed (including `SettingsViewModelTest` and `TariffEditorViewModelTest`).
  - Static checks: `./gradlew lintDebug detekt ktlintCheck` passed with 0 errors and 0 warnings.
  - Connected UI tests: 8/8 tests passed on emulator (including `SettingsAndTariffTest`).
  - Runtime verification: Verified on emulator, navigated to Settings, toggled preferences, opened Tariff Editor, captured screenshots: `screenshots/phase6_settings.png`, `screenshots/phase6_tariff_editor.png`. Logcat verified 0 crashes/exceptions.
- **Phase 7**:
  - Encrypted Backup and Restore (`BackupManager`):
    - Full SQLite / Room database JSON export and import.
    - Password-based encryption using PBKDF2WithHmacSHA256 (10,000 iterations, 256-bit key) + AES-256-GCM authenticated cipher with 16-byte random salt and 12-byte IV.
    - Backup file container format: `[SALT: 16 bytes][IV: 12 bytes][CIPHERTEXT + TAG]`.
    - Tamper detection and bad-tag authentication failure protection.
  - Export System (`ExportManager`):
    - RFC 4180 CSV export of all readings with proper escaping, timestamps, notes, and meter identity.
    - Native Android `PdfDocument` generation for formal electricity bill statements with tabular slab itemization, energy charges, fixed utility charges, VAT, and total payable in BDT.
    - Android `FileProvider` integration configured in `AndroidManifest.xml` (`file_paths.xml`) for secure system share intents.
  - UI Integration:
    - History screen updated with CSV and PDF export action buttons.
    - Bill Breakdown screen updated with PDF Statement export action button in TopAppBar.
    - Settings screen updated with "Encrypted Backup & Restore" navigation entry point.
    - Dedicated `BackupScreen` with passphrase validation, loading indicator, and share intent triggers.
  - Localization & Accessibility:
    - 100% string resource coverage across `values/strings.xml` and `values-bn/strings.xml` for full English and Bengali support.
    - Accessibility semantics, test tags, content descriptions, and large text layout resilience.
  - Unit tests: 67/67 unit tests passed (+7 tests for `BackupManagerTest`, `ExportManagerTest`, `BackupViewModelTest`).
  - Static checks: `./gradlew lintDebug detekt ktlintCheck` passed with 0 errors and 0 warnings.
  - Connected UI tests: 9/9 tests passed on emulator (including `BackupAndExportTest`).
  - Runtime verification: Verified on emulator, captured screenshots: `screenshots/phase7_export_history.png`, `screenshots/phase7_backup.png`. Logcat verified 0 crashes/exceptions.

## UI Enhancements
- **Fintech-Style Bottom Navigation Bar**:
  - Replaced floating action button (`ExtendedFloatingActionButton`) with an elevated circular action button situated directly in the center of the bottom navigation bar: `[Home] [Analytics] [Center + Button] [History] [Settings]`.
  - Styled with fintech aesthetics (CircleShape, Deep Teal `#00695E` surface, White `+` icon, 4dp shadow elevation, -4dp offset popping up slightly from the bar, label "Add reading" / `add_reading`).
  - Tablet navigation rail layout preserved with matching center circular action button (`rail_add_reading`).
  - Preserved semantic `testTag("fab_add_reading")` ensuring all UI tests and automated accessibility tools continue to function without breakage.
  - Static checks: `./gradlew lintDebug detekt ktlintCheck` passed with 0 errors and 0 warnings.
  - Unit tests: 67/67 unit tests passed.
  - Connected UI tests: 9/9 tests passed on emulator (`medium_phone(AVD) - 16`).
  - Release build: `./gradlew assembleRelease` passed with R8 minification, ProGuard rules, and resource shrinking (24 MB release APK produced at `app/build/outputs/apk/release/app-release.apk`).
  - Runtime verification on live emulator: Verified smooth tap navigation to Add Reading screen and captured screenshots: `screenshots/fintech_bottom_bar.png` and `screenshots/fintech_add_reading_opened.png`.

## Fix & Polish: 10 Issues Execution

### Issue 1: Cold start shows an EMPTY app for a moment
- **Root Cause**:
  1. `androidx.core:core-splashscreen` API was not integrated; no starting splash theme with `Theme.SplashScreen` and no `setKeepOnScreenCondition { !appReady }` were registered in `MainActivity`. The system splash was dismissed immediately upon the first drawn frame.
  2. `MainActivity.kt` initialized `WattWiseTheme` without collecting `settingsPreferences.themeMode`, defaulting to `isSystemInDarkTheme()`, which caused a light/dark flash when user preferences differed from system settings.
  3. `DashboardScreen.kt`, `HistoryScreen.kt`, and `BillScreen.kt` lacked loading guard branches; when their respective ViewModels emitted initial default states (`isLoading = true` with 0 usage/units or empty reading lists), the composables immediately rendered real empty states (`InitialReadingRequiredBanner` / `EmptyState` / 0 kWh) before Room and DataStore emitted the actual cached records.
- **Fix Details**:
  1. Added `androidx.core:core-splashscreen:1.0.1` to `libs.versions.toml` and `app/build.gradle.kts`.
  2. Defined `Theme.WattWise.Starting` in `themes.xml` with parent `Theme.SplashScreen` and configured `MainActivity` in `AndroidManifest.xml`.
  3. Created `MainViewModel` to collect preferences (`themeMode`, `appLanguage`, `onboardingCompleted`, `appLockEnabled`) and active meter data before releasing `isAppReady`.
  4. Installed splash screen in `MainActivity.kt` with `setKeepOnScreenCondition { !mainViewModel.isAppReady.value }`.
  5. Connected `mainViewModel.themeMode` to root `WattWiseTheme(darkTheme = isDarkTheme)`.
  6. Added comprehensive skeleton shimmer components (`DashboardSkeleton`, `HistorySkeleton`, `BillSkeleton`, `AnalyticsSkeleton`, `UsageSummarySkeleton`, `BudgetOutlookSkeleton`, `ReadingItemSkeleton`) and loading guards in screen composables.
  7. Resolved feedback loop in `HistoryViewModel` by decoupling `_searchQuery` and `_activeFilter` from combined state emissions.
- **Verification**:
  - `./gradlew assembleDebug`: Passed.
  - `./gradlew lintDebug detekt ktlintCheck`: Passed (0 errors, 0 warnings).
  - `./gradlew testDebugUnitTest`: Passed (68/68 tests passed, including `MainViewModelTest`).
  - `./gradlew connectedDebugAndroidTest`: Passed (9/9 tests passed on `emulator-5554`).
  - Cold start verification on emulator: 10 consecutive cold starts via `am force-stop` and `am start -W`. Verified smooth transition from system splash to content with zero empty flash or crash. Screenshot saved to `screenshots/issue1_verified.png`.
- **Status**: Completed.

### Issue 2: Dashboard Hero Card (odd white line under progress bar)
- **Root Cause**:
  1. The progress bar previously used nested `Box` composables with percentage-based fractional width (`fillMaxWidth(cycleProgress)`), which caused edge aliasing and subpixel clipping artifacts resembling a white hairline or dot when progress was small.
  2. The hero card had no explicit `clip(HeroShape)` on the container card, allowing card container color bleeding and corner anti-aliasing artifacts against the gradient background.
  3. In `DashboardScreen.kt`, the hero card was erroneously passed `state.budget?.percentageUsed` instead of the actual cycle progress fraction (`daysElapsed / totalDays`), causing visual misalignment between the text ("Day X of Y") and the progress bar.
- **Fix Details**:
  1. Replaced the nested Box layout with a custom Canvas drawing a clean PillShape track (`Color.White.copy(alpha = 0.24f)`) and animated progress fill (`Color.White`) using `animateFloatAsState` (600ms `FastOutSlowInEasing`).
  2. Added `.clip(RoundedCornerShape(3.dp))` to strictly contain the Canvas track and fill with zero bleeding or stop-indicator dots.
  3. Added aligned row above the progress bar with cycle info ("Day X of Y • Z days left") on the left and percentage ("40%") on the right.
  4. Updated Card in `GradientHeroCard.kt` to explicitly use `containerColor = Color.Transparent`, `border = null`, and `clip(HeroShape)` on both Card and internal gradient Box to eliminate hairline borders or corner clipping leaks.
  5. Updated `DashboardScreen.kt` to calculate and pass true cycle progress (`(daysElapsed / totalDays).coerceIn(0f, 1f)`).
- **Verification**:
  - `./gradlew assembleDebug`: Passed.
  - `./gradlew lintDebug detekt ktlintCheck`: Passed (0 errors, 0 warnings).
  - `./gradlew testDebugUnitTest`: Passed (68/68 tests passed).
  - `./gradlew connectedDebugAndroidTest`: Passed (9/9 tests passed on `emulator-5554`).
  - Emulator verification: Verified hero card with clean cycle progress at 40%, smooth animation, zero white line artifacts or corner defects. Screenshot saved to `screenshots/issue2_hero_card.png`.
- **Status**: Completed.

### Issue 3: Three cards under hero (Today / Yesterday / Average daily)
- **Root Cause**:
  - The previous layout used three separate `StatCard` composables inside a `Row(horizontalArrangement = Arrangement.spacedBy(cardGap))`. Each card had independent borders, elevation shadows, and padding, causing text to squeeze and wrap awkwardly on narrow devices and completely break at high font scales (1.3x+).
- **Fix Details**:
  1. Created a unified `UsageSummaryCard` component (`UsageSummaryCard.kt`) presenting Today, Yesterday, and Daily Avg in a single cohesive card.
  2. Implemented 3 equal-weight columns separated by subtle 1dp vertical dividers (`MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)`).
  3. Included circular tinted icon wells (`primary.copy(alpha = 0.12f)`), muted `labelMedium` labels, bold `NumberMedium` values with tabular figures, and baseline-aligned "kWh" unit suffixes.
  4. Added responsive `BoxWithConstraints`: automatically adapts from a 3-column horizontal layout into a clean stacked 3-row list with horizontal dividers when `maxWidth < 280.dp` or `fontScale > 1.3f`.
  5. Tested and verified on 1.0x, 1.5x, and 2.0x font scales.
- **Verification**:
  - `./gradlew assembleDebug`: Passed.
  - `./gradlew lintDebug detekt ktlintCheck`: Passed (0 errors, 0 warnings).
  - `./gradlew testDebugUnitTest`: Passed (68/68 tests passed).
  - `./gradlew connectedDebugAndroidTest`: Passed (9/9 tests passed on `emulator-5554`).
  - Emulator verification: Verified 3-column layout at 1.0x and stacked 3-row layout at 1.5x/2.0x font scale. Screenshots saved to `screenshots/issue3_font_1_0.png`, `screenshots/issue3_font_1_5.png`, `screenshots/issue3_font_2_0.png`.
- **Status**: Completed.

### Issue 4: Monthly Budget & Outlook section
- **Root Cause**:
  1. The previous budget section relied heavily on verbose multi-line descriptive text paragraphs with redundant labels rather than clear visual hierarchy.
  2. The budget progress representation was a basic linear progress bar lacking animated arc/ring visualization, dual status indicator (color + icon chip), and segmented breakdown of spent vs projected vs limit buffer.
  3. When no budget was configured, the app had no dedicated call-to-action inviting users to set a monthly electricity spending limit with preset amounts.
- **Fix Details**:
  1. Created `MonthlyBudgetAndOutlookCard.kt` following strict design system tokens:
     - Left: 110dp `UsageRing` with animated sweep arc, centered percentage ("40%"), and dynamic status chip ("On track" 0-70% Teal, "Watch out" 70-90% Amber, "Over budget" 90%+ Red) using both color and icon.
     - Right: 3 compact stat rows with circular tinted icon wells: Spent so far, Projected month-end with range, and Budget limit + subtle Edit button.
     - Bottom: Slim segmented horizontal bar showing proportional Spent (Teal/Amber), Projected remainder (Primary container), and Buffer (outlineVariant), plus a single-line insight ("On track to stay within budget by ৳...").
     - Responsive design: stacks vertically when `maxWidth < 320.dp` or `fontScale > 1.3f`.
  2. Created `SetBudgetCtaCard` displayed when no custom budget is configured, presenting an inviting icon, concise value proposition, and "+ Set monthly budget" button.
  3. Created `SetBudgetDialog` with preset quick-select chips (৳1500, ৳2500, ৳3500, ৳5000), numeric input, and validation.
  4. Updated `DashboardViewModel` with `onSaveBudget(amountBDT)` and custom budget tracking.
  5. Updated `DashboardScreen.kt` to seamlessly toggle between CTA card and live budget outlook card.
- **Verification**:
  - `./gradlew assembleDebug`: Passed.
  - `./gradlew lintDebug detekt ktlintCheck`: Passed (0 errors, 0 warnings).
  - `./gradlew testDebugUnitTest`: Passed (68/68 tests passed).
  - `./gradlew connectedDebugAndroidTest`: Passed (9/9 tests passed on `emulator-5554`).
  - Emulator verification: Verified both `SetBudgetCtaCard` and `MonthlyBudgetAndOutlookCard` after saving budget. Screenshots saved to `screenshots/issue4_no_budget.png` and `screenshots/issue4_budget_outlook.png`. Logcat verified 0 crashes.
- **Status**: Completed.

### Issue 5: History page filters
- **Root Cause**:
  1. The previous History screen contained an unnecessary search bar and unstructured filter chips ("All", "High use", "Estimated", "Reset") that did not allow users to view their consumption by billing month or compare historical statements month by month.
  2. Users lacked an intuitive way to navigate across years and months, observe monthly aggregated usage and costs, or see month-specific empty states.
- **Fix Details**:
  1. Completely removed the search input and the legacy filter chips.
  2. Built `HistoryHeaderAndYearSelector`: Year stepper with `<` and `>` chevrons, displaying current year with Bengali numeral support, plus PDF export button.
  3. Built `HistoryMonthChipsRow`: Horizontally scrollable 12-month chip row (Jan–Dec, in English or Bangla depending on locale/digits preference). Months without data are dimmed (`alpha = 0.45f`) but still fully selectable.
  4. Injected `SavedStateHandle` into `HistoryViewModel` (`history_selected_year` and `history_selected_month`) defaulting to the current Dhaka year and month, ensuring filter selection survives configuration changes and process death.
  5. Built `MonthSummaryCard`: Displayed above the readings list, summarizing Total Usage (kWh), Estimated Bill (BDT ৳), Readings Count, and Daily Average (kWh/day) formatted with Bengali digits and locale strings.
  6. Filtered the readings list strictly to the selected year and month, while accurately computing delta against adjacent prior readings.
  7. Added Material 3 `SwipeToDismissBox` supporting swipe-to-delete with Undo snackbar and swipe-to-edit, alongside click-to-view detail dialog.
  8. Created a friendly `EmptyState` when the selected month has no readings, with a direct CTA to add a reading.
- **Verification**:
  - `./gradlew assembleDebug`: Passed.
  - `./gradlew lintDebug detekt ktlintCheck`: Passed (0 errors, 0 warnings).
  - `./gradlew testDebugUnitTest`: Passed (69/69 tests passed).
  - `./gradlew connectedDebugAndroidTest`: Passed (9/9 tests passed on `emulator-5554`).
  - Emulator verification: Verified Year stepper, Month chips row, friendly empty state, and reading item display. Screenshots saved to `screenshots/issue5_history_empty_month.png` and `screenshots/issue5_history_filters.png`. Logcat verified 0 crashes.
- **Status**: Completed.





