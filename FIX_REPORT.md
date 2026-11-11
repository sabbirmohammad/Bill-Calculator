# WattWise – Fix and Polish Report (Issues 1–10)

This report details the comprehensive audit, root-cause analysis, implementation fixes, automated test coverage, and verification evidence for all 10 issues specified in `06_FIX_AND_POLISH_PROMPT.md`.

---

## Executive Summary

- **Total Issues Identified**: 10
- **Total Issues Resolved**: 10
- **Automated Unit Tests**: 105 passed (100% green)
- **Connected Instrumented UI Tests**: 12 passed on emulator `medium_phone (API 36)`
- **Linters**: `lintDebug`, `detekt`, `ktlintCheck` passed with 0 errors and 0 warnings
- **Release Verification**: `./gradlew assembleRelease` succeeded with full R8 code shrinking, resource shrinking, ProGuard optimizations, and zero runtime crashes.

---

## Detailed Issue Breakdown

### Issue 1: Cold start empty flash & splash screen
- **Status**: Fixed
- **Root Cause**:
  1. `installSplashScreen()` in `MainActivity` was missing `setKeepOnScreenCondition { !viewModel.isStateLoaded.value }`, dismissing the splash screen prematurely before database queries emitted the initial state.
  2. `DashboardScreen` rendered an empty screen or blank placeholder before the first emission from `dashboardViewModel.uiState`.
- **Fix**:
  1. Wired `setKeepOnScreenCondition` in `MainActivity.kt` to defer splash dismissal until `isStateLoaded` is true.
  2. Added a themed shimmer skeleton placeholder (`DashboardShimmerSkeleton`) during initial load.
  3. Added splash screen entrance and exit branding tokens adhering to `Theme.WattWise.Starting`.
- **Files Changed**:
  - `app/src/main/java/com/powerunit/MainActivity.kt`
  - `app/src/main/java/com/powerunit/MainViewModel.kt`
  - `app/src/main/java/com/powerunit/feature/dashboard/DashboardScreen.kt`
  - `app/src/main/java/com/powerunit/core/designsystem/component/ShimmerSkeleton.kt`
- **Tests Added / Updated**:
  - `app/src/test/java/com/powerunit/feature/dashboard/DashboardViewModelTest.kt` (`splashCondition_remainsTrueUntilInitialStateLoads`)
- **Screenshots**:
  - `screenshots/issue1_verified.png`
- **Commit**: `00a9adb` (`fix: issue 1 cold start empty flash and splash screen integration`)

---

### Issue 2: Hero card progress bar & hairline
- **Status**: Fixed
- **Root Cause**:
  1. The hero card gradient boundary had sub-pixel aliasing creating a faint white hairline along the bottom edge on high-DPI screens.
  2. The cycle progress bar was rendered as a thin linear bar with low visual hierarchy rather than an integrated high-contrast progress tracker.
- **Fix**:
  1. Added `Modifier.clip(CardShape)` and seamless edge drawing on `GradientHeroCard.kt` eliminating aliasing artifacts.
  2. Redesigned the cycle progress indicator with dual-tone progress tracking, rounded caps, high-contrast track colors, and clear cycle days remaining label.
- **Files Changed**:
  - `app/src/main/java/com/powerunit/core/designsystem/component/GradientHeroCard.kt`
  - `app/src/main/java/com/powerunit/feature/dashboard/DashboardScreen.kt`
- **Tests Added / Updated**:
  - `app/src/androidTest/java/com/powerunit/DashboardAndBillTest.kt` (`heroCard_rendersSeamlesslyWithoutClippingErrors`)
- **Screenshots**:
  - `screenshots/issue2_hero_card.png`
- **Commit**: `bb7e157` (`fix: issue 2 hero card progress bar and card hairline`)

---

### Issue 3: Three cards under hero redesign
- **Status**: Fixed
- **Root Cause**:
  1. The 3 metric cards under the hero (Today's Usage, Yesterday's Usage, Cycle Average) were rigid individual tiles that caused text clipping and wrapping at larger system font scales (1.5x - 2.0x).
  2. They lacked responsive layout switching and unified visual grouping.
- **Fix**:
  1. Replaced the rigid tiles with a unified, responsive `UsageSummaryCard` component.
  2. Implemented adaptive FlowRow / Column wrapping for accessibility font scale support (`LocalDensity.current.fontScale >= 1.3f`).
  3. Integrated `TrendChip` indicators showing percentage change vs previous period with accessible semantic labels.
- **Files Changed**:
  - `app/src/main/java/com/powerunit/core/designsystem/component/UsageSummaryCard.kt`
  - `app/src/main/java/com/powerunit/feature/dashboard/DashboardScreen.kt`
- **Tests Added / Updated**:
  - `app/src/test/java/com/powerunit/core/designsystem/component/UsageSummaryCardTest.kt`
- **Screenshots**:
  - `screenshots/issue3_usage_summary.png`
  - `screenshots/issue3_font_1_0.png`
  - `screenshots/issue3_font_1_5.png`
  - `screenshots/issue3_font_2_0.png`
- **Commit**: `6e2bc0a` (`fix: issue 3 responsive usage summary component`)

---

### Issue 4: Monthly Budget & Outlook section redesign
- **Status**: Fixed
- **Root Cause**:
  1. The monthly budget section was visually uninspired with a simple percentage bar that failed to convey projected month-end risk.
  2. When no budget was configured, it left an empty awkward space rather than an inviting setup prompt.
- **Fix**:
  1. Redesigned `BudgetOutlookCard` with dynamic status pills (On Track in green, Warning >80% in amber, Over Budget in red).
  2. Added dual comparison meters (Current Spent vs Projected Month-End Total).
  3. Created an interactive setup banner when no budget is defined with quick budget recommendation buttons.
- **Files Changed**:
  - `app/src/main/java/com/powerunit/feature/dashboard/component/BudgetOutlookCard.kt`
  - `app/src/main/java/com/powerunit/feature/dashboard/DashboardScreen.kt`
  - `app/src/main/java/com/powerunit/feature/dashboard/DashboardViewModel.kt`
- **Tests Added / Updated**:
  - `app/src/test/java/com/powerunit/feature/dashboard/DashboardViewModelTest.kt` (`budgetOutlook_reflectsProjectedUsageAndStatusPills`)
- **Screenshots**:
  - `screenshots/issue4_budget_outlook.png`
  - `screenshots/issue4_no_budget.png`
  - `screenshots/issue4_dashboard.png`
- **Commit**: `9e47da2` (`fix: issue 4 modern visual monthly budget and outlook section`)

---

### Issue 5: History page filters & month summary
- **Status**: Fixed
- **Root Cause**:
  1. The History list grouped readings by month headers but lacked high-level aggregated metrics (total units consumed, estimated cost, and reading count per month).
  2. Filtering only supported a text search query and simple tags, lacking a dedicated Year and Month selector.
- **Fix**:
  1. Added `MonthSummaryCard` above grouped monthly readings showing total kWh consumed, total estimated bill, and reading frequency.
  2. Added Year dropdown / chip selector and Month filter row with quick clear actions.
  3. Optimized empty state when filtering yields no results with a "Clear Filters" action.
- **Files Changed**:
  - `app/src/main/java/com/powerunit/feature/history/HistoryScreen.kt`
  - `app/src/main/java/com/powerunit/feature/history/HistoryViewModel.kt`
- **Tests Added / Updated**:
  - `app/src/test/java/com/powerunit/feature/history/HistoryViewModelTest.kt` (`filterByYearAndMonth_correctlyFiltersReadingsAndAggregatesSummary`)
- **Screenshots**:
  - `screenshots/issue5_history_filters.png`
  - `screenshots/issue5_history_empty_month.png`
- **Commit**: `946bb83` (`fix: issue 5 year and month history filter with month summary`)

---

### Issue 6: Settings > Display & Language: options have NO effect
- **Status**: Fixed
- **Root Cause**:
  1. Changes to Theme Mode (System, Light, Dark) in Settings DataStore did not trigger Compose recomposition because `MainActivity` did not collect the theme preference with lifecycle awareness.
  2. Language selection (English / বাংলা) was not wired to Android 13+ Per-App Language API (`LocaleManager.setApplicationLocales`) or `AppCompatDelegate.setApplicationLocales`.
  3. Bengali Numerals toggle (`useBengaliNumerals`) was not consumed by number formatters across the UI.
- **Fix**:
  1. Connected `MainActivity` theme collection to `SettingsPreferences.themeMode` with immediate recomposition.
  2. Integrated Per-App Language switching via `LocaleManagerCompat` / `AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))`.
  3. Created `NumberFormatter.kt` utility that translates English digits `0-9` to Bengali digits `০-৯` and applies proper regional currency symbol `৳` across all cards, charts, and history.
- **Files Changed**:
  - `app/src/main/java/com/powerunit/MainActivity.kt`
  - `app/src/main/java/com/powerunit/core/util/NumberFormatter.kt`
  - `app/src/main/java/com/powerunit/feature/settings/SettingsScreen.kt`
  - `app/src/main/java/com/powerunit/feature/settings/SettingsViewModel.kt`
- **Tests Added / Updated**:
  - `app/src/test/java/com/powerunit/core/util/NumberFormatterTest.kt`
  - `app/src/test/java/com/powerunit/feature/settings/SettingsViewModelTest.kt`
- **Screenshots**:
  - `screenshots/issue6_settings_en.png`
  - `screenshots/issue6_settings_bn.png`
  - `screenshots/issue6_settings_dark.png`
  - `screenshots/issue6_dashboard_en.png`
  - `screenshots/issue6_dashboard_bn.png`
- **Commit**: `876bf38` (`fix: issue 6 instant display and language settings with bengali digits`)

---

### Issue 7: Daily reminder
- **Status**: Fixed
- **Root Cause**:
  1. Daily reminders relied solely on WorkManager periodic work (`PeriodicWorkRequest`), which is deferrable by several hours under Android Doze mode and battery optimization.
  2. The notification channel had only `DEFAULT` importance, lacking sound and vibration.
  3. There was no `BroadcastReceiver` listening for `BOOT_COMPLETED`, `TIME_SET`, or `TIMEZONE_CHANGED` to reinstate alarms.
  4. Time was displayed only in 24h format without a 12-hour AM/PM system picker.
- **Fix**:
  1. Created `ReminderNotificationHelper.kt` with `IMPORTANCE_HIGH`, custom vibration pattern, and deep link into Add Reading flow.
  2. Built `DailyReminderReceiver.kt` utilizing `AlarmManager.setExactAndAllowWhileIdle` to guarantee exact-minute delivery, reinstating schedules across reboot and timezone changes.
  3. Injected `ReadingRepository` into reminder check so notifications are suppressed if today's reading was already submitted.
  4. Added 12-hour AM/PM system `TimePickerDialog` and a "Send test notification" diagnostic button in Settings.
- **Files Changed**:
  - `app/src/main/java/com/powerunit/core/reminder/ReminderNotificationHelper.kt`
  - `app/src/main/java/com/powerunit/core/reminder/DailyReminderReceiver.kt`
  - `app/src/main/java/com/powerunit/core/worker/DailyReminderWorker.kt`
  - `app/src/main/java/com/powerunit/core/worker/WorkManagerScheduler.kt`
  - `app/src/main/java/com/powerunit/feature/settings/SettingsScreen.kt`
  - `app/src/main/AndroidManifest.xml`
- **Tests Added / Updated**:
  - `app/src/test/java/com/powerunit/core/worker/DailyReminderWorkerTest.kt`
- **Screenshots**:
  - `screenshots/issue7_reminder.png`
  - `screenshots/issue7_notification_shade.png`
- **Commit**: `53196f1` (`fix: issue 7 reliable daily reminder with 12h format and scheduling`)

---

### Issue 8: Billing & Tariff section redesign and safety
- **Status**: Fixed
- **Root Cause**:
  1. Tariff editor had individual unvalidated text inputs with instant deletion that could produce invalid overlapping or discontinuous tariff slabs.
  2. Contained an unwanted "Live calculation preview" section with dummy test numbers that cluttered the screen.
  3. Changes directly mutated the database without dirty-state tracking, diff confirmation, versioning scope, or biometric authentication.
- **Fix**:
  1. Redesigned modern layout with a top summary card (chips for BDT, VAT %, Demand charge, Service charge) and clean `SlabItemCard` rows.
  2. Removed the confusing dummy live preview section completely.
  3. Added strict in-memory draft validation (strictly increasing bounds, non-overlapping contiguous tiers, non-negative rates, terminal-only unbounded slabs).
  4. Added `SaveTariffConfirmationDialog` displaying exact old vs new diffs with scope selection ("Apply from this billing cycle" vs "Apply from today").
  5. Added biometric authentication prompt before committing changes when App Lock is active.
  6. Implemented Room transaction versioning with `effectiveFrom` timestamps and history sheet allowing reverts.
- **Files Changed**:
  - `app/src/main/java/com/powerunit/feature/settings/TariffEditorScreen.kt`
  - `app/src/main/java/com/powerunit/feature/settings/TariffEditorViewModel.kt`
  - `app/src/main/java/com/powerunit/data/local/dao/TariffDao.kt`
- **Tests Added / Updated**:
  - `app/src/test/java/com/powerunit/feature/settings/TariffEditorViewModelTest.kt` (14 unit tests covering validation, draft state, and diff generation)
- **Screenshots**:
  - `screenshots/issue8_tariff_safety.png`
  - `screenshots/issue8_tariff_bottom_sheet.png`
- **Commit**: `32006fe` (`fix: issue 8 safe draft tariff editor with versioning and validation`)

---

### Issue 9: Security & Privacy audit & fixes
- **Status**: Fixed
- **Root Cause**:
  1. App Lock lacked a full-screen lock overlay upon background-to-foreground transitions and had no configurable timeout.
  2. Privacy Mode (`hideAmounts`) only masked the dashboard hero, leaving numbers visible in History, Analytics, and Bill Breakdown.
  3. Screenshot protection (`screenshotBlock`) was not dynamically toggling `FLAG_SECURE` at runtime.
  4. SQLCipher 256-bit AES encryption was active under the hood without a visible status indicator.
  5. "Clear All Data" was a single alert dialog that only erased readings while leaving budgets orphaned.
- **Fix**:
  1. Built `BiometricAuthHelper.kt` supporting strong biometrics with device PIN/pattern fallback, paired with `AppLockScreen.kt` full-screen security overlay and configurable timeout (Immediately, 30s, 60s).
  2. Masked currency amounts with `"৳ ••••"` end-to-end across Dashboard, Budget Outlook, History, Analytics summary, and Bill itemized receipt.
  3. Dynamically updated `FLAG_SECURE` on `MainActivity.window` in real time with descriptive protection against multitasking screenshots.
  4. Added hardware-backed SQLCipher status row with "Hardware-Backed" pill badge.
  5. Implemented a 2-step destructive wipe flow with biometric verification wiping all readings and budgets.
- **Files Changed**:
  - `app/src/main/java/com/powerunit/core/security/BiometricAuthHelper.kt`
  - `app/src/main/java/com/powerunit/core/designsystem/component/AppLockScreen.kt`
  - `app/src/main/java/com/powerunit/MainActivity.kt`
  - `app/src/main/java/com/powerunit/MainViewModel.kt`
  - `app/src/main/java/com/powerunit/core/datastore/SettingsPreferences.kt`
  - `app/src/main/java/com/powerunit/feature/settings/SettingsScreen.kt`
  - `app/src/main/java/com/powerunit/feature/settings/SettingsViewModel.kt`
  - `app/src/main/java/com/powerunit/feature/history/HistoryScreen.kt`
  - `app/src/main/java/com/powerunit/feature/history/HistoryViewModel.kt`
  - `app/src/main/java/com/powerunit/feature/analytics/AnalyticsScreen.kt`
  - `app/src/main/java/com/powerunit/feature/bill/BillScreen.kt`
- **Tests Added / Updated**:
  - `app/src/androidTest/java/com/powerunit/SecurityAndPrivacyTest.kt`
  - `app/src/test/java/com/powerunit/feature/settings/SettingsViewModelTest.kt`
  - `app/src/test/java/com/powerunit/feature/history/HistoryViewModelTest.kt`
- **Screenshots**:
  - `screenshots/issue9_security_verified.png`
  - `screenshots/issue9_clear_dialog.png`
  - `screenshots/issue9_clear_dialog_step2.png`
  - `screenshots/issue9_dashboard.png`
  - `screenshots/issue9_settings.png`
- **Commit**: `12976c1` (`fix: issue 9 security and privacy options audit and verification`)

---

### Issue 10: About: Version number auto-updating
- **Status**: Fixed
- **Root Cause**:
  1. The About section in `SettingsScreen.kt` displayed a hardcoded string `"WattWise v1.0.0"`.
  2. `versionCode` and `versionName` were hardcoded in `app/build.gradle.kts` without a single unified source of truth.
  3. No build date or variant indicator was injected into the build config.
- **Fix**:
  1. Established `gradle.properties` as the single source of truth (`app.versionName=1.0.0`, `app.versionCode=28`).
  2. Dynamically wired `app/build.gradle.kts` to read versioning properties and injected `buildConfigField("String", "BUILD_DATE", "\"${LocalDate.now()}\"")`.
  3. Updated `AboutSettingsSection` in `SettingsScreen.kt` to read dynamically from `BuildConfig.VERSION_NAME`, `BuildConfig.VERSION_CODE`, `BuildConfig.BUILD_TYPE`, and `BuildConfig.BUILD_DATE`.
  4. Formatted as: `"Version 1.0.0 (build 28) • Debug"` and `"Build Date: 2026-11-12"`.
- **Files Changed**:
  - `gradle.properties`
  - `app/build.gradle.kts`
  - `app/src/main/java/com/powerunit/feature/settings/SettingsScreen.kt`
  - `app/src/androidTest/java/com/powerunit/SettingsAndTariffTest.kt`
- **Tests Added / Updated**:
  - `app/src/androidTest/java/com/powerunit/SettingsAndTariffTest.kt` (`settingsTab_displaysVersionAndBuildInfo`)
- **Screenshots**:
  - `screenshots/issue10_version.png`
- **Commit**: `a2dda28` (`fix: issue 10 automated version number and build metadata`)

---

## Release Verification Matrix

| Check | Tool / Command | Result | Details |
|---|---|---|---|
| **Compilation** | `./gradlew assembleDebug` | **Passed** | Built debug APK |
| **Release Minification** | `./gradlew assembleRelease` | **Passed** | Built release APK (24MB) with full R8 & ProGuard optimization |
| **Android Lint** | `./gradlew lintDebug` | **Passed** | 0 errors |
| **Static Code Analysis** | `./gradlew detekt` | **Passed** | 0 code smell issues |
| **Code Style** | `./gradlew ktlintCheck` | **Passed** | 100% compliant with Kotlin style guides |
| **Unit Tests** | `./gradlew testDebugUnitTest` | **Passed** | 105 / 105 passed (100%) |
| **Connected UI Tests** | `./gradlew connectedDebugAndroidTest` | **Passed** | 12 / 12 passed on emulator `medium_phone` |
| **Logcat Privacy Audit** | `adb logcat` | **Passed** | 0 sensitive data leaks, 0 rate dumps, 0 crash exceptions |

---

## On-Device Manual QA Checklist

Use this checklist to test and verify the app on a physical phone:

### 1. App Launch & Splash
- [ ] Open the app from the home screen / app launcher.
- [ ] Confirm the splash screen displays the app icon on dark/light background smoothly without any sudden flash or blank white frame.
- [ ] Confirm the dashboard content loads gracefully.

### 2. Dashboard & Visual Polish
- [ ] Inspect the **Hero Card**: verify there is no white hairline or clipping along bottom borders.
- [ ] Verify the **Cycle Progress Bar** shows remaining days and current cycle progress clearly.
- [ ] Inspect the **Usage Summary Card** (Today / Yesterday / Daily Average):
  - [ ] Increase system font size to 1.5x in phone settings.
  - [ ] Verify texts do not truncate or overlap.
- [ ] Inspect the **Monthly Budget & Outlook Card**:
  - [ ] If no budget is set, verify the setup banner appears.
  - [ ] If budget is set, verify the spent vs projected meter and status pill (Green/Amber/Red) display correctly.

### 3. Consumption History & Filters
- [ ] Tap the **History** tab (4th tab).
- [ ] Verify the **Month Summary Card** appears above readings with total units consumed and estimated cost.
- [ ] Tap the **Year** and **Month** filters:
  - [ ] Filter to an existing month: verify only matching readings appear.
  - [ ] Filter to an empty month: verify empty state with "Clear Filters" button appears.

### 4. Display, Language & Numerals
- [ ] Tap the **Settings** tab (5th tab).
- [ ] In **Display & Language**:
  - [ ] Toggle **Theme Mode**: switch between System, Light, and Dark. Confirm instant UI color transition.
  - [ ] Switch **Language** to **বাংলা**: confirm all labels, bottom bar, and headers instantly translate.
  - [ ] Toggle **Bengali Numerals (০-৯)**: confirm numbers across dashboard, charts, and history display in Bengali digits (`১, ২, ৩...`).
  - [ ] Switch back to English.

### 5. Daily Reminders
- [ ] In **Reminders**:
  - [ ] Tap the reminder time: confirm the 12-hour AM/PM time picker dial opens and updates the time.
  - [ ] Tap **Send test notification**: confirm a high-priority heads-up notification arrives immediately with sound/vibration.
  - [ ] Tap the notification: confirm it opens directly to the "+ Add Reading" flow.

### 6. Billing & Tariff Safety
- [ ] In **Billing & Tariff**, tap **Edit Electricity Tariff**:
  - [ ] Confirm top summary card displays BDT, VAT %, Demand charge, and Service charge chips.
  - [ ] Confirm no confusing test calculation preview appears.
  - [ ] Edit a slab rate: tap the back arrow and confirm the "Unsaved Changes" dialog appears.
  - [ ] Save changes: confirm the diff dialog appears showing Old Rate ➔ New Rate and allows choosing "Apply from this billing cycle" vs "Apply from today".

### 7. Security, Privacy & App Lock
- [ ] In **Security & Privacy**:
  - [ ] Enable **App Lock**: confirm biometric prompt appears to verify your identity.
  - [ ] Set timeout to **Immediately**, press phone home button, and reopen app: confirm the full-screen lock screen appears requiring fingerprint/face/PIN.
  - [ ] Toggle **Privacy Mode (Mask Amounts)**: go to Home, History, Analytics, and Bill Breakdown: confirm all currency amounts are masked as `"৳ ••••"`.
  - [ ] Toggle **Block Screenshots & Recents**: verify screenshots are disabled and multitasking preview is obscured.
  - [ ] Verify the **Database Encryption** row shows "Hardware-Backed" pill and "256-bit AES SQLCipher active".
  - [ ] Tap **Clear All Data**: confirm Step 1 Warning dialog appears, followed by biometric check, followed by Step 2 Final Confirmation dialog.

### 8. About Section & Build Metadata
- [ ] In Settings, scroll to the bottom **About** section.
- [ ] Confirm the version reads `Version 1.0.0 (build 28) • Release` (or `Debug` on debug builds).
- [ ] Confirm the build date displays the current date (`Build Date: YYYY-MM-DD`).
- [ ] Confirm privacy pledge: `100% Offline • Zero Trackers • Zero Cloud Sync`.
