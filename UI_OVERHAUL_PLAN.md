# Imani App – UI & UX Overhaul Road-map

_Last updated: 26 May 2025_

This document captures the agreed-upon refactor plan to modernise the Imani App's user interface, surface hidden features, and finish the localisation work.  The work is split into clear phases so we can merge incrementally without breaking the build.

---

## Phase 1 – Foundation & House-keeping  
*(Low risk – gets the codebase ready for the shiny stuff)*

| # | Task | Details |
|---|------|---------|
|1|**Design-system module**| • Define Material 3 `ColorScheme` (dynamic-color aware; falls back to emerald + gold Islamic palette).<br>• Shared typography scale & dims (`Typography.kt`, `Dimens.kt`).<br>• Re-usable atoms: `PrimaryButton`, `PrimaryCard`, `SectionHeader`, etc.|
|2|**Remove placeholder UI**| The mock `PrayerTimesScreen()`, `QiblaScreen()`, `QuranScreen()` inside **`MainActivity.kt`** will be deleted or moved into `PreviewScreens.kt` so only real screens ship.|
|3|**Navigation shell refactor**| • Extract `ImaniScaffold` with `LargeTopAppBar`, animated `NavigationBar`, badges.<br>• Tablet/Desktop → auto-switch to `NavigationRail`.<br>• Deep-link support (`imania://surah/1`, etc.).|

## Phase 2 – Feature-level Facelifts

| # | Screen | Planned Enhancements |
|---|--------|----------------------|
|4|Prayer Times| • Glass-morphic today-card.<br>• Horizontal pager for multiple days.<br>• Animated circular progress for _time until next prayer_.|
|5|Qibla| • Already using new strings; will add subtle haptic feedback and gradient compass needle.<br>• Polished permission sheet with lottie animation.|
|6|Qur'an| • Collapsing toolbar with surah art.<br>• Arabic / translation toggle in-place.<br>• Sticky ayah header during scroll.<br>• Material 3 `SearchBar` with suggestions.|
|7|Du'a Generator (currently hidden)| • New screen: category chips, AI prompt field, favourites tab, disclaimer sheet.<br>• Hooks into existing `DuaViewModel`.|
|8|Mosque Finder| • Map-first layout (Google Maps v3).<br>• Bottom sheet for list & filters.<br>• Cluster markers.<br>• "Add mosque" FAB.|

## Phase 3 – Finishing Touches

| # | Area | Work |
|---|------|------|
|9|Global RTL/LTR| Full pass to verify mirroring & string lengths.|
|10|In-app language picker| `SettingsScreen` → `AppCompatDelegate.setApplicationLocales(...)` (API 33 + fallback).|
|11|Accessibility| TalkBack labels, large-font support, colour-contrast audit.|
|12|Perf & polish| Motion tweaks, haptics, remove any remaining hard-coded strings.

---

## Deliverables per Phase
1. **Compile-green commit(s)** after each numbered task.  
2. Updated screenshots / GIFs in `/docs/screenshots/` demonstrating the change.  
3. If a task touches localisation, corresponding keys must be added to `values/`, `values-sw/`, `values-lg/`, `values-ar/`.

---

## How to follow progress

‣ Each task will be developed in its own short-lived branch and PR.  
‣ PR template will reference the number in this document.  
‣ Once merged, the table boxes above will be ticked ✓.

---

_Questions or new ideas?  Add them as GitHub Issues referencing this file so we keep the scope under control._ 