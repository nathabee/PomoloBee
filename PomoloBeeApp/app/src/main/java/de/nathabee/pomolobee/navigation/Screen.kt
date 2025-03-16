package de.nathabee.pomolobee.navigation


sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Home")
    object Camera : Screen("camera", "Camera")
    object Settings : Screen("settings", "Settings")
    object About : Screen("about", "About")
}
