# PomoloBee - App Test Checklist
---

<details>
<summary>Table of Content</summary>

<!-- TOC -->
- [PomoloBee - App Test Checklist](#pomolobee-app-test-checklist)
  - [Build Deploy](#build-deploy)
    - [Optional Launch app after install](#optional-launch-app-after-install)
  - [Logs Debugging](#logs-debugging)
  - [Test list](#test-list)
<!-- TOC END -->

</details>

---


## Build Deploy

From terminal on Ubuntu:

```bash
./gradlew clean
./gradlew build
./gradlew assembleDebug
./gradlew installDebug
```

### Optional Launch app after install
```bash
adb shell monkey -p de.nathabee.pomolobee -c android.intent.category.LAUNCHER 1
```

## Logs Debugging

Use Android Studio → `View > Tool Windows > Logcat`

---
## Test list
Test report will be stored in documentation/Reports
use the most recent checklist, save it with current datum 
use the report Template, save it with current datum 
run the test