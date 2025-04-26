package de.nathabee.pomolobee.ui.component

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import coil.compose.rememberAsyncImagePainter
import de.nathabee.pomolobee.model.ImageRecord
import de.nathabee.pomolobee.navigation.Screen
import de.nathabee.pomolobee.network.ImageApiService
import de.nathabee.pomolobee.util.StorageUtils
import de.nathabee.pomolobee.util.parseXYLocation
import kotlinx.coroutines.launch


@Composable
fun ImageCard(
    image: ImageRecord,
    rootUri : Uri?,
    imagesDir: DocumentFile?,
    mediaUrl: String,
    isCloudMode: Boolean = false,
    onPreview: (ImageRecord) -> Unit,
    onAnalyze: (ImageRecord) -> Unit,
    onDelete: (ImageRecord) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

/*
    val (imageUri, hasLocalImage) = remember(image, imagesDir, mediaUrl) {
        var localUri: Uri? = null
        var localFound = false

        val fallbackFilename = "image_default.jpg"
        val filename = image.originalFilename?.takeIf { it.isNotBlank() } ?: fallbackFilename

        val file = imagesDir?.findFile(filename)

        if (file != null && file.exists()) {
            Log.d("ImageCard", "🗂️ Using local image: ${file.uri}")
            localUri = file.uri
            localFound = true
        } else {
            Log.w("ImageCard", "❌ Local file not found: $filename")
            // Optionally, trigger cloud fetch if filename == fallback and cloud mode is on (you handle this later)
        }


        // If not found locally, try remote
        if (localUri == null && !image.imageUrl.isNullOrBlank() && image.imageId != null) {
            val sanitizedPath = image.imageUrl.removePrefix("/media") // ✅ Remove duplicate
            val fullUrl = mediaUrl.trimEnd('/') + sanitizedPath
            Log.d("ImageCard", "🌐 Using remote image: $fullUrl")
            return@remember fullUrl to false
        }


        return@remember localUri to localFound
    }
*/
    val (imageUri, hasLocalImage) = remember(image, imagesDir, mediaUrl) {
        var localUri: Uri? = null
        var localFound = false

        val fallbackFilename = "image_default.jpg"
        val filename = image.originalFilename?.takeIf { it.isNotBlank() } ?: fallbackFilename

        val file = imagesDir?.findFile(filename)

        if (file != null && file.exists()) {
            localUri = file.uri
            localFound = true
        } else {
            // check if imageUrl is a local Uri already
            if (!image.imageUrl.isNullOrBlank() && (image.imageUrl.startsWith("content://") || image.imageUrl.startsWith("file://"))) {
                Log.d("ImageCard", "📸 Using temporary local Uri: ${image.imageUrl}")
                return@remember Uri.parse(image.imageUrl) to true
            }

            if (!image.imageUrl.isNullOrBlank() && image.imageId != null) {
                val sanitizedPath = image.imageUrl.removePrefix("/media")
                val fullUrl = mediaUrl.trimEnd('/') + sanitizedPath
                Log.d("ImageCard", "🌐 Using remote image: $fullUrl")
                return@remember fullUrl to false
            }
        }

        return@remember localUri to localFound
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            // Image thumbnail
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Image thumbnail",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("📅 ${image.date}", style = MaterialTheme.typography.labelMedium)
                Text("🧪 Status: ${image.status}", style = MaterialTheme.typography.labelSmall)
                Text("🌿 ${image.fruitType}", style = MaterialTheme.typography.bodyMedium)

                image.xyLocation?.let {
                    val coords = parseXYLocation(it)
                    coords?.let {
                        Text("📍 Location: (${(it.x * 100).toInt()}%, ${(it.y * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { onPreview(image) }) { Text("Preview") }
                    TextButton(onClick = { onAnalyze(image) }) { Text("Analyze") }
                    TextButton(onClick = { onDelete(image) }) {
                        Text("Delete", color = Color.Red)
                    }
                }

                if (!hasLocalImage && isCloudMode && image.imageId != null && rootUri != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                ImageApiService.fetchImageFromCloud(
                                    context = context,
                                    rootUri = rootUri,
                                    mediaUrl = mediaUrl,
                                    imageUrl = image.imageUrl.removePrefix("/media"),
                                    filename = image.originalFilename ?: "image-${image.imageId}.jpg"
                                )
                            }
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text("☁️ Retrieve from Cloud")
                    }
                }

            }
        }
    }
}
