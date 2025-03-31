package de.nathabee.pomolobee

import android.content.Context
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import de.nathabee.pomolobee.navigation.NavGraph
import de.nathabee.pomolobee.repository.OrchardRepository
import de.nathabee.pomolobee.ui.components.DrawerMenu
import de.nathabee.pomolobee.ui.theme.PomoloBeeTheme
import kotlinx.coroutines.launch
import java.io.File

import android.Manifest

private val REQUIRED_PERMISSIONS = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

private const val REQUEST_CODE_PERMISSIONS = 123


class MainActivity : ComponentActivity() {

    //###########################################################################
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ask permissions
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }


        copyAssetsIfNotExists(this) // copy assets if not exists first install

        OrchardRepository.loadAllConfig() // 👈 parse & cache JSON files in OrchardCache

        // Load native OpenCV library
        System.loadLibrary("opencv_java4")

        setContent {
            PomoloBeeTheme {
                PomoloBeeApp()
            }
        }
    }


    //###########################################################################
// ✅ Now inside MainActivity
    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                this,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}

//###########################################################################
fun copyAssetsIfNotExists(context: Context) {
    val rootPath = "/sdcard/PomoloBee/"
    val assetManager = context.assets

    val assetStructure = listOf(
        "config/fruits.json",
        "config/locations.json",
        "fields/svg/C1_map.svg",
        "fields/svg/default_map.svg",
        "fields/background/C1.jpeg"
    )

    for (assetPath in assetStructure) {
        val destFile = File(rootPath + assetPath)
        if (!destFile.exists()) {
            destFile.parentFile?.mkdirs()
            assetManager.open(assetPath).use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}

//###########################################################################
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomoloBeeApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Run copy on first composition only
    LaunchedEffect(Unit) {
        copyAssetsIfNotExists(context)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerMenu(
                scope = scope,
                drawerState = drawerState
            ) { route ->
                navController.navigate(route)
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("PomoloBee") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open drawer")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                NavGraph(navController = navController)
            }
        }
    }


}
