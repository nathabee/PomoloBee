**Initialisation history **
- This document is used for me to trace how i created this repository initialisation when all project was empty, for the next time i make a project. It serves only information purposes.


🎯 **This document serves as a high-level development guide**.  
📌 **Detailed code and specifications are available in GitHub**. 

---
<details>
<summary>Table of Content</summary>

<!-- TOC -->
- [**STEP BY STEP DEVELOPMENT AND TEST**](#step-by-step-development-and-test)
  - [**STEP 1 Project Initialization**](#step-1-project-initialization)
  - [**STEP 2 Screen Management and Navigation**](#step-2-screen-management-and-navigation)
    - [**Project Architecture**](#project-architecture)
  - [**STEP 3 Apple Detection Overview**](#step-3-apple-detection-overview)
  - [**STEP 4 User Preferences Jetpack DataStore**](#step-4-user-preferences-jetpack-datastore)
  - [**STEP 5 UI/UX Improvements**](#step-5-uiux-improvements)
  - [**STEP 6 include a App Theme and a special Font](#step-6-include-a-app-theme-and-a-special-font)
  - [**STEP 7 define specification](#step-7-define-specification)
  - [Testing and Deployment**](#testing-and-deployment)
<!-- TOC END -->
 
</details>

---

# **STEP BY STEP DEVELOPMENT AND TEST**

## **STEP 1 Project Initialization**
🔹 **Create Empty Activity Project** in **Android Studio**  
🔹 **Project Name** = `PomoloBee`  
🔹 **Package Name** = `pomolobee` (Changed to `de.nathabee.pomolobee`)  
🔹 **GitHub Repository**: `PomoloBee/PomoloBeeApp`  
🔹 **Update Dependencies**:  
   - Use **`libs.versions.toml`** for dependency management  
   - Switch from **KAPT** to **KSP** for better performance  
🔹 **Modify `build.gradle.kts`** to support **Jetpack Compose**  

---

## **STEP 2 Screen Management and Navigation**
### **Project Architecture**
Since **Jetpack Compose replaces Fragments**, the project follows a **modern Compose-based navigation structure**:
- **1 Main Activity (`MainActivity.kt`)**
- **1 Navigation Host (`NavGraph.kt`)**
- **Multiple Screens (`HomeScreen.kt`, `CameraScreen.kt`, `SettingsScreen.kt`)**
- **Drawer Menu (`DrawerMenu.kt`) for easy navigation**  

🔹 **Implement Jetpack Compose Navigation**  
🔹 **Define `Screen.kt` (Sealed Class) to manage routes**  
🔹 **Use `NavGraph.kt` to define screen transitions**  

---

## **STEP 3 Apple Detection Overview**
🔹 **Integrate OpenCV for image processing**  
🔹 **Develop `CameraView.kt` to handle live camera feed**  
🔹 **Process images in `utils/ImageProcessing.kt` using OpenCV**:
   - Convert to grayscale  
   - Apply Gaussian blur  
   - Detect edges using Canny edge detection  
🔹 **Use `CameraScreen.kt` to display live camera feed and processed frames**  

---

## **STEP 4 User Preferences Jetpack DataStore**
🔹 **Store user settings using `UserPreferences.kt`**  
🔹 **Allow users to select preferred apple types in `SettingsScreen.kt`**  
🔹 **Save & Retrieve settings using Jetpack DataStore**  

---

## **STEP 5 UI/UX Improvements**
🔹 **Implement Material 3 theming (`ui/theme/`)**  
🔹 **Ensure responsive layout for different devices**  
🔹 **Improve UI animations and transitions**  

---

## **STEP 6 include a App Theme and a special Font
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


---

## **STEP 7 define specification
- creation data modele for Django 
- define workflow
- define API format
- create detailled specification for the App (screen, workflow)
- define UI for App


## Testing and Deployment**
🔹 **Test Navigation Flow** (Ensure all screens are accessible)  
🔹 **Validate OpenCV Processing** (Check live camera detection)  
🔹 **Debug DataStore Preferences** (Ensure settings are saved & loaded correctly)  
🔹 **Optimize App Performance**  
🔹 **Prepare for Deployment (Google Play Store if needed)**  

---
 
 