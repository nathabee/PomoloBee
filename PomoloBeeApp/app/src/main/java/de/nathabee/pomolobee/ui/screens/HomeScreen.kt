package de.nathabee.pomolobee.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import de.nathabee.pomolobee.navigation.Screen

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to PomoloBee", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { navController.navigate(Screen.Camera.route) }) {
            Text("Open Camera")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = { navController.navigate(Screen.Settings.route) }) {
            Text("Settings")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = { navController.navigate(Screen.About.route) }) {
            Text("About")
        }
    }
}
