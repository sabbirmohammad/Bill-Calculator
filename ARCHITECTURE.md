# ARCHITECTURE – WattWise

## Tech stack (use current stable versions; Gradle Kotlin DSL + version catalog)
- Language: Kotlin 2.x, coroutines + Flow, kotlinx-serialization, kotlinx-datetime
- UI: Jetpack Compose + Material 3 (Material You), type-safe Compose Navigation, Compose animation APIs
- Architecture: Clean Architecture + MVVM with unidirectional data flow (UiState / UiEvent / UiEffect per screen)
- DI: Hilt
- Local DB: Room (with migrations and exported schemas) + SQLCipher for encryption at rest
- Preferences: Jetpack DataStore (encrypted values for sensitive items)
- Background: WorkManager (daily reminder, budget checks, auto-backup)
- Charts: custom Compose Canvas charts (preferred) or Vico; no heavy legacy chart libraries
- Images: Coil; CameraX + ML Kit text recognition (optional on-device OCR of the meter display)
- Widgets: Jetpack Glance
- Testing: JUnit5, MockK, Turbine, Room in-memory tests, Compose UI tests, Paparazzi or Roborazzi
- Min SDK 26, target SDK = latest stable, edge-to-edge, predictive back gesture

## Module / package layout
```
app/                    Application, MainActivity, NavHost, DI wiring
core/designsystem/      theme, colors, typography, shapes, reusable components
core/common/            Result wrappers, dispatchers, date/number utils, formatters
core/database/          Room DB, DAOs, entities, migrations, encryption
core/datastore/         settings storage
feature/onboarding/
feature/dashboard/
feature/addreading/
feature/analytics/
feature/history/
feature/bill/
feature/budget/
feature/settings/
feature/backup/
domain/                 models, repository interfaces, use cases (pure Kotlin, no Android imports)
data/                   repository implementations, mappers
```

## Rules
- The domain layer has zero Android dependencies and 100% unit-test coverage for the calculation use cases.
- Every screen exposes a single immutable UiState via StateFlow; side effects (snackbar, navigation) go through a Channel/SharedFlow.
- Repositories return Flow for observable data. Never block the main thread.
- Money is stored as Long minor units (poisha) or BigDecimal, never Float or Double. Readings are BigDecimal with 1 decimal place.
- All user-visible text lives in strings.xml with English (default) and Bangla (values-bn). No hard-coded strings.
- Use sealed interfaces for errors; show friendly, actionable messages.

## Database entities (Room)
- MeterEntity(id, name, type[POSTPAID|PREPAID], startReading, multiplierFactor, isActive, createdAt)
- ReadingEntity(id, meterId, readingValue, recordedAt[date+time], note, photoUri?, isEstimated, isMeterReset, createdAt, updatedAt)
- TariffEntity(id, name, effectiveFrom, vatPercent, serviceChargeFlat, demandChargeFlat, isActive)
- TariffSlabEntity(id, tariffId, fromUnit, toUnit?, ratePerUnit)
- Billing cycle setting: cycleStartDay (1..28)
- BudgetEntity(id, cycleStartDate, unitLimit?, amountLimit?)
- RechargeEntity (prepaid): id, date, amountPaid, unitsReceived, note
- Index on (meterId, recordedAt). Unique constraint: one reading per meter per timestamp.

## Validation rules
- A new reading must be >= the previous reading unless flagged as a meter reset. If lower, show: "This is lower than your last reading (X). Did the meter reset or get replaced?"
- Warn (do not block) when one day's consumption is more than 3x the 30-day average (possible typo).
- No future dates. Allow back-dating; recompute all derived values automatically after edits or deletes.

## Calculation use cases (pure Kotlin, fully unit-tested)
1. CalculateDailyUsage: per-day units, with gap spreading and the isEstimated flag.
2. CalculateCycleUsage(cycleStart, today): total units so far, daily average, days left.
3. CalculateBill(units, tariff): progressive slab billing.
   - energyCharge = sum over slabs of (units falling in slab x slab rate)
   - subtotal = energyCharge + demandCharge + serviceCharge
   - vat = subtotal x vatPercent / 100
   - total = subtotal + vat
   - Return a full breakdown (each slab, units, rate, amount, plus demand, service, VAT, total) so the UI can show a transparent receipt.
4. ForecastMonthEnd: projected units = usedSoFar + (weighted average of last 7 days x 0.6 + cycle average x 0.4) x daysLeft. Return a low / expected / high range.
5. CompareWithPrevious: percent change vs previous day, same weekday last week, previous cycle, same month last year.
6. DetectAnomalies: flag days where usage > mean + 2 standard deviations over the last 30 days.
7. CostPerDay, CostPerUnitEffective, and a CO2 estimate (kg = kWh x configurable emission factor, default 0.55, editable).
8. Prepaid mode: remaining balance, estimated days remaining, and a "recharge soon" alert.

## Tariff defaults
Ship an editable residential slab tariff template for Bangladesh with placeholder rates and a clear note: "Verify rates with your utility; rates change." Never hard-code rates in logic; always read them from TariffEntity so the user can edit or add a new tariff with an effective date. Bills for older cycles keep using the tariff that applied then.
