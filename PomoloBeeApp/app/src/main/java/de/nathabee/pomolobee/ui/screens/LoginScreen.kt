/**
 * LOGIN SCREEN (Planned for Phase 2)
 * 
 * Description:
 * This screen will handle user authentication using a username and password.
 * 
 * Planned Features:
 * - Login form (username/password)
 * - Auth error handling
 * - Store auth token locally
 * - Navigate to main app if login succeeds
 * 
 * Integration:
 * - Will use Django Token or Session Auth via Retrofit
 * - Optional: Add "Remember me" checkbox and logout logic in SettingsScreen
 * 
 * Not needed in Phase 1 – this is a placeholder for planning purposes.
 */
package de.nathabee.pomolobee.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize

@Composable
fun LoginScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("LOGIN SCREEN (Planned for Phase 2) - Work in Progress")
    }
}
