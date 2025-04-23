// ImageRepository.kt	Interface between ViewModels and file/network logic. Handles: saving, listing, uploading, deleting, syncing images.


package de.nathabee.pomolobee.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.google.gson.Gson
import de.nathabee.pomolobee.cache.OrchardCache
import de.nathabee.pomolobee.model.EstimationResponse
import de.nathabee.pomolobee.model.ImageListResponse
import de.nathabee.pomolobee.util.ErrorLogger


object ImageRepository {

    fun loadAllImageDataFromUri(context: Context, rootUri: Uri): Boolean {
        Log.d("ImageRepo", "🚀 Starting image + estimation load from rootUri = $rootUri")

        return try {
            val rootDoc = DocumentFile.fromTreeUri(context, rootUri)
            if (rootDoc == null) {
                ErrorLogger.logError(
                    context,
                    rootUri,
                    "❌ Failed to create DocumentFile from rootUri"
                )
                return false
            }

            val imageDataDir = rootDoc.findFile("image_data")
            if (imageDataDir == null || !imageDataDir.isDirectory) {
                ErrorLogger.logError(
                    context,
                    rootUri,
                    "❌ 'image_data' folder not found under $rootUri"
                )
                return false
            }

            val imagesFile = imageDataDir.findFile("images.json")
            val estimationsFile = imageDataDir.findFile("estimations.json")

            if (imagesFile == null) {
                ErrorLogger.logError(
                    context,
                    rootUri,
                    "❌ images.json not found in image_data directory"
                )
            } else {
                Log.d("ImageRepo", "📄 Found images.json at: ${imagesFile.uri}")
            }

            if (estimationsFile == null) {
                ErrorLogger.logError(
                    context,
                    rootUri,
                    "❌ estimations.json not found in image_data directory"
                )
            } else {
                Log.d("ImageRepo", "📄 Found estimations.json at: ${estimationsFile.uri}")
            }

            if (imagesFile == null || estimationsFile == null) {
                return false
            }

            val imagesJson = context.contentResolver.openInputStream(imagesFile.uri)
                ?.bufferedReader().use { it?.readText() }

            val estimationsJson = context.contentResolver.openInputStream(estimationsFile.uri)
                ?.bufferedReader().use { it?.readText() }

            if (imagesJson == null || estimationsJson == null) {
                ErrorLogger.logError(
                    context,
                    rootUri,
                    "❌ Failed to read images.json or estimations.json"
                )
                return false
            }

            val imageResponse = Gson().fromJson(imagesJson, ImageListResponse::class.java)
            val imageList = imageResponse?.data?.images ?: emptyList()


            val estimationResponse = Gson().fromJson(estimationsJson, EstimationResponse::class.java)
            val estimationList = estimationResponse?.data?.estimations ?: emptyList()

            Log.d("ImageRepo", "🖼️ Loaded ${imageList.size} image records")
            Log.d("ImageRepo", "📊 Loaded ${estimationList.size} estimation records")

            OrchardCache.loadImages(imageList)
            OrchardCache.loadEstimations(estimationList)


            Log.d("ImageRepo", "🎉 Image and estimation data successfully cached")

            val pendingImagesFile = imageDataDir.findFile("pending_images.json")
            if (pendingImagesFile == null) {
                Log.w("ImageRepo", "⚠️ pending_images.json not found — skipping")
            } else {
                Log.d("ImageRepo", "📄 Found pending_images.json at: ${pendingImagesFile.uri}")
                val pendingJson = context.contentResolver.openInputStream(pendingImagesFile.uri)
                    ?.bufferedReader().use { it?.readText() }
                if (pendingJson != null) {
                    val pendingList = try {
                        Gson().fromJson(pendingJson, ImageListResponse::class.java).data.images
                    } catch (e: Exception) {
                        ErrorLogger.logError(context, rootUri, "❌ Failed to parse pending_images.json", e)
                        emptyList()
                    }

                    OrchardCache.loadPendingImages(pendingList)
                    Log.d("ImageRepo", "🕒 Loaded ${pendingList.size} pending images")
                }
            }

            true
        } catch (e: Exception) {
            ErrorLogger.logError(
                context,
                rootUri,
                "💥 Exception while loading image data from Uri",
                e
            )
            false
        }
    }


}

