# PomoloBee - App Test Checklist
---

  

## Installation Behavior

| Test | Expected Outcome | Pass? |
|------|------------------|-------|
| App requests permissions (CAMERA, READ/WRITE) | System permission dialog shown | ☐ |
| App copies initial assets to `/sdcard/PomoloBee/` on first run | Files exist after install: <br> `config/fruits.json`, `fields/svg/C1_map.svg`, etc. | ☐ |
| `OrchardRepository.loadAllConfig()` loads local JSON into cache | No crash, log success | ☐ |

---

## Start Navigation

| Test | Expected Outcome | Pass? |
|------|------------------|-------|
| App launches without crash | MainActivity starts and UI visible | ☐ |
| First screen is **CameraScreen** | As per `startDestination = Screen.Camera.route` in `NavGraph` | ☐ |
| Top bar + drawer icon are visible | Drawer opens on click | ☐ |
| Menu shows correct options (see spec) | `Camera`, `Settings`, `Orchard`, `Location`, `About`, etc. | ☐ |

---

## Drawer Menu Navigation

| Test | Navigate To | Expected Result | Pass? |
|------|-------------|------------------|-------|
| Tap `Camera` | CameraScreen | Camera UI opens | ☐ |
| Tap `Orchard` | OrchardScreen | Field list displayed | ☐ |
| Tap `Location` | LocationScreen | Field/Row dropdowns present | ☐ |
| Tap `SvgMap` via Location → Select from map | SvgMapScreen | Shows correct SVG | ☐ |
| Tap `Settings` | SettingsScreen | Inputs visible | ☐ |
| Tap `About` | AboutScreen | App version shown | ☐ |
| Back button works between screens | Navigates or exits correctly | ☐ |

---

## Orchard SVG Map Interaction

| Test | Expected Outcome | Pass? |
|------|------------------|-------|
| Orchard screen lists fields (from `OrchardCache`) | Fields from `locations.json` shown | ☐ |
| Selecting field C1 → opens SvgMap with C1_map.svg | Map renders correctly | ☐ |
| Selecting field C2 (not in map) → default_map.svg shown | Graceful fallback | ☐ |

---

## Camera to Location Flow

| Test | Expected Outcome | Pass? |
|------|------------------|-------|
| From Camera screen, tap to go to Location screen | Navigation works | ☐ |
| Field dropdown shows available fields | From cache | ☐ |
| Tap “Select from map” → opens SvgMapScreen | Field’s SVG shown | ☐ |
| Tap a row in SVG → returns to Location screen | Combo updates with selected row | ☐ |
| Tap "Validate" in Location → returns to Camera | Selected field & row shown | ☐ |

---

## ️ Settings Persistence

| Test | Expected Outcome | Pass? |
|------|------------------|-------|
| Go to Settings and update values (e.g., API URL, sync mode) | Inputs accept changes | ☐ |
| Tap "Validate" → changes saved | No crash or loss | ☐ |
| Close app, restart | Settings are persisted (loaded from `UserPreferences`) | ☐ |

---

## ℹ️ About Screen

| Test | Expected Outcome | Pass? |
|------|------------------|-------|
| About screen accessible from menu | Opens normally | ☐ |
| App version (e.g., v0.1.0) displayed | Static or from `BuildConfig.VERSION_NAME` | ☐ |

---

## Advanced / Bonus Tests

| Test | Outcome | Pass? |
|------|---------|-------|
| Run `ConnectionRepository.syncOrchard()` manually (or via UI) | Log success, cache updated | ☐ |
| Logs show OpenCV library loaded | No native lib error | ☐ |
| Unknown field ID in SvgMap → Shows "Field not found" text | Graceful error | ☐ |

---

## Notes

- Cloud sync & API calls are not implemented yet.
- Image history/results are stubbed.
- Test only local mode (`/sdcard/`) for now.

---
 