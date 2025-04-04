package de.nathabee.pomolobee.ui.screens

import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import de.nathabee.pomolobee.model.Location
import de.nathabee.pomolobee.model.Row
import de.nathabee.pomolobee.util.getSvgUriForLocation
import de.nathabee.pomolobee.viewmodel.OrchardViewModel
import de.nathabee.pomolobee.viewmodel.SettingsViewModel

@Composable
fun SvgMapScreen(
    location: Location,
    settingsViewModel: SettingsViewModel,
    orchardViewModel: OrchardViewModel,
    onRawSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val storageRootUri by settingsViewModel.storageRootUri.collectAsState()
    val fruits by orchardViewModel.fruits.collectAsState()

    val svgUri = remember(storageRootUri, location) {
        storageRootUri?.let { getSvgUriForLocation(context, it, location) }
    }

    val svgContent = remember(svgUri) {
        svgUri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    inputStream.bufferedReader().readText()
                }
            } catch (e: Exception) {
                Log.e("SvgMapScreen", "❌ Error reading SVG file: ${e.message}")
                null
            }
        }
    }

    var selectedRowInfo by remember { mutableStateOf<Row?>(null) }
    val fruitName = selectedRowInfo?.let { row ->
        fruits.find { it.fruitId == row.fruitId }?.name
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (svgContent != null) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onRowClicked(rowId: String) {
                                val id = rowId.removePrefix("row_").toIntOrNull()
                                val row = location.rows.find { it.rowId == id }
                                if (row != null) {
                                    this@apply.post {
                                        selectedRowInfo = row
                                    }
                                }
                            }
                        }, "Android")

                        val htmlContent = """
                            <html>
                            <body>
                                $svgContent
                                <script>
                                    document.addEventListener("DOMContentLoaded", function() {
                                        const paths = document.querySelectorAll("path[id^='row_']");
                                        paths.forEach(function(p) {
                                            p.style.stroke = "blue";
                                            p.addEventListener("click", function() {
                                                Android.onRowClicked(p.id);
                                            });
                                        });
                                    });
                                </script>
                            </body>
                            </html>
                        """.trimIndent()

                        loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                    }
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    selectedRowInfo?.let { row ->
        AlertDialog(
            onDismissRequest = { selectedRowInfo = null },
            title = { Text("Row Info") },
            text = {
                Column {
                    Text("🆔 Row ID: ${row.rowId}")
                    Text("🌿 Short Name: ${row.shortName}")
                    Text("🌱 Nb Plants: ${row.nbPlant}")
                    Text("🍏 Fruit Type: ${row.fruitType}")
                    Text("🍎 Fruit Name: $fruitName")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onRawSelected("row_${row.rowId}")
                    selectedRowInfo = null
                }) {
                    Text("✅ OK")
                }
            }
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Button(onClick = onBack) {
        Text("⬅ Back")
    }
}
