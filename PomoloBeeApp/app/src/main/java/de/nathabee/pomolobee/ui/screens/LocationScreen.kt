package de.nathabee.pomolobee.ui.screens

import PomolobeeViewModels
import android.widget.Toast
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
    sharedViewModels: PomolobeeViewModels
)
{
    val context = LocalContext.current
    val orchardViewModel = sharedViewModels.orchard
    val cameraViewModel = sharedViewModels.camera



    // ✅ To receive data from SvgMapScreen
    val svgReturnKey = "fromSvg_Location"
    val receiveHandle = navController.currentBackStackEntry?.savedStateHandle



    val tempImageRecord by cameraViewModel.tempImageRecord.collectAsState()
    val locations by orchardViewModel.locations.collectAsState()
    val fruits by orchardViewModel.fruits.collectAsState()

    val selectedLocation = remember(tempImageRecord, locations) {
        locations.find { it.field.fieldId == tempImageRecord.fieldId }
    }
    val selectedRow = remember(tempImageRecord, selectedLocation) {
        selectedLocation?.rows?.find { it.rowId == tempImageRecord.rowId }
    }

    val rows = selectedLocation?.rows ?: emptyList()




    // Handle row coming from SVG Map
    LaunchedEffect(receiveHandle?.get<Int>("${svgReturnKey}_rowId")) {
        val rowId = receiveHandle?.get<Int>("${svgReturnKey}_rowId")
        val xy = receiveHandle?.get<String>("${svgReturnKey}_xy")

        if (rowId == null) {
            return@LaunchedEffect // nothing selected -> keep previous state
        }

        val newLocation = locations.find { it.rows.any { row -> row.rowId == rowId } }
        val newRow = newLocation?.rows?.find { it.rowId == rowId }

        if (newLocation != null) {
            cameraViewModel.updateSelectedFieldAndRow(
                fieldId = newLocation.field.fieldId,
                rowId = newRow?.rowId,
                fieldShort = newLocation.field.shortName,
                rowShort = newRow?.shortName ?: "UnknownRow",
                fruitType = newRow?.fruitType
            )
        }

        xy?.let {
            cameraViewModel.updatePendingXYLocation(it)
        }

        receiveHandle.remove<Int>("${svgReturnKey}_rowId")
        receiveHandle.remove<String>("${svgReturnKey}_xy")
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
                val newLocation = locations.find { it.field.name == name }
                if (newLocation != null) {
                    cameraViewModel.updateSelectedFieldAndRow(
                        fieldId = newLocation.field.fieldId,
                        rowId = null, // Reset row
                        fieldShort = newLocation.field.shortName,
                        rowShort = "UnknownRow",
                        fruitType =  null
                    )
                    cameraViewModel.updatePendingXYLocation("")

                    /* cameraViewModel.setTempImageRecord(
                         tempImageRecord.copy(
                             fieldId = newLocation.field.fieldId,
                             rowId = -1, // reset selected row
                             fruitType = "Unbestimmt"
                         )
                     )

                     */
                }
            }
        )


        // 🔄 Sync field to preferences
        /*
        LaunchedEffect(selectedLocation?.field?.fieldId) {
            selectedLocation?.field?.fieldId?.let {
                //cameraViewModel.updateSelectedField(it)
                cameraViewModel.updateSelectedFieldAndRow(
                    fieldId = it ???.field.fieldId,
                    rowId = null, // Reset row
                    fieldShort = it  ??? .field.shortName,
                    rowShort = "UnknownRow",
                    fruitType =  it .field.fruitType
                )
            //  newLocation.field.fruitType
            }
        }*/



        // 🌿 Row Dropdown (only after field selected)
        if (selectedLocation != null) {
            ExposedDropdownMenuBoxWithLabel(
                label = "🌿 Row",
                items = rows.map { it.name },
                selectedItem = selectedRow?.name,
                onItemSelected = { rowName ->
                    val newRow = selectedLocation.rows.find { it.name == rowName }
                    if (newRow != null) {
                        cameraViewModel.updateSelectedFieldAndRow(
                            fieldId = selectedLocation.field.fieldId,
                            rowId = newRow.rowId,
                            fieldShort = selectedLocation.field.shortName,
                            rowShort = newRow.shortName,
                            fruitType = newRow.fruitType
                        )
                    }

                }
            )
        }


        // 🧠 Restore selected row after SVG map or reload
        /*
        LaunchedEffect(selectedRowId, selectedLocation) {
            if (selectedRowId != null && selectedLocation != null) {
                selectedRow = selectedLocation!!.rows.find { it.rowId == selectedRowId }
            }
        }

         */

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
                Toast.makeText(context, "✅ Location selected", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            },
            enabled = selectedLocation != null && selectedRow != null
        ) {
            Text("✅ Confirm & Continue")
        }



    }
}
