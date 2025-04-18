package de.nathabee.pomolobee.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.Text
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import de.nathabee.pomolobee.ui.screens.*
import de.nathabee.pomolobee.viewmodel.ImageViewModel
import de.nathabee.pomolobee.viewmodel.OrchardViewModel
import de.nathabee.pomolobee.viewmodel.SettingsViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    orchardViewModel: OrchardViewModel,
    imageViewModel: ImageViewModel,
    settingsViewModel: SettingsViewModel
) {
    val locations by orchardViewModel.locations.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Camera.route
    ) {

        // 📷 Camera
        composable(Screen.Camera.route) {
            CameraScreen(
                navController = navController,
                orchardViewModel = orchardViewModel,
                settingsViewModel = settingsViewModel,
                imageViewModel = imageViewModel,
            )
        }

        // 🔄 Processing
        composable(Screen.Processing.route) {
            ProcessingScreen()
        }

        // ⚙️ Settings
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                orchardViewModel = orchardViewModel,
                settingsViewModel = settingsViewModel
            )
        }

        // 🌳 Orchard Overview
        composable(Screen.Orchard.route) {
            OrchardScreen(
                navController = navController,
                orchardViewModel = orchardViewModel
            )
        }

        // 📍 Location Picker
        composable(Screen.Location.route) {
            LocationScreen(
                navController = navController,
                orchardViewModel = orchardViewModel,
                settingsViewModel = settingsViewModel,
                imageViewModel = imageViewModel
            )
        }

        // 🖼️ Image History
        composable(Screen.ImageHistory.route) {
            ImageHistoryScreen(
                orchardViewModel = orchardViewModel,
                imageViewModel = imageViewModel,
                settingsViewModel = settingsViewModel
            )
        }

        // 🗺️ SVG Map (returns row + xy via returnKey)
        composable(
            route = "${Screen.SvgMap.route}?fieldId={fieldId}&returnKey={returnKey}",
            arguments = listOf(
                navArgument("fieldId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("returnKey") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->

            val fieldId = backStackEntry.arguments?.getString("fieldId")?.toIntOrNull()
            val returnKey = backStackEntry.arguments?.getString("returnKey") ?: "svg_return"
            val location = locations.find { it.field.fieldId == fieldId }

            if (location != null) {
                SvgMapScreen(
                    location = location,
                    settingsViewModel = settingsViewModel,
                    orchardViewModel = orchardViewModel,
                    navController = navController,
                    returnKey = returnKey
                )
            } else {
                Text("❌ Field not found")
            }
        }

        // 🐞 Error Log
        composable(Screen.ErrorLog.route) {
            ErrorLogScreen(settingsViewModel = settingsViewModel)
        }

        // ℹ️ About
        composable(Screen.About.route) {
            AboutScreen(settingsViewModel = settingsViewModel)
        }
    }
}
