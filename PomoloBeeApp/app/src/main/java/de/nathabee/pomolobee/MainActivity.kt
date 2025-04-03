package de.nathabee.pomolobee

import android.content.Context
import android.net.Uri
import android.os.Bundle
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
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController



import de.nathabee.pomolobee.navigation.NavGraph
import de.nathabee.pomolobee.ui.components.DrawerMenu
import de.nathabee.pomolobee.ui.theme.PomoloBeeTheme
import de.nathabee.pomolobee.ui.screens.InitScreen
import de.nathabee.pomolobee.cache.OrchardCache
import de.nathabee.pomolobee.ui.components.PermissionManager
import de.nathabee.pomolobee.viewmodel.SettingsViewModel
import de.nathabee.pomolobee.viewmodel.SettingsViewModelFactory
import de.nathabee.pomolobee.viewmodel.OrchardViewModel
import de.nathabee.pomolobee.util.copyAssetsIfNotExists





class MainActivity : ComponentActivity() {

    //###########################################################################

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions.all { it.value }
            if (granted) {
                onPermissionsGranted()
            } else {
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        if (PermissionManager.allGranted(this)) {
            onPermissionsGranted()
        } else {
            permissionLauncher.launch(PermissionManager.REQUIRED_PERMISSIONS)
        }

    }

    private fun onPermissionsGranted() {
        // copyAssetsIfNotExists(this)
        // OrchardRepository.loadAllConfig(this)

        System.loadLibrary("opencv_java4")

        setContent {
            PomoloBeeTheme {
                PomoloBeeAppWithViewModel()
            }
        }

    }


}



//###########################################################################

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(navController: NavHostController) {
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
                NavGraph(navController)
            }
        }
    }
}

//###########################################################################
@Composable
fun PomoloBeeAppWithViewModel() {
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(LocalContext.current))
    PomoloBeeApp(viewModel)
}


//###########################################################################
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomoloBeeApp(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val storageRootUri by viewModel.storageRootUri.collectAsState()
    val isInitialized by viewModel.isSetupComplete.collectAsState()

    val orchardViewModel: OrchardViewModel = viewModel()
    val setupState = remember { mutableStateOf(false) }

    if (!isInitialized || !setupState.value) {
        InitScreen(onSetupComplete = { pickedUri ->
            viewModel.setStorageRoot(pickedUri)
            setupState.value = true
        })

    } else {
        AppScaffold(navController = navController)

        LaunchedEffect(storageRootUri) {
            if (OrchardCache.locations.isEmpty()) {
                storageRootUri?.let { uri ->
                    copyAssetsIfNotExists(context, uri)
                }

                viewModel.configDirectory.value?.let { configUri ->
                    orchardViewModel.loadLocalConfig(configUri, context)
                }

            }
        }
    }
}


