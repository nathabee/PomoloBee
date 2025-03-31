package de.nathabee.pomolobee.ui.screens

import android.widget.ImageView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.load
import de.nathabee.pomolobee.model.Location
import java.io.File


fun getSvgFileFromUrl(svgMapUrl: String?): File {
    val filename = svgMapUrl?.substringAfterLast("/") ?: "default_map.svg"
    return File("/sdcard/PomoloBee/fields/svg", filename)
}


fun getBackgroundFileFromUrl(backgroundUrl: String?): File? {
    if (backgroundUrl == null) return null
    val filename = backgroundUrl.substringAfterLast("/")
    return File("/sdcard/PomoloBee/fields/background", filename)
}


@Composable
fun SvgMapScreen(
    location: Location,
    onRawSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val svgFile = getSvgFileFromUrl(location.field.svgMapUrl)
    if (!svgFile.exists()) {
        // fallback to default
        File("/sdcard/PomoloBee/fields/svg/default_map.svg")
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

                // TODO: Real row detection logic with touch on SVG paths
                svgView.setOnClickListener {
                    // Simulated for now
                    onRawSelected("row_4")
                }

                svgView
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBack) {
            Text("Back")
        }
    }
}
