# Agent.md – Build plan and working rules for WattWise

Read these files fully before writing any code, and treat them as the source of truth:
1. PRD.md – product, features, screen-by-screen spec
2. ARCHITECTURE.md – stack, modules, data model, calculation engine
3. DESIGN_SYSTEM.md – colors, typography, shapes, motion, components
4. SECURITY_AND_QUALITY.md – security, accessibility, performance, testing

If two files conflict, ask me one short question instead of guessing.

## Build phases
After each phase, stop, summarize what you built, list how to run and test it, and wait for my "continue".

- Phase 1 – Foundation: Gradle setup, version catalog, module structure, Hilt, theme/design system (colors, typography, shapes, components), navigation shell with bottom bar, Room schema + SQLCipher.
- Phase 2 – Core domain: entities, repositories, all calculation use cases with unit tests (include worked tariff examples).
- Phase 3 – Reading flow: onboarding, add/edit reading, validation, history list with search/filter/swipe actions.
- Phase 4 – Dashboard and Bill: hero card, stats, forecast, budget card, bill breakdown and what-if.
- Phase 5 – Analytics: custom Compose charts, comparisons, heatmap, slab donut, anomalies.
- Phase 6 – Settings, tariff editor, notifications (WorkManager), widget (Glance), app lock and security hardening.
- Phase 7 – Backup/restore, CSV/PDF export, localization (English + Bangla), accessibility pass.
- Phase 8 – Polish: animations, empty states, performance profiling, screenshot tests, release build config, Play Store checklist and listing text.

## Output rules
- Give complete, compilable files (no "..." placeholders), with the file path above each code block.
- Explain each important architectural decision in two lines max.
- Use realistic sample data in previews. Never invent library APIs; if unsure, say so and use the stable alternative.
- If a requirement is ambiguous, make the best assumption, state it in one line, and continue.
- Keep the UI consistent with the tokens in DESIGN_SYSTEM.md. Do not introduce new colors or fonts without telling me.
- Never put secrets, keys or real personal data in code, logs or tests.

## First message to send
"Read all four spec files, confirm in 5 lines what you understood, then start Phase 1."
