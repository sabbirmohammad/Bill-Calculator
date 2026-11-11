# SECURITY AND QUALITY – WattWise

## Privacy-first principles
- Offline-first, no account, no tracking, no ads, no third-party analytics.
- Request only the permissions needed: POST_NOTIFICATIONS (runtime) and CAMERA (only when the user taps scan/photo). No INTERNET permission unless the user enables Google Drive backup.
- A plain-language privacy screen explains what is stored and that it never leaves the device unless the user exports or backs up.

## Data protection
- Encrypt the Room database with SQLCipher. Generate a random 256-bit passphrase, wrap it with an Android Keystore key (AES-GCM, hardware-backed / StrongBox when available), and store only the wrapped value. Never hard-code keys.
- Encrypt backup files with AES-256-GCM, key derived from a user password (Argon2id, or PBKDF2-HMAC-SHA256 at 310k+ iterations), random salt and IV, authenticated format with a version header. Verify integrity on restore and fail safely.
- Store meter photos in app-private storage (not the public gallery); strip EXIF location data.
- Set android:allowBackup="false" and provide explicit data-extraction rules so device transfer does not leak the plain database.
- Exclude sensitive screens from the recents thumbnail and optionally block screenshots (FLAG_SECURE) via a setting.

## App access
- Optional app lock: BiometricPrompt (strong biometrics) with device-credential fallback; configurable auto-lock timeout; lock when the app goes to background. Rate-limit PIN attempts with exponential backoff.
- "Hide amounts" privacy mode that masks money values on the dashboard and widget.

## Code and build hardening
- Enable R8 minification and obfuscation for release; keep rules minimal and tested.
- Sign with Play App Signing; no secrets in the repo; use local.properties / CI secrets.
- Use Play Integrity API only if Drive backup or another network feature is added.
- No exported components unless required (android:exported="false" for all others); explicit PendingIntents with FLAG_IMMUTABLE.
- Validate and sanitize all imported CSV/backup input (size limits, type checks, no path traversal). Parameterized Room queries only.
- If any network call exists (Drive backup): TLS only (network security config, cleartext disabled), OAuth with minimal scope (drive.appdata), never log tokens.
- Logging: no sensitive data in logs; strip logs in release (Timber no-op tree).
- Dependency hygiene: pin versions, enable Dependabot or similar, run lint, detekt and ktlint in CI. Aim to follow OWASP MASVS (L1, with selected L2 controls).

## Accessibility
- TalkBack labels and roles on every control; charts expose a text summary and "view as table".
- Touch targets at least 48dp; font scale up to 200% without clipping; never rely on color alone; full keyboard and switch-access navigation.

## Performance
- Cold start under 1 second on a mid-range phone; 60fps scrolling with 5+ years of daily data (about 2,000 rows): paging, stable keys, remember/derivedStateOf, avoid recomposition storms. Add Baseline Profiles and a Macrobenchmark module.
- Heavy calculations run on Dispatchers.Default and are cached per cycle; recompute incrementally when a reading changes.

## Edge cases to handle and test
Missed days, back-dated entries, edited or deleted readings in the middle of history, meter reset/replacement, two readings on the same day, timezone or DST/clock changes, month boundaries and custom cycle start days, leap years, tariff changes mid-cycle, meter rollover (e.g. 99999.9 -> 0.0), process death during entry, rotation and split-screen, and a device date set incorrectly.

## Testing targets
- Unit tests: all use cases, tariff slab math (include worked examples), forecasting, anomaly detection.
- Database tests: every migration, constraints, queries.
- UI tests: add-reading flow, validation dialogs, navigation, theme switch, locale switch.
- Screenshot tests for every screen in light, dark, large font and Bangla.
- CI: GitHub Actions running build, lint, detekt, unit and instrumented tests.

## Code quality
Idiomatic Kotlin, no `!!`, KDoc on public domain APIs, small composables (under about 80 lines), previews with realistic fake data, and a seed-data generator for debug builds (12 months of sample readings).
