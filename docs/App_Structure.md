**PomoloBeeApp detailled specification**
---
<details>
<summary>Table of Content</summary>
  
<!-- TOC -->
- [**️ Project Architecture**](#project-architecture)
- [**Project Structure**](#project-structure)
    - [Folder Recap](#folder-recap)
  - [**Root Files**](#root-files)
    - [**1 `MainActivity.kt`**](#1-mainactivitykt)
  - [**Navigation Manages Screen Routing**](#navigation-manages-screen-routing)
    - [**2 `navigation/NavGraph.kt`**](#2-navigationnavgraphkt)
    - [**3 `navigation/Screen.kt`**](#3-navigationscreenkt)
  - [**UI Manages UI Components Screens**](#ui-manages-ui-components-screens)
    - [**4 `ui/components/CameraView.kt`**](#4-uicomponentscameraviewkt)
    - [**5 `ui/components/DrowerMenu.kt`**](#5-uicomponentsdrowermenukt)
  - [**UI Screens**](#ui-screens)
    - [**6 `ui/screens/HomeScreen.kt`**](#6-uiscreenshomescreenkt)
    - [**7 `ui/screens/CameraScreen.kt`**](#7-uiscreenscamerascreenkt)
    - [**8 `ui/screens/SettingsScreen.kt`**](#8-uiscreenssettingsscreenkt)
    - [**9 `ui/screens/AboutScreen.kt`**](#9-uiscreensaboutscreenkt)
    - [**10 `ui/screens/PreviewScreen.kt`**](#10-uiscreenspreviewscreenkt)
    - [**11 `ui/screens/LocalResultScreen.kt`**](#11-uiscreenslocalresultscreenkt)
    - [**12 `ui/screens/ErrorLogScreen.kt`**](#12-uiscreenserrorlogscreenkt)
    - [**13 `ui/screens/SplashScreen.kt`**](#13-uiscreenssplashscreenkt)
    - [**14 `ui/screens/ImageHistoryScreen.kt`**](#14-uiscreensimagehistoryscreenkt)
    - [**15 `ui/components/FolderPicker.kt`**](#15-uicomponentsfolderpickerkt)
    - [**16 `ui/components/PermissionManager.kt`**](#16-uicomponentspermissionmanagerkt)
    - [**17 `viewmodel/SharedViewModel.kt`**](#17-viewmodelsharedviewmodelkt)
    - [**18 `viewmodel/ImageListViewModel.kt`**](#18-viewmodelimagelistviewmodelkt)
  - [**Theme Handles UI Styling**](#theme-handles-ui-styling)
    - [**`ui/theme/Color.kt`**](#uithemecolorkt)
    - [**11 `ui/theme/Theme.kt`**](#11-uithemethemekt)
    - [**12 `ui/theme/Type.kt`**](#12-uithemetypekt)
  - [**Data Handles User Preferences**](#data-handles-user-preferences)
    - [**13 `data/UserPreferences.kt`**](#13-datauserpreferenceskt)
  - [**Utils Handles fruit Image Processing**](#utils-handles-fruit-image-processing)
    - [**14 `utils/ImageProcessing.kt`**](#14-utilsimageprocessingkt)
  - [**Model Classes Shared between UI/Repo/API**](#model-classes-shared-between-uirepoapi)
  - [**ViewModels**](#viewmodels)
  - [**Repository**](#repository)
    - [**ImageRepository.kt**](#imagerepositorykt)
    - [**OrchardRepository.kt**](#orchardrepositorykt)
    - [**SettingsRepository.kt** *optional but recommended*](#settingsrepositorykt-optional-but-recommended)
  - [**Folder overview**](#folder-overview)
<!-- TOC END -->
 
</details>

---

# **️ Project Architecture**
Since **Jetpack Compose doesn't use Fragments**, we will replace the **"1 Activity - Multiple Fragments"** structure with:

- **1 Main Activity**
- **1 NavHost (Handles screen navigation)**
- **Multiple Composable Screens**
- **A Drower Menu (Navigation Drower)**

---


---

# **Project Structure**
```plaintext
 tree app/src/main/java
app/src/main/java
└── de
    └── nathabee
        └── pomolobee
            ├── data
            │   └── UserPreferences.kt
            ├── MainActivity.kt
            ├── navigation
            │   ├── NavGraph.kt
            │   └── Screen.kt
            ├── ui
            │   ├── components
            │   │   ├── CameraView.kt
            │   │   ├── DrowerMenu.kt 
            │   │   ├── ImageCard.kt               // reuse for preview + metadata
            │   │   ├── FolderPicker.kt            // used in SettingsScreen to change image path
            │   │   └── PermissionManager.kt
            │   ├── screens
            │   │   ├── ProcessingScreen.kt        // for viewing & uploading images
            │   │   ├── ResultScreen.kt            // shows detection/yield after processing
            │   │   ├── OrchardScreen.kt           // read-only orchard visualisation
            │   │   ├── LocationScreen.kt          // row + field selection before save
            │   │   ├── AboutScreen.kt
            │   │   ├── CameraScreen.kt
            │   │   ├── ErrorLogScreen.kt
            │   │   ├── HomeScreen.kt
            │   │   ├── ImageHistoryScreen.kt
            │   │   ├── LocalResultScreen.kt
            │   │   ├── PreviewScreen.kt
            │   │   ├── SettingsScreen.kt
            │   │   └── SplashScreen.kt
            │   └── theme
            │       ├── Color.kt
            │       ├── Theme.kt
            │       └── Type.kt
            ├── utils
            │   └── ImageProcessing.kt
            ├── network
            │   ├── ApiClient.kt             // Retrofit builder
            │   ├── ImageApiService.kt       // For image-related endpoints
            │   ├── OrchardApiService.kt     // For orchards, rows, fruits
            │   └── ModelApiService.kt       // For ML model metadata: versioning, local/remote info
            ├── repository
            │   ├── ImageRepository.kt       // Handles all image-related data operations
            │   ├── OrchardRepository.kt     // For fields, rows, fruits
            │   ├── SettingsRepository.kt    // Optional, for DataStore abstraction
            └── model/
                ├── PendingImage.kt
                ├── OrchardData.kt
            └── viewmodel
                ├── ImageListViewModel.kt
                ├── ImageViewModel.kt          // Manages local + uploaded images
                ├── SettingsViewModel.kt       // Manages preferences (path, syncing, etc.)
                └── SharedViewModel.kt




```

---

### Folder Recap

| Folder | Use for |
|--------|---------|
| `ui/` | Composable screens, components, themes |
| `viewmodel/` | Jetpack ViewModels (UI/business logic) |
| `repository/` | Data sources (local, remote, combined) |
| `network/` | Retrofit/API definitions |
| `model/` | Data classes, DTOs |
| `cache/` | In-memory singleton caches |
| `data/` | Data storage (e.g., `UserPreferences`) |
| `utils/` | General-purpose helpers
---

## **Root Files**
### **1 `MainActivity.kt`**
📌 **Purpose:**  
- The **entry point** of the app.
- **Hosts the `PomoloBeeApp()` function**, which initializes navigation and UI.
- Calls **`NavGraph.kt`** to handle screen changes.

📌 **Key Responsibilities:**
- Loads the main UI layout.
- Initializes **Jetpack Compose Navigation** (`rememberNavController()`).
- Handles the **drower menu**.

---

## **Navigation Manages Screen Routing**
### **2 `navigation/NavGraph.kt`**
📌 **Purpose:**  
- **Defines how users navigate between screens**.
- Uses **Jetpack Compose `NavHost`**.

📌 **Key Responsibilities:**
- Lists all available screens (`HomeScreen`, `CameraScreen`, `SettingsScreen`, etc.).
- Uses **`NavController`** to handle screen transitions.

📌 **Example Usage in `MainActivity.kt`:**
```kotlin
NavGraph(navController)
```

---

### **3 `navigation/Screen.kt`**
📌 **Purpose:**  
- **Defines all available screen routes** using a **sealed class**.

📌 **Key Responsibilities:**
- Provides a **single source of truth** for navigation routes.
- Prevents hardcoded route strings.

📌 **Example Definition:**
```kotlin
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Camera : Screen("camera")
    object Settings : Screen("settings")
    object Processing : Screen("processing")
    object Result : Screen("result")
    object Location : Screen("location")
    object Orchard : Screen("orchard")
    object About : Screen("about")
}

```

---

## **UI Manages UI Components Screens**
### **4 `ui/components/CameraView.kt`**
📌 **Purpose:**  
- Displays the **camera preview using OpenCV**.
- Processes camera frames using `detectfruit()`.

📌 **Key Responsibilities:**
- Uses `AndroidView` to embed a native camera preview in Compose.
- Passes frames to **`utils/ImageProcessing.kt`** for fruit detection.

📌 **Example Usage in `CameraScreen.kt`:**
```kotlin
CameraView(context = context, modifier = Modifier.fillMaxSize())
```

---

### **5 `ui/components/DrowerMenu.kt`**
📌 **Purpose:**  
- **Creates the navigation drower (sidebar menu)**.

📌 **Key Responsibilities:**
- Displays a list of **navigation items (Home, Camera, Settings)**.
- Calls `navController.navigate(route)` when a user selects an item.

📌 **Example Usage in `MainActivity.kt`:**
```kotlin
ModalNavigationDrower(
    drowerContent = { DrowerMenu(navController) }
)
```

---

## **UI Screens**
### **6 `ui/screens/HomeScreen.kt`**
📌 **Purpose:**  
- Displays **home screen UI**.

📌 **Key Responsibilities:**
- Provides buttons to navigate to other screens (`CameraScreen`, `SettingsScreen`).

📌 **Example Usage:**
```kotlin
Button(onClick = { navController.navigate(Screen.Camera.route) }) {
    Text("Open Camera")
}
```

---

### **7 `ui/screens/CameraScreen.kt`**
📌 **Purpose:**  
- **Displays the camera interface**.
- Calls **`CameraView.kt`** for OpenCV processing.

📌 **Key Responsibilities:**
- Initializes OpenCV (`OpenCVLoader.initDebug()`).
- Passes camera frames to `detectfruit()` for processing.

---

### **8 `ui/screens/SettingsScreen.kt`**
📌 **Purpose:**  
- Displays settings where users can **save preferences**.

📌 **Key Responsibilities:**
- Uses **Jetpack DataStore (`UserPreferences.kt`)** to save and load settings.

📌 **Example Usage:**
```kotlin
scope.launch { UserPreferences.savePreference(context, "fruit_type", selectedfruit) }
```

---

### **9 `ui/screens/AboutScreen.kt`**
📌 **Purpose:**  
- Displays **app information**.

📌 **Key Responsibilities:**
- Provides details about PomoloBee and **its purpose**.

---
### **10 `ui/screens/PreviewScreen.kt`**
📌 **Purpose:**  
- Provides a UI for the user to preview the selected image **before saving** or **uploading**.
- Allows re-selection or field/row assignment before confirming.

📌 **Key Responsibilities:**
- Display full-screen preview of image.
- Enable selection or change of field/row if not yet set.
- Buttons to "Save Locally" or "Discard Image".

---

### **11 `ui/screens/LocalResultScreen.kt`**
📌 **Purpose:**  
- Displays **results generated from local AI model** before uploading to backend.

📌 **Key Responsibilities:**
- Show image, fruit count, yield, and confidence from local detection.
- Optionally compare against last known backend result.

---

### **12 `ui/screens/ErrorLogScreen.kt`**
📌 **Purpose:**  
- View application-level errors, especially around storage, API, or local model.

📌 **Key Responsibilities:**
- Fetch errors from `/logs/errors.json` stored in Jetpack DataStore.
- Group logs by date or component.
- Add a drower entry if `DebugMode` is enabled.

---

### **13 `ui/screens/SplashScreen.kt`**
📌 **Purpose:**  
- Shown on app launch to manage first-run setup and permission checks.

📌 **Key Responsibilities:**
- Ask for permissions (camera, storage).
- Check Jetpack DataStore for first-run flags.
- Redirect to `CameraScreen` once ready.

---

### **14 `ui/screens/ImageHistoryScreen.kt`**
📌 **Purpose:**  
- Browse and filter all uploaded and processed images.

📌 **Key Responsibilities:**
- Pull data from `GET /api/history/`.
- Allow filtering by date, orchard, or status.
- Tap to open `ResultScreen` or `LocalResultScreen`.

---

### **15 `ui/components/FolderPicker.kt`**
📌 **Purpose:**
- Allow users to select a storage folder using Android's Storage Access Framework (SAF).

📌 **Key Responsibilities:**
- Trigger SAF intent to open directory picker.
- Store the chosen URI/path to DataStore.

---

### **16 `ui/components/PermissionManager.kt`**
📌 **Purpose:**
- Centralizes permission request & handling logic (camera, gallery, file storage).

📌 **Key Responsibilities:**
- Displays rationale dialog when needed.
- Fallback handling for permanent denials ("go to settings").
- Uses Compose + Accompanist APIs.

---

### **17 `viewmodel/SharedViewModel.kt`**
📌 **Purpose:**
- Share image, field/row selection, and temporary state across screens.

📌 **Key Responsibilities:**
- Store selected image URI and metadata before saving.
- Provide consistent access to draft image state.

---

### **18 `viewmodel/ImageListViewModel.kt`**
📌 **Purpose:**
- Central ViewModel to manage unsent, processing, and processed image lists.

📌 **Key Responsibilities:**
- Pull data from Jetpack DataStore and API.
- Handle list updates, retries, deletions.
- Used by `ProcessingScreen` and `HistoryScreen`.

---

## **Theme Handles UI Styling**
### **`ui/theme/Color.kt`**
📌 **Purpose:**  
- Defines **color schemes** for the app.

---

### **11 `ui/theme/Theme.kt`**
📌 **Purpose:**  
- Defines **Material 3 theming** for the entire app.

---

### **12 `ui/theme/Type.kt`**
📌 **Purpose:**  
- Defines **custom fonts and typography**.

---

## **Data Handles User Preferences**
### **13 `data/UserPreferences.kt`**
📌 **Purpose:**  
- Uses **Jetpack DataStore** to store and retrieve user preferences.

📌 **Key Responsibilities:**
- Saves **user settings** (e.g., preferred fruit type).
- Retrieves saved settings when the app starts.

📌 **Example Usage in `SettingsScreen.kt`:**
```kotlin
UserPreferences.savePreference(context, "fruit_type", selectedfruit)
```

---

## **Utils Handles fruit Image Processing**
### **14 `utils/ImageProcessing.kt`**
📌 **Purpose:**  
- Processes camera frames **to detect fruit**.

📌 **Key Responsibilities:**
- Converts images to grayscale.
- Applies **edge detection** for fruit recognition.

📌 **Example Usage in `CameraView.kt`:**
```kotlin
val processedFrame = detectfruit(inputFrame.rgba())
```
---
## **Model Classes Shared between UI/Repo/API**
The models like ( PendingImage.kt or Orchard.kt) match the JSON models:

```kotlin
data class PendingImage(
    val id: Int,
    val imagePath: String,
    val rowId: Int,
    val date: String
)

```
---
## **ViewModels**
 ViewModel logic internally to:
Not call APIs or DataStore directly (no call imageApiService). Instead, call functions from the appropriate repository  :

```kotlin
val response = imageRepository.uploadImage(imageData)

```
---
## **Repository**

### **ImageRepository.kt**
Handles:
- `uploadImage(imageData)`
- `getImageStatus(id)`
- `getResults(id)`
- `deleteImage(id)`
- `retryProcessing(id)`

Uses: `ImageApiService.kt` (Retrofit)

---

### **OrchardRepository.kt**
Handles:
- `getFields()`
- `getRows()`
- `getFruits()`

Uses: `OrchardApiService.kt`

---

### **SettingsRepository.kt** *optional but recommended*
Wraps:
- DataStore read/write
- Folder path preference
- "Backend disabled" toggle
- Error logs (writes to `/logs/errors.json`)

---

## **Folder overview**
| **📂 Folder** | **Purpose** |
|--------------|------------|
| `MainActivity.kt` | **Entry point** of the app, initializes UI & navigation |
| `navigation/` | **Manages screen navigation** with `NavGraph.kt` |
| `ui/components/` | **Reusable UI elements (CameraView, Drower Menu)** |
| `ui/screens/` | **Defines individual app screens** |
| `ui/theme/` | **Defines UI styling (colors, typography, themes)** |
| `data/` | **Stores user preferences (Jetpack DataStore)** |
| `utils/` | **Handles image processing with OpenCV** |
| `model/` | **Data classes like PendingImage, OrchardData** |
| `viewmodel/` | **ViewModels delegating to repositories (Image, Settings, Shared)** |
| `repository/` | **Business logic for images, settings, orchards** |
| `network/` | **Retrofit interfaces and API config** |

--- 


---

 ## Data architecture 
 
 ### Data storage


| **Key (DataStore)** | **Purpose** | **Method in ViewModel** | **Type** | **Used In** |
|---------------------|-------------|--------------------------|----------|-------------|
| `storage_root_path` | Root folder | `storageRoot`, `updateStorageRoot()` | StateFlow + suspend | `InitScreen`, `SettingsScreen` |
| *(derived)* | Config path | `configDirectory` | Computed | `SettingsScreen` |
| *(derived)* | Image path | `imageDirectory` | Computed | `SettingsScreen` |
| *(derived)* | Root fallback | `effectiveStorageRoot` | Computed | Internal logic |
| *(derived)* | Setup check | `isSetupComplete` | Computed | `PomoloBeeApp`, `InitScreen` |
| `sync_mode` | Sync strategy | `syncMode`, `updateSyncMode()` | StateFlow + suspend | `SettingsScreen` |
| `api_endpoint` | Server URL | `apiEndpoint`, `updateApiEndpoint()` | StateFlow + suspend | `SettingsScreen` |
| `media_endpoint` | Media server URL | `mediaEndpoint`, `updateMediaEndpoint()` | StateFlow + suspend | `SettingsScreen` |
| `debug_enabled` | Debug features toggle | `isDebug`, `updateDebugMode()` | StateFlow + suspend | `SettingsScreen` |
| `api_version` | Backend version synced | `apiVersion`, `updateApiVersion()` | StateFlow + suspend | `SettingsScreen` |
| `last_sync_date` | Sync timestamp | `lastSyncDate`, `updateLastSync()` | StateFlow + suspend | `SettingsScreen` |
| `selected_field` | Selected field ID | `selectedFieldId`, `updateSelectedField()` | StateFlow + suspend | `OrchardScreen`, `CameraScreen` |
| `selected_row` | Selected row ID | `selectedRowId`, `updateSelectedRow()` | StateFlow + suspend | `SvgMapScreen`, `CameraScreen` |


 
 ### example from storage to screen
  
 - example : `media_endpoint`**  
 

---
#### What is `media_endpoint`?

- It’s a **user-configurable** URL.
- Stored as a string in `DataStore` under the key `"media_endpoint"`.
- Exposed via `UserPreferences`, accessed and mutated through `SettingsViewModel`.

---

#### ️ How it’s defined in the code

##### In `UserPreferences.kt`

```kotlin
fun getMediaEndpoint(): Flow<String?> =
    context.dataStore.data.map { it[MEDIA_ENDPOINT_KEY] }

suspend fun setMediaEndpoint(value: String) {
    context.dataStore.edit { it[MEDIA_ENDPOINT_KEY] = value }
}
```

This gives us:
- A **Flow** of the current value (`null` if not set)
- A **suspend** setter to store a new value

---

#### In `SettingsViewModel.kt`

##### Reading

```kotlin
val mediaEndpoint = prefs.getMediaEndpoint().stateIn(
    viewModelScope, SharingStarted.Eagerly, ""
)
```

This gives you:
- `StateFlow<String?>` → which can be **observed in Compose** using `.collectAsState()`
- It **reactively updates** if the stored value changes

##### Writing

```kotlin
fun updateMediaEndpoint(value: String) = viewModelScope.launch {
    prefs.setMediaEndpoint(value)
}
```

This wraps the `UserPreferences` write method in coroutine scope for safe Compose usage.

---

#### ️ In `SettingsScreen.kt` the UI

##### Reading

```kotlin
val mediaEndpoint by viewModel.mediaEndpoint.collectAsState()
```

- This makes the value available inside the screen.
- It’s used to **pre-fill** the TextField when the screen loads.

##### Writing

```kotlin
TextField(
    value = mediaInput,
    onValueChange = { mediaInput = it },
    modifier = Modifier.fillMaxWidth()
)

Button(onClick = {
    scope.launch {
        viewModel.updateMediaEndpoint(mediaInput)
    }
}) {
    Text("💾 Save Settings")
}
```

- User types a new value → stored in `mediaInput`
- On button click, `viewModel.updateMediaEndpoint(...)` is called

---

#### TL;DR Summary

| Layer | Code | Purpose |
|-------|------|---------|
| `UserPreferences` | `getMediaEndpoint()` + `setMediaEndpoint()` | Raw storage/retrieval |
| `SettingsViewModel` | `val mediaEndpoint`, `fun updateMediaEndpoint()` | Reactive access + save logic |
| `SettingsScreen` | `collectAsState()`, `TextField`, `Button` | Display + update via ViewModel only |

---

✅ **You never call `prefs.setMediaEndpoint(...)` in the screen directly** — only the ViewModel does that. The screen only observes or triggers updates through the ViewModel.
 