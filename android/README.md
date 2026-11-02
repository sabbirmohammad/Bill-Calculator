# Bangladesh Electricity Meter Reading & Bill Calculator
### Production-Quality Native Android Application

A complete, offline-first native Android application engineered specifically for tracking electricity consumption and calculating electricity bills using traditional mechanical analog electricity meters commonly deployed across Bangladesh.

---

## ⚡ Core Highlights & Capabilities

* **Fixed Meter Configuration for Mechanical Counters:**
  * Configurable black whole-unit wheels (whole kWh) + exactly 1 red decimal wheel (0.1 kWh resolution).
  * Reconstructed formula: $\text{Reading} = \text{Black Digits} + \frac{\text{Red Digit}}{10}$.
  * Preservation of leading zeros for physical counter display (e.g., `00145.2 kWh`).
  * Full support for Bangla (`০-৯`) and English (`0-9`) numeric inputs.

* **Strict Financial & Engineering Precision:**
  * $100\%$ pure `BigDecimal` arithmetic across all reading, consumption, and financial operations. Zero binary floating-point (`Double` / `Float`) drift.
  * Intermediate consumption precision at 4 decimal places; monetary balances rounded with `RoundingMode.HALF_UP` to 2 decimal places in BDT (৳).

* **Advanced Progressive Tariff Engine:**
  * No hardcoded rates. Supports flat-rate and user-defined progressive block-based consumption slabs with effective date versioning.
  * **Differential Daily Cost Rule:** Correctly computes daily/interval energy costs under progressive slabs as $\Delta \text{Cost} = \text{Bill}_{\text{energy}}(C_{\text{cumulative\_new}}) - \text{Bill}_{\text{energy}}(C_{\text{cumulative\_prev}})$, ensuring accurate costs when consumption crosses slab boundaries.
  * Configurable additional charges: Demand Charge, Meter Rent, Service Charges, VAT % with customizable tax base.

* **Billing Period Lifecycle & Historical Integrity:**
  * Independent monthly billing cycles without resetting the physical meter reading.
  * Missing day interval tracking without fabricating daily averages as actual readings.
  * Boundary completeness flags: `PARTIAL_TRACKING`, `INCOMPLETE_BOUNDARY_DATA`, `COMPLETED`.
  * Cascading recalculation when historical readings are edited or removed.
  * Physical counter rollover and meter replacement workflows.

* **Offline-First Storage & Reminders:**
  * Local Room Database with type converters storing exact numerical strings (`VARCHAR`) to avoid SQLite IEEE 754 precision issues.
  * JSON and CSV full offline backup export and transactional restore.
  * Daily local reading reminders scheduled via WorkManager without needing internet or cloud accounts.

---

## 🏗️ Architecture & Technology Stack

| Layer | Technologies & Standards |
| :--- | :--- |
| **Language** | Kotlin 2.0.21 (JVM 17) |
| **UI Framework** | Jetpack Compose + Material Design 3 |
| **Architecture** | Clean Architecture (Presentation, Domain, Data) + MVVM |
| **Database** | Room 2.6.1 with SQLite transactions |
| **Dependency Injection** | Dagger Hilt 2.51.1 |
| **Asynchronous & Streams** | Kotlin Coroutines & Flow (StateFlow / SharedFlow) |
| **Preferences** | AndroidX DataStore Preferences |
| **Background Work** | AndroidX WorkManager (Local daily reminder) |
| **Date & Time** | `java.time` in `Asia/Dhaka` timezone |
| **Arithmetic** | Java `BigDecimal` |
| **Build System** | Gradle 8.7 + Version Catalog (`libs.versions.toml`) |

---

## 📁 Project Structure

```text
android/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/bangla/electricitybill/
│       │   │   ├── ElectricityBillApp.kt             // Hilt Application + WorkManager Config
│       │   │   ├── MainActivity.kt                  // Single Activity host with Compose
│       │   │   ├── core/util/                       // BanglaDigitNormalizer, BigDecimalMath, DateUtils
│       │   │   ├── di/                              // Hilt Database & Repository Modules
│       │   │   ├── domain/
│       │   │   │   ├── model/                       // Meter, Reading, Tariff, BillingPeriod, ItemizedBill
│       │   │   │   ├── engine/                      // Parser, Validator, ConsumptionCalc, TariffEngine, Forecast
│       │   │   │   ├── repository/                  // Repository interfaces
│       │   │   │   └── usecase/                     // SaveReading, RecalculateHistory, ReplaceMeter
│       │   │   ├── data/
│       │   │   │   ├── entity/                      // Room Entities (8 tables)
│       │   │   │   ├── dao/                         // DAOs with Flow and Transaction support
│       │   │   │   ├── database/                    // AppDatabase, Converters
│       │   │   │   ├── repository/                  // Repository implementations & Entity mappers
│       │   │   │   ├── backup/                      // BackupManager, BackupGson, CSV Export
│       │   │   │   ├── datastore/                   // PreferencesManager
│       │   │   │   └── worker/                      // DailyReadingReminderWorker
│       │   │   └── presentation/
│       │   │       ├── navigation/                  // AppNavigation, BottomNav, Destinations
│       │   │       ├── theme/                       // Color, Typography, Theme (Light/Dark)
│       │   │       ├── components/                  // MechanicalMeterDisplay, SlabBreakdownCard, TrendChart
│       │   │       ├── onboarding/                  // 4-step interactive setup wizard
│       │   │       ├── dashboard/                   // Meter status, interval metrics, slab summary, forecast
│       │   │       ├── reading/                     // Add reading with live consumption preview & rollover dialog
│       │   │       ├── history/                     // Chronological list, gap alerts, delete with recalculation
│       │   │       ├── reports/                     // Official monthly bill statement with share action
│       │   │       ├── analytics/                   // Trend bars, expenditure metrics, CSV/JSON export
│       │   │       └── settings/                    // Language, theme, reminder time, meter replacement
│       │   └── res/                                 // Vector icons, English & Bangla strings
│       └── test/                                    // Unit tests covering all 25 test cases
├── gradle/
│   ├── libs.versions.toml                           // Gradle Version Catalog
│   └── wrapper/                                     // Gradle 8.7 wrapper
├── build.gradle.kts
├── settings.gradle.kts
└── local.properties
```

---

## 🧪 Verification & Test Suite

The test suite covers all 25 specified acceptance test cases:

```bash
cd android
./gradlew test
```

### Test Case Coverage Matrix

* **Reading Tests (T1–T7):**
  * `test01_normalConsumptionCalculation`: $12345.6 \rightarrow 12348.2 = 2.6\text{ kWh}$.
  * `test02_zeroConsumptionIsValid`: $12345.6 \rightarrow 12345.6 = 0.0\text{ kWh}$.
  * `test03_lowerReadingFailsWithoutRollover`: $12345.6 \rightarrow 12340.2$ rejected without rollover confirmation.
  * `test04_emptyRedDigitFails`: Rejects empty red decimal wheel input.
  * `test05_multipleRedDigitsFails`: Enforces strict single red digit resolution.
  * `test06_banglaDigitNormalization`: Normalizes `১২৩৪৮.২` to `12348.2` `BigDecimal`.
  * `test07_preserveLeadingZeros`: Preserves zero-padded counter string `00145.2`.

* **Missing Reading & Interval Tests (T8):**
  * `test08_missingReadingsIntervalCalculation`: Nov 2 ($12345.6$) to Nov 5 ($12348.2$) correctly computes $2.6\text{ kWh}$, $3\text{ days}$, avg $0.87\text{ kWh/day}$, flagged as an interval.

* **Monthly Billing Period Tests (T9–T13):**
  * `test09_billingPeriodIndependence`: Each month operates under an independent accounting cycle.
  * `test10_physicalMeterNotResetAtMonthEnd`: Physical meter readings accumulate continuously.
  * `test11_previousMonthsAccessible`: Prior months remain readable with historical rates.
  * `test12_missingMonthBoundaryReadingsIdentified`: Flags missing boundary readings as `INCOMPLETE_BOUNDARY_DATA`.
  * `test13_midMonthInitialTrackingMarkedAsPartial`: Mid-month setups flagged as `PARTIAL_TRACKING`.

* **Tariff Engine Tests (T14–T18):**
  * `test14_flatRateCalculation`: $250\text{ kWh} \times \text{৳}8.00 = \text{৳}2,000.00$.
  * `test15_progressiveSlabCalculation`: Multi-tier slab calculation correctly split across boundaries ($205\text{ units} \rightarrow \text{৳}1,332.45$).
  * `test16_differentialDailyEnergyCost`: Daily slab cost calculated via cumulative difference: $\text{Bill}(205) - \text{Bill}(190) = \text{৳}109.95$.
  * `test17_fixedMonthlyChargesAppliedOnce`: Fixed monthly charges (demand, meter rent) applied once per month and excluded from daily intervals.
  * `test18_vatCalculationWithConfiguredBase`: VAT computed on Energy Cost + applicable base charges.

* **Data Integrity & Recalculation Tests (T19–T25):**
  * `test19_historicalTariffVersionsPreserved`: Previous bills retain original tariff version rates.
  * `test20_editingHistoricalReadingRecalculatesIntervals`: Cascade recalculation automatically updates affected intervals and month-end summaries.
  * `test21_meterReplacementPreservesIndependentReadings`: Meter replacement starts new baseline without cross-meter negative differences.
  * `test22_counterRolloverCalculation`: Counter rollover formula $((10^N - V_{\text{prev}}) + V_{\text{curr}})$ correctly yields $2.5\text{ kWh}$.
  * `test24_jsonBackupPayloadSerializationAndDeserialization`: JSON backup payload correctly serializes/deserializes all 8 tables and `BigDecimal`/`LocalDateTime` fields.
  * `test25_exactDecimalPrecision`: Strict `BigDecimal` prevents binary floating-point drift.

---

## 🚀 Building & Generating the APK

To compile and assemble the signed debug APK:

```bash
cd android
./gradlew assembleDebug
```

Output APK location:
```text
android/app/build/outputs/apk/debug/app-debug.apk
```
