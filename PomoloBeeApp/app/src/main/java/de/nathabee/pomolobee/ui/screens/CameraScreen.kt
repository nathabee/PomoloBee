package de.nathabee.pomolobee.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import de.nathabee.pomolobee.ui.components.CameraView
import org.opencv.android.OpenCVLoader

@Composable
fun CameraScreen(navController: NavController) {
    val context = LocalContext.current
    val openCvLoaded = remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        OpenCVLoader.initDebug().also {
            openCvLoaded.value = it
            Log.d("CameraScreen", "OpenCV Loaded: $it")
        }
        onDispose { }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (openCvLoaded.value) {
            CameraView(context = context, modifier = Modifier.fillMaxSize())
        } else {
            Text("Loading OpenCV...", fontSize = 20.sp)
        }
    }
}
