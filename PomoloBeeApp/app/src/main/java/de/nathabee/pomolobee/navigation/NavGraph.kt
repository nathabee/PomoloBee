package de.nathabee.pomolobee.navigation

import PomolobeeViewModels
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
    sharedViewModels: PomolobeeViewModels
) {
    val orchardViewModel = sharedViewModels.orchard
    val imageViewModel = sharedViewModels.image
    val settingsViewModel = sharedViewModels.settings

    val locations by orchardViewModel.locations.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Camera.route
    ) {

        // 📷 Camera
        composable(Screen.Camera.route) {
            CameraScreen(
                navController = navController,
                sharedViewModels = sharedViewModels
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
                sharedViewModels = sharedViewModels
            )
        }

        // 🌳 Orchard Overview
        composable(Screen.Orchard.route) {
            OrchardScreen(
                navController = navController,
                sharedViewModels = sharedViewModels
            )
        }

        // 📍 Location Picker
        composable(Screen.Location.route) {
            LocationScreen(
                navController = navController,
                sharedViewModels = sharedViewModels
            )
        }

        // 🖼️ Image History
        composable(Screen.ImageHistory.route) {
            ImageHistoryScreen(
                sharedViewModels = sharedViewModels,
                navController =  navController
            )
        }

        // 🗺️ SVG Map (returns row + xy via returnKey)
        composable(
            /*route = "${Screen.SvgMap.route}?fieldId={fieldId}&returnKey={returnKey}",*/
            route = "${Screen.SvgMap.route}?fieldId={fieldId}&returnKey={returnKey}&xyMarker={xyMarker}&readOnly={readOnly}",

            arguments = listOf(
                navArgument("fieldId") { type = NavType.StringType },
                navArgument("returnKey") { type = NavType.StringType },
                navArgument("xyMarker") { type = NavType.StringType; nullable = true },
                navArgument("readOnly") { type = NavType.BoolType; defaultValue = false }
            )

        ) { backStackEntry ->

            val fieldId = backStackEntry.arguments?.getString("fieldId")?.toIntOrNull()
            val returnKey = backStackEntry.arguments?.getString("returnKey") ?: "svg_return"
            val location = locations.find { it.field.fieldId == fieldId }
            val xyMarker = backStackEntry.arguments?.getString("xyMarker")
            val readOnly = backStackEntry.arguments?.getBoolean("readOnly") ?: false

            locations.find { it.field.fieldId == fieldId }?.let { location ->
                SvgMapScreen(
                    location = location,
                    sharedViewModels = sharedViewModels,
                    navController = navController,
                    returnKey = returnKey,
                    xyMarker = xyMarker,
                    readOnly = readOnly
                )

            } ?: Text("❌ Field not found")

        }

        composable(
            route = "${Screen.ImagePreviewMap.route}?fieldId={fieldId}&xyMarker={xyMarker}&readOnly={readOnly}",
            arguments = listOf(
                navArgument("fieldId") { type = NavType.IntType },
                navArgument("xyMarker") { type = NavType.StringType; nullable = true },
                navArgument("readOnly") { type = NavType.BoolType; defaultValue = true } // default true for readonly view
            )
        ) { backStackEntry ->

            val fieldId = backStackEntry.arguments?.getInt("fieldId")
            val xyMarker = backStackEntry.arguments?.getString("xyMarker")
            val readOnly = backStackEntry.arguments?.getBoolean("readOnly") ?: true

            val location = locations.find { it.field.fieldId == fieldId }

            if (location != null) {
                SvgMapScreen(
                    location = location,
                    xyMarker = xyMarker,
                    readOnly = readOnly,
                    sharedViewModels = sharedViewModels,
                    navController = navController,
                    returnKey = "readonly" // no return expected
                )
            } else {
                Text("❌ Field not found")
            }
        }


        // 🐞 Error Log
        composable(Screen.ErrorLog.route) {
            ErrorLogScreen(
                sharedViewModels = sharedViewModels)
        }

        // ℹ️ About
        composable(Screen.About.route) {
            AboutScreen(
                sharedViewModels = sharedViewModels)
        }
    }
}
