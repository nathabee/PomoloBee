package de.nathabee.pomolobee.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.nathabee.pomolobee.navigation.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun DrawerMenu(
    scope: CoroutineScope,
    drawerState: DrawerState,
    onNavigate: (String) -> Unit
) {
    val menuItems = listOf(
        Screen.Home, Screen.Camera, Screen.Settings, Screen.About
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "PomoloBee",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF6200EE),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Divider()

        menuItems.forEach { screen ->
            Text(
                text = screen.title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        scope.launch { drawerState.close() }
                        onNavigate(screen.route)
                    }
                    .padding(12.dp)
            )
        }
    }
}
