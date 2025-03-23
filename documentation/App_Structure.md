**PomoloBeeApp detailled specification**
---
<details>
<summary>Table of Content</summary>
 
<!-- TOC -->
- [**️ Project Architecture**](#project-architecture)
- [**Project Structure**](#project-structure)
  - [**Root Files**](#root-files)
    - [**1 `MainActivity.kt`**](#1-mainactivitykt)
  - [**Navigation Manages Screen Routing**](#navigation-manages-screen-routing)
    - [**2 `navigation/NavGraph.kt`**](#2-navigationnavgraphkt)
    - [**3 `navigation/Screen.kt`**](#3-navigationscreenkt)
  - [**UI Manages UI Components Screens**](#ui-manages-ui-components-screens)
    - [**4 `ui/components/CameraView.kt`**](#4-uicomponentscameraviewkt)
    - [**5 `ui/components/DrawerMenu.kt`**](#5-uicomponentsdrawermenukt)
  - [**UI Screens**](#ui-screens)
    - [**6 `ui/screens/HomeScreen.kt`**](#6-uiscreenshomescreenkt)
    - [**7 `ui/screens/CameraScreen.kt`**](#7-uiscreenscamerascreenkt)
    - [**8 `ui/screens/SettingsScreen.kt`**](#8-uiscreenssettingsscreenkt)
    - [**9 `ui/screens/AboutScreen.kt`**](#9-uiscreensaboutscreenkt)
  - [**Theme Handles UI Styling**](#theme-handles-ui-styling)
    - [**`ui/theme/Color.kt`**](#uithemecolorkt)
    - [**11 `ui/theme/Theme.kt`**](#11-uithemethemekt)
    - [**12 `ui/theme/Type.kt`**](#12-uithemetypekt)
  - [**Data Handles User Preferences**](#data-handles-user-preferences)
    - [**13 `data/UserPreferences.kt`**](#13-datauserpreferenceskt)
  - [**Utils Handles Apple Image Processing**](#utils-handles-apple-image-processing)
    - [**14 `utils/ImageProcessing.kt`**](#14-utilsimageprocessingkt)
- [**Summary**](#summary)
<!-- TOC END -->
 
</details>

---

# **️ Project Architecture**
Since **Jetpack Compose doesn't use Fragments**, we will replace the **"1 Activity - Multiple Fragments"** structure with:

- **1 Main Activity**
- **1 NavHost (Handles screen navigation)**
- **Multiple Composable Screens**
- **A Drawer Menu (Navigation Drawer)**

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
            │   │   └── DrawerMenu.kt
            │   ├── screens
            │   │   ├── AboutScreen.kt
            │   │   ├── CameraScreen.kt
            │   │   ├── HomeScreen.kt
            │   │   └── SettingsScreen.kt
            │   └── theme
            │       ├── Color.kt
            │       ├── Theme.kt
            │       └── Type.kt
            └── utils
                └── ImageProcessing.kt


```

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
- Handles the **drawer menu**.

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
}
```

---

## **UI Manages UI Components Screens**
### **4 `ui/components/CameraView.kt`**
📌 **Purpose:**  
- Displays the **camera preview using OpenCV**.
- Processes camera frames using `detectApple()`.

📌 **Key Responsibilities:**
- Uses `AndroidView` to embed a native camera preview in Compose.
- Passes frames to **`utils/ImageProcessing.kt`** for apple detection.

📌 **Example Usage in `CameraScreen.kt`:**
```kotlin
CameraView(context = context, modifier = Modifier.fillMaxSize())
```

---

### **5 `ui/components/DrawerMenu.kt`**
📌 **Purpose:**  
- **Creates the navigation drawer (sidebar menu)**.

📌 **Key Responsibilities:**
- Displays a list of **navigation items (Home, Camera, Settings)**.
- Calls `navController.navigate(route)` when a user selects an item.

📌 **Example Usage in `MainActivity.kt`:**
```kotlin
ModalNavigationDrawer(
    drawerContent = { DrawerMenu(navController) }
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
- Passes camera frames to `detectApple()` for processing.

---

### **8 `ui/screens/SettingsScreen.kt`**
📌 **Purpose:**  
- Displays settings where users can **save preferences**.

📌 **Key Responsibilities:**
- Uses **Jetpack DataStore (`UserPreferences.kt`)** to save and load settings.

📌 **Example Usage:**
```kotlin
scope.launch { UserPreferences.savePreference(context, "apple_type", selectedApple) }
```

---

### **9 `ui/screens/AboutScreen.kt`**
📌 **Purpose:**  
- Displays **app information**.

📌 **Key Responsibilities:**
- Provides details about PomoloBee and **its purpose**.

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
- Saves **user settings** (e.g., preferred apple type).
- Retrieves saved settings when the app starts.

📌 **Example Usage in `SettingsScreen.kt`:**
```kotlin
UserPreferences.savePreference(context, "apple_type", selectedApple)
```

---

## **Utils Handles Apple Image Processing**
### **14 `utils/ImageProcessing.kt`**
📌 **Purpose:**  
- Processes camera frames **to detect apples**.

📌 **Key Responsibilities:**
- Converts images to grayscale.
- Applies **edge detection** for apple recognition.

📌 **Example Usage in `CameraView.kt`:**
```kotlin
val processedFrame = detectApple(inputFrame.rgba())
```

---

# **Summary**
| **📂 Folder** | **Purpose** |
|--------------|------------|
| `MainActivity.kt` | **Entry point** of the app, initializes UI & navigation |
| `navigation/` | **Manages screen navigation** with `NavGraph.kt` |
| `ui/components/` | **Reusable UI elements (CameraView, Drawer Menu)** |
| `ui/screens/` | **Defines individual app screens** |
| `ui/theme/` | **Defines UI styling (colors, typography, themes)** |
| `data/` | **Stores user preferences (Jetpack DataStore)** |
| `utils/` | **Handles image processing with OpenCV** |

--- 