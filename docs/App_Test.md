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

- go to thje github  page : https://nathabee.github.io/PomoloBee/
- click button "App Test Check List"
- fill meta data in "Test Session Info"
- there is 2 button for 2 choices :
- open a "new app test checklist" (per default opened, can be used to reset a test)
- open a "JSON checklist" (to continue working on a JSON exported locally / on the cloud last time)
- fill test result
- export as JSON (to keep working on the checklist ) at the end or as MD (to get a finalized output)