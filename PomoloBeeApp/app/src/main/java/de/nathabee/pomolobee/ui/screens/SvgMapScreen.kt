package de.nathabee.pomolobee.ui.screens

import PomolobeeViewModels
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
import androidx.navigation.NavController
import de.nathabee.pomolobee.model.Location
import de.nathabee.pomolobee.model.Row
import de.nathabee.pomolobee.util.StorageUtils
import de.nathabee.pomolobee.viewmodel.OrchardViewModel
import de.nathabee.pomolobee.viewmodel.SettingsViewModel

fun injectBase64Image(svg: String, base64Image: String): String {
    val tag = """
        <image x="0" y="0" width="100%" height="100%" opacity="0.4"
               preserveAspectRatio="none" xlink:href="data:image/jpeg;base64,$base64Image"
               style="pointer-events: none;" xmlns:xlink="http://www.w3.org/1999/xlink" />
    """.trimIndent()

    val svgTagEnd = Regex("""<svg[^>]*>""").find(svg)?.range?.lastOrNull()
    return if (svgTagEnd != null) {
        val insertPos = svgTagEnd + 1
        svg.substring(0, insertPos) + tag + svg.substring(insertPos)
    } else svg.replace("</svg>", "$tag</svg>")
}

@Composable
fun SvgMapScreen(
    location: Location,
    xyMarker: String? = null,      // ⬅ where to place the red cross (if not null)
    readOnly: Boolean = false,      // ⬅ disables interaction true :display location of an image , false : click to select a location
    sharedViewModels: PomolobeeViewModels,
    navController: NavController,
    returnKey: String
) {
    val context = LocalContext.current
    val orchardViewModel = sharedViewModels.orchard
    val settingsViewModel = sharedViewModels.settings


    val storageRootUri by settingsViewModel.storageRootUri.collectAsState()
    val fruits by orchardViewModel.fruits.collectAsState()

    var xySelected by remember { mutableStateOf<String?>(null) }
    var selectedRowInfo by remember { mutableStateOf<Row?>(null) }
    var showFruitInfo by remember { mutableStateOf(false) }



    val svgUri = remember(storageRootUri, location) {
        storageRootUri?.let { StorageUtils.getSvgUriForLocation(context, it, location) }
    }

    val svgContent = remember(svgUri) {
        svgUri?.let {
            runCatching {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    var rawSvg = stream.bufferedReader().readText()

                    val bgUri = StorageUtils.getBackgroundUriForLocation(context, storageRootUri!!, location)
                    val base64 = bgUri?.let { uri ->
                        context.contentResolver.openInputStream(uri)?.readBytes()
                            ?.let { bytes -> android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP) }
                    }

                    if (base64 != null) {
                        rawSvg = injectBase64Image(rawSvg, base64)
                    }

                    rawSvg
                }
            }.getOrElse {
                Log.e("SvgMapScreen", "❌ Error reading SVG: ${it.message}")
                null
            }
        }
    }

    val selectedFruit by remember(selectedRowInfo, fruits) {
        derivedStateOf {
            selectedRowInfo?.let { row -> fruits.find { it.fruitId == row.fruitId } }
        }
    }

    Column(Modifier.fillMaxSize()) {
        // 🖼️ SVG + image background
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
                            fun onRowClickedWithXY(rowId: String, xyLocation: String) {
                                val normalizedId = rowId.removePrefix("row_").removeSuffix("_hit")
                                val id = normalizedId.toIntOrNull()
                                val row = location.rows.find { it.rowId == id }

                                if (row != null) {
                                    this@apply.post {
                                        selectedRowInfo = row
                                        xySelected = xyLocation
                                        Log.d("SvgMapScreen", "✅ Selected row=${row.rowId} at $xyLocation")
                                    }
                                }
                            }
                        }, "Android")

                        val html = """
                            <html><head><style>
                                svg { width: 100vw; height: auto; max-width: 100%; display: block; }
                                body { margin: 0; padding: 0; overflow: scroll; }
                            </style></head>
                            <body>$svgContent
                                <script>
                                document.addEventListener("DOMContentLoaded", function() {
                                    const svg = document.querySelector("svg");
                                
                                    // ⛔ Skip interactive click handler in read-only mode
                                    ${if (!readOnly) """
                                    const paths = document.querySelectorAll("path[id^='row_']");
                                    paths.forEach(function(p) {
                                        p.style.stroke = "blue";
                                        p.addEventListener("click", function(event) {
                                            const rect = svg.getBoundingClientRect();
                                            const x = (event.clientX - rect.left) / rect.width;
                                            const y = (event.clientY - rect.top) / rect.height;
                                            const xy = JSON.stringify({ x: x, y: y });
                                            Android.onRowClickedWithXY(p.id, xy);
                                        });
                                    });
                                    """ else "// read-only mode, no interaction" }
                                
                                    // ➕ Draw red cross if xyMarker is provided
                                    ${xyMarker?.let {
                                        val coords = de.nathabee.pomolobee.util.parseXYLocation(it)
                            if (coords != null)
                                """ 
                                    // ADD THIS BLOCK INSTEAD OF THE OLD ONE
                                    const size = 10;
                                    const line1 = document.createElementNS("http://www.w3.org/2000/svg", "line");
                                    line1.setAttribute("x1", ${coords.x} * svg.viewBox.baseVal.width - size);
                                    line1.setAttribute("y1", ${coords.y} * svg.viewBox.baseVal.height - size);
                                    line1.setAttribute("x2", ${coords.x} * svg.viewBox.baseVal.width + size);
                                    line1.setAttribute("y2", ${coords.y} * svg.viewBox.baseVal.height + size);
                                    line1.setAttribute("stroke", "red");
                                    line1.setAttribute("stroke-width", "2");
                            
                                    const line2 = document.createElementNS("http://www.w3.org/2000/svg", "line");
                                    line2.setAttribute("x1", ${coords.x} * svg.viewBox.baseVal.width - size);
                                    line2.setAttribute("y1", ${coords.y} * svg.viewBox.baseVal.height + size);
                                    line2.setAttribute("x2", ${coords.x} * svg.viewBox.baseVal.width + size);
                                    line2.setAttribute("y2", ${coords.y} * svg.viewBox.baseVal.height - size);
                                    line2.setAttribute("stroke", "red");
                                    line2.setAttribute("stroke-width", "2");
                            
                                    svg.appendChild(line1);
                                    svg.appendChild(line2);
                                    """ else ""
                                                        }}
                                });
                                </script>

                                
                            </body></html>
                        """.trimIndent()

                        loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
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

        // 📋 Row Info Dialog
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
                        navController.previousBackStackEntry?.savedStateHandle?.apply {
                            set("${returnKey}_rowId", row.rowId)
                            set("${returnKey}_xy", xySelected)

                        }
                        navController.popBackStack()
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
                            enabled = selectedFruit != null
                        ) {
                            Text("🍏 Info Fruit")
                        }
                    }
                }
            )
        }

        // 🍏 Fruit Info Dialog
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

        Button(onClick = { navController.popBackStack() }) {
            Text("⬅ Back")
        }
    }
}
