package de.nathabee.pomolobee.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.nathabee.pomolobee.cache.OrchardCache
import de.nathabee.pomolobee.ui.screens.*
import androidx.compose.material3.Text


@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Camera.route) {
        composable(Screen.Camera.route) { CameraScreen(navController) }
        composable(Screen.Processing.route) { ProcessingScreen() }
        composable(Screen.Settings.route) { SettingsScreen() }
        composable(Screen.Orchard.route) { OrchardScreen( navController) }
        composable(Screen.About.route) { AboutScreen() }
        composable(Screen.Location.route) { LocationScreen(navController) }


        composable("svgmap/{fieldId}") { backStackEntry ->
            val fieldId = backStackEntry.arguments?.getString("fieldId")?.toIntOrNull()
            val location = OrchardCache.locations.find { it.field.fieldId == fieldId }

            if (location != null) {


                SvgMapScreen(
                    location = location,
                    onRawSelected = { rowId ->
                        println("Row selected: $rowId") // Later use for selection
                    },
                    onBack = { navController.popBackStack() }
                )
            } else {
                Text("Field not found")
            }
        }


    }
}
