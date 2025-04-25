package de.nathabee.pomolobee

import PomolobeeViewModels
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController



import de.nathabee.pomolobee.navigation.NavGraph
import de.nathabee.pomolobee.ui.components.DrawerMenu
import de.nathabee.pomolobee.ui.theme.PomoloBeeTheme
import de.nathabee.pomolobee.ui.screens.InitScreen
import de.nathabee.pomolobee.ui.components.PermissionManager
import de.nathabee.pomolobee.viewmodel.ImageViewModel
import de.nathabee.pomolobee.viewmodel.ImageViewModelFactory
import de.nathabee.pomolobee.viewmodel.SettingsViewModel
import de.nathabee.pomolobee.viewmodel.SettingsViewModelFactory
import de.nathabee.pomolobee.viewmodel.OrchardViewModel
import de.nathabee.pomolobee.viewmodel.OrchardViewModelFactory




    class MainActivity : ComponentActivity() {

        private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            permissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val granted = permissions.all { it.value }
                if (granted) {
                    launchApp()
                } else {
                    Toast.makeText(this, "Permissions not granted", Toast.LENGTH_LONG).show()
                    finish()
                }
            }

            if (PermissionManager.allGranted(this)) {
                launchApp()
            } else {
                permissionLauncher.launch(PermissionManager.REQUIRED_PERMISSIONS)
            }
        }

        private fun launchApp() {
            setContent {
                PomoloBeeTheme {
                    PomoloBeeApp() // 👈 no need to pass URI anymore
                }
            }
        }
    }







//###########################################################################

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    navController: NavHostController,
    sharedViewModels: PomolobeeViewModels
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerMenu(scope, drawerState) { route -> navController.navigate(route) }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("PomoloBee") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                NavGraph(
                    navController = navController,
                    sharedViewModels = sharedViewModels
                )
            }
        }
    }
}




    //###########################################################################
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PomoloBeeApp() {
        val context = LocalContext.current
        val navController = rememberNavController()

        val sharedViewModels = PomolobeeViewModels(
            settings = viewModel(factory = SettingsViewModelFactory(context)),
            orchard = viewModel(factory = OrchardViewModelFactory(context)),
            image = viewModel(factory = ImageViewModelFactory(context))
        )

        val initDone by sharedViewModels.settings.initDone.collectAsState()


        Log.d("PomoloBeeApp", "🚀 recomposed with initDone = $initDone")
        if (!initDone) {
            Log.d("PomoloBeeApp", "Start InitScreen")
            InitScreen(
                sharedViewModels = sharedViewModels,
                onInitFinished = {
                    // is done is made bei initScreen
                   sharedViewModels.settings.markInitDone()
                }
            )
        } else {
            Log.d("PomoloBeeApp", "Scaffolding app")
            AppScaffold(
                navController = navController,
                sharedViewModels = sharedViewModels
            )
        }


    }

        /* INFO :If you ever face double UI (e.g. InitScreen + MainScreen flashing),
           you can debounce init like this:
                val hasStartedMainScreen = remember { mutableStateOf(false) }

                if (!initDone && !hasStartedMainScreen.value) {
                    InitScreen(...)
                } else {
                    hasStartedMainScreen.value = true
                    MainAppScaffold(...)
                }
         */
