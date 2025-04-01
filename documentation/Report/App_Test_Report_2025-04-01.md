# PomoloBee - App Test Report

## ️ Test Session
- **Date:** 2025-04-01
- **Tester:** nathabee
- **Device:** *(e.g., Pixel 5, Samsung A52, Emulator API 33)*
- **Android Version:** *(e.g., 13.0)*
- **App Version:** *(e.g., v0.1.0 - debug)*
- **Build Variant:** *(e.g., debug/release)*

---

## Test Summary

| Test Description | [ ] Pass | [ ] Partial | [ ] Fail | Notes |
|------------------|----------|-------------|----------|-------|
| App installs without crash | | | | App launches, no permission crash, no black screen |
| Permissions requested on first run | | | | Camera + storage permissions |
| Assets copied to `/sdcard/PomoloBee/` | | | | Check via file explorer or `adb shell` |
| Splash screen shown (if any) | | | | Optional if implemented |
| First screen is CameraScreen | | | | Check against `NavGraph.kt` |
| Drawer menu opens from top-left | | | | Menu icon visible and works |
| Menu contains all expected items | | | | Based on App_Spec |
| Navigation to each screen works | | | | Camera, Orchard, Location, Settings, About |
| Back button returns correctly | | | | No crashes, follows nav stack |
| OrchardScreen shows fields | | | | Fields from `locations.json` |
| SVG map loads for field C1 | | | | Shows `C1_map.svg` correctly |
| Fallback SVG loads for unknown field | | | | e.g., C2 → `default_map.svg` |
| From Camera → go to Location screen | | | | Navigation works |
| Dropdown shows available fields/rows | | | | Based on cached data |
| "Select from map" opens SVG | | | | Opens correct field SVG |
| Selecting row updates Location screen | | | | Row shown in combo box |
| "Validate" returns to Camera with selected data | | | | State retained |
| Settings screen opens | | | | All inputs present |
| Settings changes persist after restart | | | | Check using `UserPreferences` |
| About screen accessible | | | | App info and version visible |
| Version is correct (e.g., v0.1.0) | | | | From `BuildConfig.VERSION_NAME` |

---

## Issues/Bugs Found

### 1. ️ *Short title here e.g. "Location screen row not updating"*
- **Expected:** Dropdown should update after SVG selection
- **Observed:** *(What actually happened?)*
- **Steps to Reproduce:**
  1. Open Location screen
  2. Tap "Select from map"
  3. Select a row
- **Suspected Cause:** *(e.g., ViewModel not storing row ID)*
- **Priority:** [ ] Low [ ] Medium [ ] High
- **Screenshot/Log:** *(optional)*

---

### 2. ️ *Another bug if any*

...

---

## General Notes

- Were there any crashes or unexpected behaviors?
- Was the UI responsive and clear?
- Any performance or rendering delays?

💡 *Use this space to record overall feedback during testing.*

---

## Screenshots / Logs

*Include screenshots, Logcat snippets, or `adb shell` results here (optional).*

---

✅ **Tip:** Save each test report with a date, like `App_Test_Report_2025-04-01.md`

---

