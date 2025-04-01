package de.nathabee.pomolobee.ui.screens

import android.content.Context
import android.widget.ImageView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.load
import de.nathabee.pomolobee.model.Location

import de.nathabee.pomolobee.data.UserPreferences
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

import java.io.File



fun getSvgFileFromUrl(svgMapUrl: String?, context: Context): File {
    val filename = svgMapUrl?.substringAfterLast("/") ?: "default_map.svg"
    val userPrefs = UserPreferences(context)

    val configDir = runBlocking {
        userPrefs.getConfigPath().first()
    }
    return File(File(configDir).parentFile?.parentFile, "fields/svg/$filename")

}


fun getBackgroundFileFromUrl(backgroundUrl: String?, context: Context): File? {
    if (backgroundUrl == null) return null
    val filename = backgroundUrl.substringAfterLast("/")
    val userPrefs = UserPreferences(context)

    val configDir = runBlocking {
        userPrefs.getConfigPath().first()
    }
    return File(File(configDir).parentFile?.parentFile, "fields/background/$filename")

}


@Composable
fun SvgMapScreen(
    location: Location,
    onRawSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val svgFileState = remember { mutableStateOf<File?>(null) }

    // Load the correct file path asynchronously
    LaunchedEffect(location.field.svgMapUrl) {
        val prefs = UserPreferences(context)
        val configDir = prefs.getConfigPath().first()
        val rootDir = File(configDir).parentFile?.parentFile

        val filename = location.field.svgMapUrl?.substringAfterLast("/") ?: "default_map.svg"
        val svgPath = File(rootDir, "fields/svg/$filename")

        svgFileState.value = if (svgPath.exists()) {
            svgPath
        } else {
            File(rootDir, "fields/svg/default_map.svg")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Tap on a row in: ${location.field.name}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        svgFileState.value?.let { svgFile ->
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                factory = { context ->
                    val svgView = ImageView(context)

                    svgView.load(svgFile) {
                        crossfade(true)
                        placeholder(android.R.drawable.ic_menu_gallery)
                        error(android.R.drawable.ic_delete)
                    }

                    svgView.setOnClickListener {
                        onRawSelected("row_4")
                    }

                    svgView
                }
            )
        } ?: run {
            Text("Loading map...")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBack) {
            Text("Back")
        }
    }
}
