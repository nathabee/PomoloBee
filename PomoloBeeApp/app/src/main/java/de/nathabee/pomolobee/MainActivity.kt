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
import kotlinx.coroutines.delay



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
        val persisted = contentResolver.persistedUriPermissions
        persisted.forEach {
            Log.d("PersistedPermission", "URI=${it.uri}, Read=${it.isReadPermission}, Write=${it.isWritePermission}")
        }

        val prefs = UserPreferences(this)
        val rawUri = runBlocking { prefs.getRawStorageRoot().first() }
        val parsedUri = rawUri?.let { Uri.parse(it) }

        // 👇 Add detailed logging here
        if (parsedUri != null) {
            if (!hasAccessToUri(this, parsedUri)) {
                val rootDoc = DocumentFile.fromTreeUri(this, parsedUri)
                if (rootDoc == null || !rootDoc.exists()) {
                    Log.w("Startup", "⚠️ Storage root is inaccessible or missing — SD card may be removed or permission revoked.")
                    // Don't reset the preference — just inform the user later via UI if needed
                }
            } else {
                Log.i("Startup", "✅ Verified access to storage root: $parsedUri")
            }
        }

        val startupUri = parsedUri?.takeIf { hasAccessToUri(this, it) }

        Log.d("MainActivity", "📦 Loaded startupUri = $startupUri")

        setContent {
            PomoloBeeTheme {
                PomoloBeeApp(startupUri = startupUri)
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

    var resolvedUri by remember { mutableStateOf<Uri?>(startupUri) }

    Log.d("PomoloBeeApp", "📦 Initial startupUri = $resolvedUri")

    if (resolvedUri == null) {
        OrchardCache.clear()
        InitScreen(
            settingsViewModel = viewModel(factory = SettingsViewModelFactory(context)),
            orchardViewModel = viewModel(factory = OrchardViewModelFactory(context)),
            onInitFinished = { newUri ->
                Log.i("PomoloBeeApp", "🎉 Init finished → updating state instead of recreating")
                resolvedUri = newUri
            }
        )
        return
    }

    // ✅ Create ViewModels *after* init
    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(context))
    val orchardViewModel: OrchardViewModel = viewModel(factory = OrchardViewModelFactory(context))

    LaunchedEffect(resolvedUri) {
        delay(100)
        Log.i("PomoloBeeApp", "🚀 Attempting to load config from: $resolvedUri")
        if (OrchardCache.locations.isEmpty()) {
            copyAssetsIfNotExists(context, resolvedUri!!)
            orchardViewModel.loadLocalConfig(resolvedUri!!, context)
            settingsViewModel.invalidate()
            Log.i("PomoloBeeApp", "✅ Config and assets loaded")
        } else {
            Log.d("PomoloBeeApp", "👍 Cache already initialized — skipping config load")
        }
    }

    AppScaffold(
        navController = navController,
        orchardViewModel = orchardViewModel,
        settingsViewModel = settingsViewModel
    )
}
