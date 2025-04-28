package de.nathabee.pomolobee.ui.screens

import PomolobeeViewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.nathabee.pomolobee.model.Location
import de.nathabee.pomolobee.viewmodel.OrchardViewModel
import de.nathabee.pomolobee.navigation.Screen
import de.nathabee.pomolobee.ui.component.FieldCard

@Composable
fun OrchardScreen(
    navController: NavController,
    sharedViewModels: PomolobeeViewModels
) {
    val orchardViewModel = sharedViewModels.orchard

    val locations by orchardViewModel.locations.collectAsState()

    // 🧠 Fetch fruits (from viewmodel or cache)
    val fruits = remember { de.nathabee.pomolobee.cache.OrchardCache.fruits }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(locations) { location ->
            FieldCard(
                location = location,
                fruits = fruits,
                onVisualize = {
                    navController.navigate(
                        Screen.SvgMap.withArgs(
                            "fieldId" to location.field.fieldId.toString(),
                            "returnKey" to "unused"
                        )
                    )
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
