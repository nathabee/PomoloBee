package de.nathabee.pomolobee

import android.content.Context
import android.net.Uri
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
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController



import de.nathabee.pomolobee.navigation.NavGraph
import de.nathabee.pomolobee.ui.components.DrawerMenu
import de.nathabee.pomolobee.ui.theme.PomoloBeeTheme
import de.nathabee.pomolobee.ui.screens.InitScreen
import de.nathabee.pomolobee.cache.OrchardCache
import de.nathabee.pomolobee.ui.components.PermissionManager
import de.nathabee.pomolobee.ui.screens.SettingsScreen
import de.nathabee.pomolobee.viewmodel.SettingsViewModel
import de.nathabee.pomolobee.viewmodel.SettingsViewModelFactory
import de.nathabee.pomolobee.viewmodel.OrchardViewModel
import de.nathabee.pomolobee.util.copyAssetsIfNotExists
import de.nathabee.pomolobee.util.hasAccessToUri
import de.nathabee.pomolobee.viewmodel.OrchardViewModelFactory
import kotlinx.coroutines.flow.StateFlow


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
                PomoloBeeApp()
            }
        }

    }


}



//###########################################################################

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    navController: NavHostController,
    orchardViewModel: OrchardViewModel,
    settingsViewModel: SettingsViewModel
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
                    orchardViewModel = orchardViewModel,
                    settingsViewModel = settingsViewModel
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

    Log.d("PomoloBeeApp", "🔥 Composable recomposed")

    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(context))
    val orchardViewModel: OrchardViewModel = viewModel(factory = OrchardViewModelFactory(context))

    val storageRootUri by settingsViewModel.storageRootUri.collectAsState()
    val rawUri = storageRootUri
    var initDone by remember { mutableStateOf(false) }

    Log.d("PomoloBeeApp", "📦 Raw URI from preferences = $rawUri")
    Log.d("PomoloBeeApp", "📦 OrchardCache initialized = ${OrchardCache.isInitialized()}")
    Log.d("PomoloBeeApp", "📦 hasAccessToUri = ${rawUri?.let { hasAccessToUri(context, it) }}")
    Log.d("PomoloBeeApp", "📦 initDone = $initDone")

    if (rawUri == null || !initDone || !hasAccessToUri(context, rawUri)) {
        Log.w("PomoloBeeApp", "🚨 Invalid or not ready → showing InitScreen")

        InitScreen(
            settingsViewModel = settingsViewModel,
            orchardViewModel = orchardViewModel,
            onInitFinished = {
                Log.i("PomoloBeeApp", "✅ Init finished")
                initDone = true
            }
        )
        return // 🛑 Prevent launching the rest of the app
    }

    Log.i("PomoloBeeApp", "✅ Valid URI + initDone → launching full app")

    AppScaffold(
        navController = navController,
        orchardViewModel = orchardViewModel,
        settingsViewModel = settingsViewModel
    )

    LaunchedEffect(Unit) {
        Log.d("PomoloBeeApp", "⏳ LaunchedEffect started. Is cache empty? ${OrchardCache.locations.isEmpty()}")

        if (OrchardCache.locations.isEmpty()) {
            Log.i("PomoloBeeApp", "📦 Cache is empty. Starting config load and asset copy...")
            copyAssetsIfNotExists(context, rawUri)
            orchardViewModel.loadLocalConfig(rawUri, context)
            settingsViewModel.invalidate()
            Log.i("PomoloBeeApp", "🎉 Config load complete")
        } else {
            Log.d("PomoloBeeApp", "👍 Cache already initialized — skipping config load")
        }
    }
}
