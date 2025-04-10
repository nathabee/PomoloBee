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
import de.nathabee.pomolobee.model.Fruit
import de.nathabee.pomolobee.util.getBackgroundUriForLocation
import de.nathabee.pomolobee.util.getSvgUriForLocation
import de.nathabee.pomolobee.viewmodel.OrchardViewModel
import de.nathabee.pomolobee.viewmodel.SettingsViewModel

fun injectBase64Image(svg: String, base64Image: String): String {
    val base64Tag = """
        <image x="0" y="0" width="1599" height="978"
               opacity="0.4" preserveAspectRatio="none"
               xlink:href="data:image/jpeg;base64,$base64Image"
               xmlns:xlink="http://www.w3.org/1999/xlink" />
    """.trimIndent()

    val cleanedSvg = svg.replace(Regex("""<svg:image[^>]+/>"""), "")
    return cleanedSvg.replace("</svg>", "$base64Tag\n</svg>")
}


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
    //val fruits by orchardViewModel.fruits.collectAsState()



    val svgUri = remember(storageRootUri, location) {
        storageRootUri?.let { getSvgUriForLocation(context, it, location) }
    }

    val svgContent = remember(svgUri) {
        svgUri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    var rawSvg = inputStream.bufferedReader().readText()

                    // Inject image based on shortName like "C1"
                    // val shortName = location.field.shortName
                    val backgroundUri = getBackgroundUriForLocation(context, storageRootUri!!, location)
                    if (backgroundUri != null) {
                        val base64Image = context.contentResolver.openInputStream(backgroundUri)?.use { input ->
                            android.util.Base64.encodeToString(input.readBytes(), android.util.Base64.NO_WRAP)
                        }

                        if (base64Image != null) {
                            rawSvg = injectBase64Image( rawSvg, base64Image)
                        }
                    }


                    rawSvg
                }
            } catch (e: Exception) {
                Log.e("SvgMapScreen", "❌ Error reading SVG file: ${e.message}")
                null
            }
        }
    }




    var showFruitInfo by remember { mutableStateOf(false) }
    var selectedRowInfo by remember { mutableStateOf<Row?>(null) }

    val fruits by orchardViewModel.fruits.collectAsState()



    val selectedFruit by remember(selectedRowInfo, fruits) {
        derivedStateOf {
            selectedRowInfo?.let { row ->
                fruits.find { it.fruitId == row.fruitId }
            }
        }
    }




    LaunchedEffect(selectedRowInfo, fruits) {
        if (selectedRowInfo != null) {
            Log.d("SvgMapScreen", "🍏 Row selected: ID=${selectedRowInfo!!.rowId}, fruitId=${selectedRowInfo!!.fruitId}")

            Log.d("SvgMapScreen", "🍎 All available fruits (${fruits.size}):")
            fruits.forEach { fruit ->
                Log.d("SvgMapScreen", "  • ID=${fruit.fruitId}, Name=${fruit.name}")
            }

            val matchedFruit = fruits.find { it.fruitId == selectedRowInfo!!.fruitId }
            Log.d("SvgMapScreen", "✅ Match result: ${matchedFruit?.name ?: "❌ No match found!"}")
        } else {
            Log.d("SvgMapScreen", "ℹ️ No row selected yet.")
            Log.d("SvgMapScreen", "🍎 Fruits at this point (${fruits.size}):")
            fruits.forEach { fruit ->
                Log.d("SvgMapScreen", "  • ID=${fruit.fruitId}, Name=${fruit.name}")
            }
        }
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
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        isVerticalScrollBarEnabled = true
                        isHorizontalScrollBarEnabled = true

                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onRowClicked(rowId: String) {
                                Log.d("SvgMapScreen", "🖱️ Row clicked: $rowId")
                                //val id = rowId.removePrefix("row_").toIntOrNull()
                                Log.d("SvgMapScreen", "🖱️ Row clicked: $rowId, resolved to ID = $id")

                                Log.d("SvgMapScreen", "📋 Rows in location: ${location.rows.map { it.rowId }}")

                                val id = rowId.removePrefix("row_").toIntOrNull()
                                val row = location.rows.find { it.rowId == id }
                                if (row != null) {
                                    this@apply.post {
                                        selectedRowInfo = row
                                        Log.d("SvgMapScreen", "📦 set selectedRowInfo: rowId=${row.rowId}, fruitId=${row.fruitId}")
                                    }
                                } else {
                                    Log.w("SvgMapScreen", "❌ Row not found for id: $id")
                                }
                            }
                        }, "Android")

                        val htmlContent = """
<html>
<head>
    <style>
        svg {
            width: 100vw;
            height: auto;
            max-width: 100%;
            display: block;
        }
        body {
            margin: 0;
            padding: 0;
            overflow: scroll;
        }
    </style>
</head>
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

        Log.d("SvgMapScreen", "🔍 Button state → selectedFruit=${selectedFruit?.name}")
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
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        onRawSelected("row_${row.rowId}")
                        selectedRowInfo = null
                    }) {
                        Text("✅ OK")
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { selectedRowInfo = null }) {
                            Text("❌ Cancel")
                        }
                        TextButton(
                            onClick = { showFruitInfo = true },
                            enabled = selectedFruit != null && fruits.isNotEmpty()

                        ) {
                            Text("🍏 Info Fruit")
                        }

                    }
                }
            )
        }


        Spacer(modifier = Modifier.height(8.dp))
    }



    if (showFruitInfo) {
        selectedFruit?.let { fruit ->
            AlertDialog(
                onDismissRequest = { showFruitInfo = false },
                title = { Text("🍏 ${fruit.name}") },
                text = {
                    Column {
                        Text("📄 ${fruit.description}")
                        Text("📅 Harvest: ${fruit.yieldStartDate} to ${fruit.yieldEndDate}")
                        Text("📦 Avg Yield: ${fruit.yieldAvgKg} kg")
                        Text("🍎 Avg Fruit: ${fruit.fruitAvgKg} kg")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showFruitInfo = false }) {
                        Text("✅ Close")
                    }
                }
            )
        }
    }








    Spacer(modifier = Modifier.height(8.dp))

    Button(onClick = onBack) {
        Text("⬅ Back")
    }


}
