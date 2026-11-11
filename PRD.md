# PRD – WattWise (Electricity Usage Tracker)

## Role
You are a principal Android engineer, senior product designer (UI/UX) and security-minded architect working as one team. Build a production-quality, beautiful, modern Android app in Kotlin called "WattWise" (working name) that tracks household electricity usage from manual daily meter readings and calculates consumption and bills.

## Product goal
The user reads their physical electricity meter once a day and types the reading into the app. The app instantly shows units used today, this week and this month, the estimated bill so far, the projected month-end bill, trends, and alerts when usage or budget is going out of control.

## Target user
A household in Bangladesh (Asia/Dhaka timezone, currency BDT ৳, English + Bangla UI) with a postpaid or prepaid kWh meter. The app works 100% offline. No account is required.

## Core features
1. Quick daily reading entry: one tap from the dashboard, big numeric input, date defaults to today, optional time, note and meter photo.
2. Automatic consumption calculation: units used = current reading minus previous reading. Missed days are filled by spreading consumption evenly across the gap and marking those days "estimated".
3. Meter reset or replacement handling (user marks a reading as "new meter" with a new starting value).
4. Billing cycle support: user chooses the cycle start day (for example the 1st or the 15th) so "this month" matches the real bill.
5. Bill calculator using a fully configurable tariff (slabs, demand charge, service charge, VAT percent, prepaid recharge mode).
6. Budget and alerts: monthly unit limit and monthly BDT budget, with warnings at 70%, 90% and 100%.
7. Analytics: daily, weekly, monthly and yearly views, comparisons, averages, peaks, and a month-end forecast.
8. Full reading history with search, filter, edit, delete and undo.
9. Backup and restore (local file and optional Google Drive app-data folder), CSV and PDF export.
10. Home screen widget and a daily reminder notification ("Time to record your meter reading").

## Non-goals (do not build)
User accounts, ads, analytics SDKs, and any network call that sends reading data to a server.

## Screen-by-screen UI/UX spec
Build every screen with loading, empty, error and populated states, in light and dark mode, with Compose previews. Follow DESIGN_SYSTEM.md for all visuals.

### 1. Splash + Onboarding (3 swipe cards + setup)
Cards: "Read your meter", "See your usage", "Stay under budget". Setup steps: meter type (postpaid/prepaid), current meter reading, billing cycle start day, tariff template, monthly budget (optional), reminder time (default 9:00 PM), language and theme. Skippable; everything is editable later in Settings.

### 2. Dashboard (Home)
- Top: greeting, date, meter switcher chip (if multiple meters).
- GradientHeroCard: big "This month" units (count-up), estimated bill so far in ৳, a UsageRing showing % of budget/limit, and a line such as "On track: projected ৳2,140 by month end".
- Quick stats row (horizontally scrollable): Today, Yesterday, 7-day average, Cycle average, each with a TrendChip.
- "Record today's reading" prominent card (shows last reading and time since; becomes a green "Done for today" state after saving).
- Mini bar chart of the last 14 days; tap a bar to see the day. Highlight today and anomalies.
- Forecast card: expected units/bill with low-high range and days left.
- Insights carousel: 2-3 auto-generated tips ("Weekends use 22% more than weekdays", "Your peak day was Friday, 18.2 units").
- Budget status card with progress and next alert threshold.
- Recent readings (last 3) with "See all".

### 3. Add / Edit Reading (bottom sheet or full screen)
- Large numeric field with custom number pad and decimal point; shows the previous reading above and the live difference below ("+7.4 units since yesterday").
- Date and time chips (default now), note field, optional photo (camera/gallery), optional "Scan meter" button (on-device OCR; user confirms the detected value).
- Inline validation, Save disabled until valid, confirm dialog for suspicious values, success animation, undo snackbar.

### 4. Analytics
- Segmented tabs: Day | Week | Month | Year, plus a date-range picker.
- Hero chart: usage (bars) with cost (line overlay), tap/drag tooltip, pinch to zoom, average line.
- Summary tiles: total units, total cost, daily average, peak day, lowest day.
- Comparison card: this period vs previous period vs same period last year, with percent deltas.
- Weekday pattern heatmap (7 columns) and a cumulative-vs-budget line.
- Cost breakdown donut by slab (shows how much cost comes from the expensive slabs).
- Forecast and anomaly list. Share chart as an image.

### 5. History
- Grouped by month with sticky headers and monthly totals.
- Each row: date, reading, units used (+ trend chip), cost, tags for Estimated / Meter reset / Photo / Note.
- Search, filter (date range, estimated only, with photo, high usage), sort. Swipe left to delete (with undo), swipe right to edit, long-press for multi-select.
- Row tap opens a detail sheet with photo, full breakdown and edit history.
- Export CSV / PDF from the top bar.

### 6. Bill (cycle bill calculator)
- Receipt-style breakdown: each slab, units, rate, amount; demand charge, service charge, VAT, total.
- "What-if" slider: change expected units and see the bill update live.
- Past cycles list with a paid/unpaid toggle and the actual bill amount the user entered (to compare estimate vs actual and refine accuracy).
- Prepaid mode: recharge log, balance and days remaining.

### 7. Budget and Alerts
Set monthly unit limit and BDT budget, choose alert thresholds, quiet hours and notification channels. Show a history of how often budgets were met.

### 8. Settings
Sections:
- Profile and Meters: add/edit/archive meters.
- Billing: cycle day, tariff editor with slabs, VAT, charges, effective dates.
- Notifications: reminder time, days, budget alerts, weekly summary.
- Appearance: theme, dynamic color, language (English/Bangla), number format including Bangla digits, units kWh.
- Security: app lock, biometric, hide amounts, screenshot block.
- Data: backup/restore, auto-backup frequency, export, import CSV, clear all data (typed confirmation).
- About: version, privacy statement, licenses, feedback. Plus a Help/FAQ screen on how to read different meter types.

### 9. Extra screens
- Tariff editor (slabs table with add/remove rows and a live preview calculation).
- Backup and restore wizard.
- Achievements/streaks (reading streak, "under budget" months), subtle and optional.
- Home screen widget (small: today's usage; medium: cycle usage + bill + quick add button).
- Lock screen (biometric/PIN) and a friendly empty state when no data exists.
