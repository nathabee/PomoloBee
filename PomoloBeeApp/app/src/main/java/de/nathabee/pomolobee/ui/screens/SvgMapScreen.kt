package de.nathabee.pomolobee.ui.screens

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

import de.nathabee.pomolobee.cache.OrchardCache
import de.nathabee.pomolobee.model.Location
import de.nathabee.pomolobee.model.Row
import de.nathabee.pomolobee.viewmodel.SettingsViewModel
import de.nathabee.pomolobee.viewmodel.SettingsViewModelFactory
import java.io.File

@Composable
fun SvgMapScreen(
    location: Location,
    onRawSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(context))
    val rootPath by viewModel.effectiveStorageRoot.collectAsState()

    val svgFilePath = remember(rootPath, location) {
        val fileName = location.field.svgMapUrl?.substringAfterLast("/") ?: "default_map.svg"
        File("$rootPath/fields/svg/$fileName").takeIf { it.exists() }
            ?: File("$rootPath/fields/svg/default_map.svg")
    }

    var selectedRowInfo by remember { mutableStateOf<Row?>(null) }
    val fruitName = selectedRowInfo?.let { OrchardCache.fruits.find { f -> f.fruitId == it.fruitId }?.name }

    Column(modifier = Modifier.fillMaxSize()) {
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
                            <object id="svg" type="image/svg+xml" data="file://${svgFilePath.absolutePath}"></object>
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
