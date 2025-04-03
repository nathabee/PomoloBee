package de.nathabee.pomolobee.ui.screens

import android.util.Log
import android.widget.ImageView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.JavascriptInterface
import androidx.documentfile.provider.DocumentFile

import de.nathabee.pomolobee.cache.OrchardCache
import de.nathabee.pomolobee.model.Location
import de.nathabee.pomolobee.model.Row
import de.nathabee.pomolobee.viewmodel.SettingsViewModel
import de.nathabee.pomolobee.viewmodel.SettingsViewModelFactory
import java.io.File
import de.nathabee.pomolobee.util.getSvgUriForLocation

@Composable
fun SvgMapScreen(
    location: Location,
    onRawSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(context))


    val storageRootUri by viewModel.storageRootUri.collectAsState()
    val svgUri = remember(storageRootUri, location) {
        storageRootUri?.let { getSvgUriForLocation(context, it, location) }
    }
    LaunchedEffect(svgUri) {
        Log.d("SvgMapScreen", "📍 SVG URI resolved to: $svgUri")
    }
    if (svgUri == null) {
        Log.e("SvgMapScreen", "❌ SVG URI is null — check if svgMapUrl is missing or file not found.")
    }
    Log.d("SvgMapScreen", "🗺 svgMapUrl from location = ${location.field.svgMapUrl}")




    var selectedRowInfo by remember { mutableStateOf<Row?>(null) }
    val fruitName = selectedRowInfo?.let { OrchardCache.fruits.find { f -> f.fruitId == it.fruitId }?.name }

    Column(modifier = Modifier.fillMaxSize()) {
        if (svgUri != null) {
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

                        val svgHtml = "data=\"${svgUri}\""

                        val htmlContent = """
                        <html>
                        <body>
                            <object id="svg" type="image/svg+xml" $svgHtml></object>
                            <script>
                                document.addEventListener("DOMContentLoaded", function() {
                                    const embed = document.getElementById("svg");
                                    embed.addEventListener("load", function() {
                                        const svgDoc = embed.contentDocument;
                                        const paths = svgDoc.querySelectorAll("path[id^='row_']");
                                        paths.forEach(function(p) {
                                            p.style.stroke = "blue";
                                            p.addEventListener("click", function() {
                                                Android.onRowClicked(p.id);
                                            });
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
            // Show a message or loader while waiting for svgUri to be available
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = androidx.compose.ui.Alignment.Center
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
