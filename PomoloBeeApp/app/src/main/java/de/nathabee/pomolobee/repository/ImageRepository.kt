// ImageRepository.kt	Interface between ViewModels and file/network logic. Handles: saving, listing, uploading, deleting, syncing images.


// fun updateImageStatus(fileName: String, newStatus: String, nbFruit: Int?, confidence: Double?)
// for maintaining pending_images.json?


package de.nathabee.pomolobee.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.google.gson.Gson
import de.nathabee.pomolobee.cache.OrchardCache
import de.nathabee.pomolobee.model.EstimationData
import de.nathabee.pomolobee.model.EstimationResponse
import de.nathabee.pomolobee.model.ImageListData
import de.nathabee.pomolobee.model.ImageListResponse

object ImageRepository {
    fun loadAllImageDataFromUri(context: Context, rootUri: Uri): Boolean {
        Log.d("ImageRepo", "🚀 Starting image + estimation load from rootUri = $rootUri")

        return try {
            val rootDoc = DocumentFile.fromTreeUri(context, rootUri)
            if (rootDoc == null) {
                Log.e("ImageRepo", "❌ Failed to create DocumentFile from rootUri")
                return false
            }

            val imageDataDir = rootDoc.findFile("image_data")
            if (imageDataDir == null || !imageDataDir.isDirectory) {
                Log.e("ImageRepo", "❌ 'image_data' folder not found under $rootUri")
                return false
            }

            val imagesFile = imageDataDir.findFile("images.json")
            val estimationsFile = imageDataDir.findFile("estimations.json")

            if (imagesFile == null) {
                Log.e("ImageRepo", "❌ images.json not found in image_data directory")
            } else {
                Log.d("ImageRepo", "📄 Found images.json at: ${imagesFile.uri}")
            }

            if (estimationsFile == null) {
                Log.e("ImageRepo", "❌ estimations.json not found in image_data directory")
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
                Log.e("ImageRepo", "❌ Failed to read images.json or estimations.json")
                return false
            }

            val imageList = Gson().fromJson(imagesJson, ImageListResponse::class.java).data.images
            val estimationList = Gson().fromJson(estimationsJson, EstimationResponse::class.java).data.estimations

            Log.d("ImageRepo", "🖼️ Loaded ${imageList.size} image records")
            Log.d("ImageRepo", "📊 Loaded ${estimationList.size} estimation records")

            OrchardCache.loadImages(imageList)
            OrchardCache.loadEstimations(estimationList)

            Log.d("ImageRepo", "🎉 Image and estimation data successfully cached")

            true
        } catch (e: Exception) {
            Log.e("ImageRepo", "💥 Exception while loading image data from Uri", e)
            false
        }
    }
}
