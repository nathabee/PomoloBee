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
import androidx.compose.ui.Alignment



import de.nathabee.pomolobee.navigation.NavGraph
import de.nathabee.pomolobee.ui.components.DrawerMenu
import de.nathabee.pomolobee.ui.theme.PomoloBeeTheme
import de.nathabee.pomolobee.ui.screens.InitScreen
import de.nathabee.pomolobee.cache.OrchardCache
import de.nathabee.pomolobee.data.UserPreferences
import de.nathabee.pomolobee.ui.components.PermissionManager
import de.nathabee.pomolobee.ui.screens.SettingsScreen
import de.nathabee.pomolobee.viewmodel.SettingsViewModel
import de.nathabee.pomolobee.viewmodel.SettingsViewModelFactory
import de.nathabee.pomolobee.viewmodel.OrchardViewModel
import de.nathabee.pomolobee.util.copyAssetsIfNotExists
import de.nathabee.pomolobee.util.hasAccessToUri
import de.nathabee.pomolobee.viewmodel.OrchardViewModelFactory
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first



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
        // load if needed
        // System.loadLibrary("opencv_java4")

        // 🔍 Read preference synchronously BEFORE Compose
        val prefs = UserPreferences(this)
        val rawUri = runBlocking { prefs.getRawStorageRoot().first() }
        val startupUri = rawUri?.let { Uri.parse(it) }?.takeIf { hasAccessToUri(this, it) }

        Log.d("MainActivity", "📦 Loaded startupUri = $startupUri")

        setContent {
            PomoloBeeTheme {
                PomoloBeeApp(startupUri = startupUri) // 👈 Pass URI once
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
fun PomoloBeeApp(startupUri: Uri?) {
    val context = LocalContext.current
    val navController = rememberNavController()

    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(context))
    val orchardViewModel: OrchardViewModel = viewModel(factory = OrchardViewModelFactory(context))

    // Logging initial state
    Log.d("PomoloBeeApp", "🔥 Composable recomposed")
    Log.d("PomoloBeeApp", "📦 Initial startupUri = $startupUri")

    // Show InitScreen if URI is missing or invalid
    if (startupUri == null) {
        Log.w("PomoloBeeApp", "❌ startupUri is null → launching InitScreen")

        InitScreen(
            settingsViewModel = settingsViewModel,
            orchardViewModel = orchardViewModel,
            onInitFinished = {
                Log.i("PomoloBeeApp", "🎉 Init finished → recreating activity")
                (context as? ComponentActivity)?.recreate()
            }
        )
        return
    }

    // Load config if needed
    LaunchedEffect(startupUri) {
        delay(100) // Let Compose settle
        Log.i("PomoloBeeApp", "🚀 Attempting to load config from: $startupUri")

        if (OrchardCache.locations.isEmpty()) {
            Log.i("PomoloBeeApp", "📦 Cache empty → Loading config + copying assets")
            copyAssetsIfNotExists(context, startupUri)
            orchardViewModel.loadLocalConfig(startupUri, context)
            settingsViewModel.invalidate()
            Log.i("PomoloBeeApp", "✅ Config and assets loaded")
        } else {
            Log.d("PomoloBeeApp", "👍 Cache already initialized — skipping config load")
        }
    }

    Log.i("PomoloBeeApp", "🧭 Launching main app UI")

    AppScaffold(
        navController = navController,
        orchardViewModel = orchardViewModel,
        settingsViewModel = settingsViewModel
    )
}
