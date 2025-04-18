package de.nathabee.pomolobee.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import de.nathabee.pomolobee.model.Location
import de.nathabee.pomolobee.model.Row
import de.nathabee.pomolobee.navigation.Screen
import de.nathabee.pomolobee.ui.components.ExposedDropdownMenuBoxWithLabel
import de.nathabee.pomolobee.viewmodel.ImageViewModel
import de.nathabee.pomolobee.viewmodel.OrchardViewModel
import de.nathabee.pomolobee.viewmodel.SettingsViewModel

@Composable
fun LocationScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel,
    orchardViewModel: OrchardViewModel,
    imageViewModel: ImageViewModel
)
{
    val context = LocalContext.current

    val locations by orchardViewModel.locations.collectAsState()
    val fruits by orchardViewModel.fruits.collectAsState()

    // ✅ To receive data from SvgMapScreen
    val svgReturnKey = "fromSvg_Location"
    val receiveHandle = navController.currentBackStackEntry?.savedStateHandle

// ✅ To return data to CameraScreen
    val cameraReturnKey = "fromLocation"
    val sendHandle = navController.previousBackStackEntry?.savedStateHandle




    var selectedLocation by remember { mutableStateOf<Location?>(null) }
    var selectedRow by remember { mutableStateOf<Row?>(null) }

    val selectedRowId by settingsViewModel.selectedRowId.collectAsState()
    val selectedFieldId by settingsViewModel.selectedFieldId.collectAsState()

    val rows = selectedLocation?.rows ?: emptyList()


    LaunchedEffect(locations) {
        println("🌍 Locations loaded: ${locations.size}")
        println("🍎 Fruits loaded: ${fruits.size}")

        locations.forEach { loc ->
            println("📦 Field: ${loc.field.name} — Rows: ${loc.rows.size}")
            loc.rows.forEach { row ->
                println("  🌿 Row: ${row.name}, fruitId=${row.fruitId}")
            }
        }
        println("🍎 Fruits loaded: ${fruits.size}")
        fruits.forEach { f ->
            println("  🍏 Fruit ID=${f.fruitId}, Name=${f.name}")
        }
    }


    // Handle row coming from SVG Map
    LaunchedEffect(receiveHandle?.get<Int>("${svgReturnKey}_rowId")) {
        val rowId = receiveHandle?.get<Int>("${svgReturnKey}_rowId")
        val xy = receiveHandle?.get<String>("${svgReturnKey}_xy")

        if (rowId != null) {
            selectedLocation = locations.find { it.rows.any { row -> row.rowId == rowId } }
            selectedRow = selectedLocation?.rows?.find { it.rowId == rowId }

            settingsViewModel.updateSelectedRow(rowId)
            selectedLocation?.field?.fieldId?.let {
                settingsViewModel.updateSelectedField(it)
            }

            // Optionally store XY
            xy?.let { imageViewModel.setPendingXYLocation(it) }

            // Clean up
            receiveHandle.remove<Int>("${svgReturnKey}_rowId")
            receiveHandle.remove<String>("${svgReturnKey}_xy")
        }

    }

    // Send row back to CameraScreen
    Button(
        onClick = {
            selectedRow?.let { sendHandle?.set("${cameraReturnKey}_rowId", it) }
            imageViewModel.pendingXYLocation.value?.let { xy ->
                sendHandle?.set("${cameraReturnKey}_xy", xy)
            }
            navController.popBackStack()
        },
        enabled = selectedLocation != null && selectedRow != null
    ) {
        Text("✅ Confirm & Continue")
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 🌱 Field Dropdown
        ExposedDropdownMenuBoxWithLabel(
            label = "🌱 Field",
            items = locations.map { it.field.name },
            selectedItem = selectedLocation?.field?.name,
            onItemSelected = { name ->
                selectedLocation = locations.find { it.field.name == name }
                selectedRow = null // reset row when field changes

                println("🔄 Selected Location: ${selectedLocation?.field?.name}")
                selectedLocation?.rows?.forEach { row ->
                    val fruitMatch = fruits.find { it.fruitId == row.fruitId }
                    println("  🔍 Row '${row.name}' → Fruit match: ${fruitMatch?.name ?: "❌ Not found"}")
                }
            }

        )

        // 🔄 Sync field to preferences
        LaunchedEffect(selectedLocation?.field?.fieldId) {
            selectedLocation?.field?.fieldId?.let { settingsViewModel.updateSelectedField(it) }
        }

        // 🔁 Restore location from stored fieldId

        LaunchedEffect(selectedFieldId) {
            if (selectedFieldId != null) {
                selectedLocation = locations.find { it.field.fieldId == selectedFieldId }
            }
        }




        // 🌿 Row Dropdown (only after field selected)
        if (selectedLocation != null) {
            ExposedDropdownMenuBoxWithLabel(
                label = "🌿 Row",
                items = rows.map { it.name },
                selectedItem = selectedRow?.name,
                onItemSelected = { rowName ->
                    selectedRow = rows.find { it.name == rowName }
                    selectedRow?.rowId?.let { settingsViewModel.updateSelectedRow(it) }
                }
            )
        }

        // 🧠 Restore selected row after SVG map or reload
        LaunchedEffect(selectedRowId, selectedLocation) {
            if (selectedRowId != null && selectedLocation != null) {
                selectedRow = selectedLocation!!.rows.find { it.rowId == selectedRowId }
            }
        }

        // 📝 Status
        Text("📌 Status: ${
            if (selectedLocation != null && selectedRow != null)
                "✅ ${selectedLocation?.field?.name ?: "?"} / ${selectedRow!!.shortName}"
            else
                "❌ No location selected"
        }")

        // 🗺️ SVG Map Button
        if (selectedLocation != null) {
            Button(onClick = {
                // navController.navigate(Screen.SvgMap.createRoute(selectedLocation!!.field.fieldId))
                navController.navigate(
                    Screen.SvgMap.withArgs(
                        "fieldId" to selectedLocation!!.field.fieldId.toString(),
                        "returnKey" to svgReturnKey
                    )
                )



            }) {
                Text("🗺️ Select from Map")
            }
        }

        // ✅ Confirm Button
        Button(
            onClick = {
                println("✅ Field: ${selectedLocation?.field?.name}, Row: ${selectedRow?.name}")
                navController.popBackStack()
            },
            enabled = selectedLocation != null && selectedRow != null
        ) {
            Text("✅ Confirm & Continue")
        }
    }
}
