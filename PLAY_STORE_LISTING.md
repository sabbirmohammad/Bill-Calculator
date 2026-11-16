# WattWise – Google Play Store Listing & Launch Checklist

---

## 1. Store Listing Metadata

### App Title (30 characters max)
**WattWise: Electricity Tracker** *(29 characters)*

*(Alternative: WattWise - বিদ্যুৎ বিল ও মিটার)*

### Short Description (80 characters max)
**Track daily meter readings, calculate slab bills & save electricity in Bangladesh.** *(80 characters)*

*(Bangla alternative: মিটার রিডিং ট্র্যাক করুন, ধাপে ধাপে বিদ্যুৎ বিল হিসাব করুন এবং খরচ কমান।)*

---

## 2. Full Description (4,000 characters max)

WattWise is an offline-first, privacy-focused electricity usage tracker and bill calculator tailored specifically for households and businesses in Bangladesh.

Tired of surprise month-end electricity bills? Take control of your energy consumption by recording your physical meter reading once a day. WattWise calculates your progressive slab billing in real-time according to Bangladesh Energy Regulatory Commission (BERC) tariffs, forecasts your month-end costs, alerts you before you cross into expensive slabs, and gives you actionable insights to stay within budget.

### 🇧🇩 Tailored for Bangladesh Utilities
Fully compatible with both postpaid and prepaid meters across all Bangladeshi distribution utilities:
• DESCO (Dhaka Electric Supply Company)
• DPDC (Dhaka Power Distribution Company)
• BPDB (Bangladesh Power Development Board)
• NESCO (Northern Electricity Supply Company)
• WZPDCL (West Zone Power Distribution Company)
• BREB (Bangladesh Rural Electrification Board / Palli Bidyut)

---

### ⚡ Key Features

**1. Quick Daily Reading Entry**
• Record your meter reading in seconds with a high-contrast numeric keypad.
• Instantly see units (kWh) consumed since your last reading.
• Smart handling for missed days (linear gap spreading with exact energy conservation), meter resets, and replacements.

**2. Exact Progressive Slab Bill Calculation**
• Computes multi-tiered progressive tariffs with official life-line slabs (e.g., 0–75, 76–200, 201–300, 301–400, 401–600, 600+ kWh).
• Itemized receipt breakdown: Energy charge per slab, Demand charges, Service charges, and exact 5% VAT.
• Custom cycle start day (1st to 28th) matching your official billing cycle.
• Versioned tariff editor: update BERC rates anytime without waiting for app updates.

**3. Month-End Forecasting & Budgeting**
• Adaptive forecasting blends your recent 7-day usage with cycle averages.
• Realistic confidence bands (low, expected, high range) and landing slab predictions.
• Monthly BDT & unit budget targets with proactive alerts at 70%, 90%, and 100%.

**4. Prepaid Meter Mode**
• Track token recharges and monitor remaining balance in Taka (৳).
• Accurate burn rate and countdown of remaining days and hours.
• Anticipates 1st-of-the-month recurring utility deductions (demand charges, meter rent, VAT) to prevent unexpected disconnections.

**5. Comprehensive Analytics & Charts**
• Custom Canvas charts: daily, weekly, monthly, and yearly consumption trends.
• Interactive cost overlay and accessible tabular data view.
• Weekday usage heatmap to identify peak load days.
• Slab donut visualization showing where your electricity money actually goes.
• Anomaly detection (Median + MAD) flagging abnormal spikes without outlier distortion.

**6. Complete Privacy & Offline Security**
• 100% Offline: No internet connection required. No account creation, no sign-in.
• Zero data collection: Your usage data never leaves your device.
• 256-bit AES SQLCipher hardware-backed database encryption at rest.
• Encrypted local backups secured with OWASP-compliant PBKDF2 (600,000 iterations) + AES-256-GCM.
• Biometric App Lock (Fingerprint / Face Unlock / PIN) and Screenshot Protection.

**7. Rich Extras & Localization**
• Full bilingual support: English & বাংলা (Bangla) with Bengali numerals (১২৩৪৫৬৭৮৯০).
• One-tap CSV export and formal PDF billing statement generation.
• Android Glance Home Screen Widget for at-a-glance consumption.
• Exact daily reminder notification so you never forget to log your reading.

---

## 3. Categorization & Tags

• **Category:** Tools / Finance / Productivity
• **Content Rating:** PEGI 3 / Everyone
• **Tags:** Electricity Bill, Meter Reader, Bangladesh Electricity, DESCO, DPDC, BPDB, NESCO, WZPDCL, BREB, Palli Bidyut, BERC Tariff, Prepaid Meter, Energy Tracker, বিদ্যুৎ বিল, মিটার রিডিং.

---

## 4. Privacy & Data Safety Declarations

### Data Collection & Sharing
• **Does your app collect user data?** No.
• **Does your app share user data with third parties?** No.
• **Data Storage:** All data resides exclusively on the user's local device storage.
• **Data Encryption:** Local database encrypted using 256-bit AES via SQLCipher.
• **Account Deletion / Wipe:** 2-step authenticated "Clear All Data" directly in Settings.

### Runtime Permissions Justification
1. `POST_NOTIFICATIONS`: Optional. Required solely to deliver scheduled daily reading reminders and budget threshold warnings.
2. `SCHEDULE_EXACT_ALARM`: Optional. Used to wake the device at the user-specified reminder time (e.g., 9:00 PM).
3. `CAMERA`: Optional. Only used when user chooses to photograph physical meter display for record-keeping.
4. `RECEIVE_BOOT_COMPLETED`: Used to restore scheduled reminder alarms after device restart.

---

## 5. Pre-Launch Release Checklist

- [x] **Target SDK:** Android 15 (API 35), Compile SDK: 36
- [x] **Min SDK:** Android 8.0 (API 26)
- [x] **64-bit Architecture:** libsqlcipher.so built for `arm64-v8a`, `armeabi-v7a`, `x86_64`
- [x] **ProGuard / R8 Minification:** `isMinifyEnabled = true`, `isShrinkResources = true` verified
- [x] **Security Invariants:** Zero hardcoded keys, zero sensitive data logged in production logcat
- [x] **Localization:** 100% string coverage in English (`values`) and Bangla (`values-bn`)
- [x] **Accessibility:** WCAG 2.2 AA compliant, 48dp+ touch targets, 200% font scaling resilience
- [x] **Edge Case Verification:** Slab boundaries, leap years, dial rollover, mid-cycle tariff changes
- [x] **Release APK / AAB Build:** Verified compilable and shrinkable
