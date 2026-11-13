# Changelog

All notable changes to WattWise will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
