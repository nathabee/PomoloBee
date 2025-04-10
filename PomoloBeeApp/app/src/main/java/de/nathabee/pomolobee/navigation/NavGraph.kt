package de.nathabee.pomolobee.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.nathabee.pomolobee.cache.OrchardCache
import de.nathabee.pomolobee.ui.screens.*
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.datastore.preferences.core.PreferencesSerializer.defaultValue
import androidx.navigation.NavType
import androidx.navigation.navArgument
import de.nathabee.pomolobee.viewmodel.OrchardViewModel
import de.nathabee.pomolobee.viewmodel.SettingsViewModel


@Composable
fun NavGraph(
    navController: NavHostController,
    orchardViewModel: OrchardViewModel,
    settingsViewModel: SettingsViewModel
)
{
    val locations by orchardViewModel.locations.collectAsState()

    NavHost(navController = navController, startDestination = Screen.Camera.route) {

        composable(Screen.Camera.route) {
            CameraScreen(
                navController = navController,
                orchardViewModel = orchardViewModel,
                settingsViewModel = settingsViewModel
            )
        }
        composable(Screen.Processing.route) { ProcessingScreen() }
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                orchardViewModel = orchardViewModel,
                settingsViewModel = settingsViewModel
            )
        }

        composable(Screen.Orchard.route) { OrchardScreen(
            navController = navController,
            orchardViewModel = orchardViewModel) }
        composable(Screen.About.route) { AboutScreen(settingsViewModel = settingsViewModel) }
        composable(Screen.Location.route) { LocationScreen(
                navController = navController,
            orchardViewModel = orchardViewModel,
            settingsViewModel = settingsViewModel
        ) }

        composable(
            route = "${Screen.SvgMap.route}?fieldId={fieldId}",
            arguments = listOf(
                navArgument("fieldId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->

            val fieldId = backStackEntry.arguments?.getString("fieldId")?.toIntOrNull()
            val location = fieldId?.let { id ->
                locations.find { it.field.fieldId == id }
            }

            if (location != null) {
                SvgMapScreen(
                    location = location,
                    settingsViewModel = settingsViewModel,
                    orchardViewModel = orchardViewModel,
                    onRawSelected = { rowIdStr ->
                        val rowId = rowIdStr.removePrefix("row_").toIntOrNull()
                        if (rowId != null) {
                            // Best practice: update both field and row in case context was lost
                            settingsViewModel.updateSelectedField(location.field.fieldId)
                            settingsViewModel.updateSelectedRow(rowId)

                            // Go back to the Location screen
                            navController.popBackStack()
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            } else {
                Text("❌ Field not found")
            }
        }


        composable(Screen.ErrorLog.route) {
            ErrorLogScreen(settingsViewModel = settingsViewModel)
        }

    }
}
