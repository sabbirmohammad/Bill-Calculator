# Changelog

All notable changes to WattWise will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.5] - 2026-11-17

### Fixed
- **Add Reading Live Ticker Overlap**: Resolved visual overlap in `AddReadingScreen` where the consumption delta pill (`+4.2 kWh • ≈ ৳35.70`) and the active tariff slab badge (`2nd Slab • Σ ... kWh`) rendered directly on top of each other inside `AnimatedVisibility`. Enclosed ticker badges in a properly spaced vertical `Column` layout.

## [1.1.4] - 2026-11-17

### Fixed & Enhanced
- **Home Recent Readings Synchronized with History**: Unified reading presentation by extracting `buildReadingUiModels()` from `HistoryViewModel`. Home screen recent readings now use the exact same calculation pipeline as the History screen.
- **Accurate Predecessor Delta Calculation**: Preserved history predecessor readings so that the third recent reading on the home screen no longer shows as an "Initial reading" with 0.0 units.
- **Progressive Tariff Slabs & Badges on Dashboard**: Replaced the flat fallback multiplier with tiered Bangladesh tariff slab rates, live slab badges, and accurate progressive costs on dashboard recent readings.
- **Localization Integration**: Added full Bengali numeral and Bangla month localization support to dashboard recent readings and detail dialogs.
- **In-App Deletion from Dashboard**: Enabled direct reading deletion from the dashboard reading detail dialog with automatic Room flow reactive updates.

## [1.1.3] - 2026-11-16

### Changed & Enhanced
- **Reversed Hero Card Layout**: Highlighted estimated bill cost in prominent bold typography as the primary hero metric on the dashboard, with energy consumption (units/kWh) positioned neatly underneath.
- **Redesigned Monthly Budget & Outlook Card**:
  - Replaced circular ring with a sleek full-width horizontal dual-segment progress bar displaying real-time spend and projected month-end trajectory.
  - Resolved "Watch out" status badge wrapping and overflow on mobile screens with flexible single-line layout constraints.
  - Added dedicated Cycle Progress Timeline pill displaying elapsed days, days remaining, and visual monthly progression.
  - Converted Spent so far and Projected bill metrics into balanced Bento stat tiles with contextual buffer indicators.

## [1.1.2] - 2026-11-16

### Fixed & Enhanced
- **Progressive Slab-Aware Billing for New Readings**: Accurately tracks cumulative consumption in the current billing cycle and calculates new reading delta costs progressively based on the current slab tier (e.g. 2nd or 3rd slab rate) instead of calculating only at the base 1st slab rate.
- **Live Tariff Slab Badges in Add Reading Screen**: Displays active slab tier, unit rate, and cumulative cycle units in real-time as readings are entered in `AddReadingScreen`.
- **Tariff Slab Tier in Celebration Dialog**: Bento Stats summary in `ReadingSuccessCelebrationDialog` now features the active Tariff Slab alongside Units Used and Estimated Cost.
- **History Screen & Detail Dialog Integration**: Displays slab badges and rates for all chronological readings and fixes meter reset delta handling.
- **Robust Bangladesh LT-A Slabs Fallback**: Ensures all 6 domestic tariff slabs are always populated and seeded, guaranteeing progressive calculation even on fresh or unconfigured installs.

## [1.1.1] - 2026-11-13

### Changed
- **Responsive Meter Reset Card**: Eliminated the fixed 48dp height constraint and multi-line column clipping in `AddReadingScreen`. Redesigned with a flexible row layout ensuring full responsiveness across all phone displays and accessibility font scale settings.
- **Streamlined Typography**: Removed redundant description text (*"Turn on if your meter rolled over..."* / *"মিটার ০ তে ফিরে আসলে বা নতুন মিটার লাগালে অন করুন"*) and conditional badge from the Meter Reset card, resulting in a clean, modern, and uncluttered layout.
- **Seamless Save Reading Button Bar**: Replaced the elevated `Surface` (tonal elevation) and border with a background-matched `Box` in `AddReadingScreen`. Completely eliminated the awkward colored background shade behind the button, providing high contrast and clear visual hierarchy for both enabled and disabled states.
- **Button Localization**: Added dedicated string resources for `btn_save_reading` and `btn_saving` in both English and Bengali.

## [1.1.0] - 2026-11-12

### Added
- **Design System In-App Update Sheet**: Complete WattWise-styled modal bottom sheet for GitHub release updates with dedicated states: Checking, Up to date, Update available (release notes + size), Downloading (progress % with smooth spring animation), Ready to install, and Error with retry.
- **Cryptographic Update Verification**: Integrated SHA-256 asset checksum comparison and download stream verification to guarantee APK integrity before installation.
- **Tariff Cycle Versioning (`tariff_version`)**: Room database migration (`MIGRATION_1_2`) with effective billing month selection (`ApplyFromMonthDialog`), side-by-side diff confirmation dialog, and biometric/PIN authorization.
- **Historical Recalculation Engine**: Recalculates consumption costs and bills for past/present cycles from the selected effective month onwards in a single atomic Room transaction, with progress dialog and one-tap Undo revert snackbar.
- **Unified Themed Controls (`WattWiseControls.kt`)**: Created custom `WattWiseSwitch`, `WattWiseCheckbox`, and `WattWiseRadioButton` adhering to design system tokens with high-contrast OFF borders (>= 3:1), Spring physics animations, and tactile haptic feedback.
- **Material 3 TimePicker Dialog (`WattWiseTimePickerDialog.kt`)**: Replaced legacy system dialog with a custom 28dp rounded dialog featuring clock dial, 12h AM/PM selector, large number digits, keyboard input mode toggle, and Bengali numeral support.
- **Comprehensive Fix Report (`FIX_REPORT.md`)**: Full architectural post-mortem and verification matrix covering all 7 issues.

### Changed
- **Streamlined Settings Layout**: Eliminated heavy paragraph-style text and redundant descriptions across all Settings cards in both English and Bangla.
- **Add Reading Meter Reset Row**: Removed redundant duplicate checkbox; made entire 48dp+ card row clickable with unified `WattWiseSwitch`.
- **Branding Uniformity**: Migrated all legacy `VoltTrack` themes and preview calls to `WattWiseTheme`.
- **Icon Updates**: Migrated `Icons.Outlined.Notes` to `Icons.AutoMirrored.Outlined.Notes`.

### Fixed
- **In-App Updater Flicker**: Eliminated screen and sheet flicker during APK download by keying `AnimatedContent` on phase transitions rather than byte-progress emission objects and throttling progress updates.
- **Tariff Rate Persistence**: Fixed issue where user-modified tariff rates reverted to defaults due to epoch ordering collisions; added persistent `isTariffSeeded` guard and `1970-01` baseline timestamping.
- **Settings DataStore Observation**: Resolved asynchronous DataStore race conditions in automated connected tests via explicit `waitUntil` flow collection.

### Security
- Biometric and device credential authentication required when saving retroactive tariff changes if App Lock is active.
- Verified zero logging of tariff rates, encryption keys, or user data.
