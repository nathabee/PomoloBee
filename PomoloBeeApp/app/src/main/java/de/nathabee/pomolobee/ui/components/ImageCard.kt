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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import coil.compose.rememberAsyncImagePainter
import de.nathabee.pomolobee.model.Estimation
import de.nathabee.pomolobee.model.Fruit
import de.nathabee.pomolobee.model.ImageRecord
import de.nathabee.pomolobee.model.Location
import de.nathabee.pomolobee.navigation.Screen
import de.nathabee.pomolobee.network.ImageApiService
import de.nathabee.pomolobee.ui.preview.ImageEstimationProvider
import de.nathabee.pomolobee.util.StorageUtils
import de.nathabee.pomolobee.util.findEstimationForImage
import de.nathabee.pomolobee.util.parseXYLocation
import kotlinx.coroutines.launch


@Composable
fun ImageCard(
    image: ImageRecord,
    estimation: Estimation?,
    rootUri : Uri?,
    imagesDir: DocumentFile?,
    mediaUrl: String,
    isCloudMode: Boolean = false,
    onPreview: (ImageRecord) -> Unit,
    onAnalyze: (Estimation) -> Unit,
    onDelete: (ImageRecord) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()





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


                image.userFruitPlant?.let {
                    Text("🍏 Estimated fruit per plant: ${image.userFruitPlant}", style = MaterialTheme.typography.labelSmall)
                }

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
                    if (estimation != null) {
                        TextButton(onClick = { onAnalyze(estimation) }) { Text("Analyze") }
                    }



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




@Preview(showBackground = true)
@Composable
fun PreviewImageEstimationCard(
    @PreviewParameter(ImageEstimationProvider::class) data: Pair<ImageRecord, Estimation>
) {
    ImageCard(
        image = data.first,
        estimation = data.second,
        rootUri = null,
        imagesDir = null,
        mediaUrl = "http://192.168.178.71:8000/media",
        isCloudMode = false,
        onPreview = {},
        onAnalyze = { _ -> },   // <-- accept estimation but do nothing
        onDelete = {}
    )
}
