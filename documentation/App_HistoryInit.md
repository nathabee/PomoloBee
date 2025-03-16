**Initialisation history **
- This document is used for me to trace how i created this repository initialisation when all project was empty, for the next time i make a project. It serves only information purposes.


🎯 **This document serves as a high-level development guide**.  
📌 **Detailed code and specifications are available in GitHub**. 

---

# **📌 STEP BY STEP DEVELOPMENT AND TEST**

## **STEP 1: Project Initialization**
🔹 **Create Empty Activity Project** in **Android Studio**  
🔹 **Project Name** = `PomoloBee`  
🔹 **Package Name** = `pomolobee` (Changed to `de.nathabee.pomolobee`)  
🔹 **GitHub Repository**: `PomoloBee/PomoloBeeApp`  
🔹 **Update Dependencies**:  
   - Use **`libs.versions.toml`** for dependency management  
   - Switch from **KAPT** to **KSP** for better performance  
🔹 **Modify `build.gradle.kts`** to support **Jetpack Compose**  

---

## **STEP 2: Screen Management and Navigation**
### **🔹 Project Architecture**
Since **Jetpack Compose replaces Fragments**, the project follows a **modern Compose-based navigation structure**:
- **1 Main Activity (`MainActivity.kt`)**
- **1 Navigation Host (`NavGraph.kt`)**
- **Multiple Screens (`HomeScreen.kt`, `CameraScreen.kt`, `SettingsScreen.kt`)**
- **Drawer Menu (`DrawerMenu.kt`) for easy navigation**  

🔹 **Implement Jetpack Compose Navigation**  
🔹 **Define `Screen.kt` (Sealed Class) to manage routes**  
🔹 **Use `NavGraph.kt` to define screen transitions**  

---

## **STEP 3: Apple Detection Overview**
🔹 **Integrate OpenCV for image processing**  
🔹 **Develop `CameraView.kt` to handle live camera feed**  
🔹 **Process images in `utils/ImageProcessing.kt` using OpenCV**:
   - Convert to grayscale  
   - Apply Gaussian blur  
   - Detect edges using Canny edge detection  
🔹 **Use `CameraScreen.kt` to display live camera feed and processed frames**  

---

## **STEP 4: User Preferences (Jetpack DataStore)**
🔹 **Store user settings using `UserPreferences.kt`**  
🔹 **Allow users to select preferred apple types in `SettingsScreen.kt`**  
🔹 **Save & Retrieve settings using Jetpack DataStore**  

---

## **STEP 5: UI/UX Improvements**
🔹 **Implement Material 3 theming (`ui/theme/`)**  
🔹 **Ensure responsive layout for different devices**  
🔹 **Improve UI animations and transitions**  

---

## **STEP 6: include a App Theme and a special Font
🔹 download a font and install in res/font
Modify app/src/main/java/de/nathabee/pomolobee/ui/theme/Type.kt to reference the font

🔹 modifiy the theme in 
import de.nathabee.pomolobee.ui.theme.PomoloBeeTheme

🔹 apply theme to mainActivity
        setContent {
            PomoloBeeTheme { // ✅ Theme is now correctly applied
                val navController = rememberNavController()
                NavGraph(navController)
            }





## Testing and Deployment**
🔹 **Test Navigation Flow** (Ensure all screens are accessible)  
🔹 **Validate OpenCV Processing** (Check live camera detection)  
🔹 **Debug DataStore Preferences** (Ensure settings are saved & loaded correctly)  
🔹 **Optimize App Performance**  
🔹 **Prepare for Deployment (Google Play Store if needed)**  

---
 
 

---

# **📌 Project Structure**
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

## **📂 Root Files**
### **1️⃣ `MainActivity.kt`**
📌 **Purpose:**  
- The **entry point** of the app.
- **Hosts the `PomoloBeeApp()` function**, which initializes navigation and UI.
- Calls **`NavGraph.kt`** to handle screen changes.

📌 **Key Responsibilities:**
- Loads the main UI layout.
- Initializes **Jetpack Compose Navigation** (`rememberNavController()`).
- Handles the **drawer menu**.

---

## **📂 Navigation (Manages Screen Routing)**
### **2️⃣ `navigation/NavGraph.kt`**
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

### **3️⃣ `navigation/Screen.kt`**
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

## **📂 UI (Manages UI Components & Screens)**
### **4️⃣ `ui/components/CameraView.kt`**
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

### **5️⃣ `ui/components/DrawerMenu.kt`**
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

## **📂 UI Screens**
### **6️⃣ `ui/screens/HomeScreen.kt`**
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

### **7️⃣ `ui/screens/CameraScreen.kt`**
📌 **Purpose:**  
- **Displays the camera interface**.
- Calls **`CameraView.kt`** for OpenCV processing.

📌 **Key Responsibilities:**
- Initializes OpenCV (`OpenCVLoader.initDebug()`).
- Passes camera frames to `detectApple()` for processing.

---

### **8️⃣ `ui/screens/SettingsScreen.kt`**
📌 **Purpose:**  
- Displays settings where users can **save preferences**.

📌 **Key Responsibilities:**
- Uses **Jetpack DataStore (`UserPreferences.kt`)** to save and load settings.

📌 **Example Usage:**
```kotlin
scope.launch { UserPreferences.savePreference(context, "apple_type", selectedApple) }
```

---

### **9️⃣ `ui/screens/AboutScreen.kt`**
📌 **Purpose:**  
- Displays **app information**.

📌 **Key Responsibilities:**
- Provides details about PomoloBee and **its purpose**.

---

## **📂 Theme (Handles UI Styling)**
### **🔟 `ui/theme/Color.kt`**
📌 **Purpose:**  
- Defines **color schemes** for the app.

---

### **1️⃣1️⃣ `ui/theme/Theme.kt`**
📌 **Purpose:**  
- Defines **Material 3 theming** for the entire app.

---

### **1️⃣2️⃣ `ui/theme/Type.kt`**
📌 **Purpose:**  
- Defines **custom fonts and typography**.

---

## **📂 Data (Handles User Preferences)**
### **1️⃣3️⃣ `data/UserPreferences.kt`**
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

## **📂 Utils (Handles Apple Image Processing)**
### **1️⃣4️⃣ `utils/ImageProcessing.kt`**
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

# **🚀 Summary**
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