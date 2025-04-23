//ImageApiService.kt	Retrofit interface: upload image, get status, fetch result, delete, retry, etc.
//
package de.nathabee.pomolobee.network

import android.content.Context
import android.net.Uri
import android.util.Log
import de.nathabee.pomolobee.util.ErrorLogger
import de.nathabee.pomolobee.util.StorageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object ImageApiService {

    suspend fun fetchImagesList(
        apiUrl: String,
        fieldId: Int? = null,
        rowId: Int? = null,
        date: String? = null,
        limit: Int = 100,
        offset: Int = 0
    ): String = withContext(Dispatchers.IO) {
        val baseUrl = "$apiUrl/images/list/"
        val queryParams = mutableListOf("limit=$limit", "offset=$offset")

        fieldId?.let { queryParams.add("field_id=$it") }
        rowId?.let { queryParams.add("row_id=$it") }
        date?.let { queryParams.add("date=${URLEncoder.encode(it, "UTF-8")}") }

        val fullUrl = "$baseUrl?${queryParams.joinToString("&")}"

        Log.d("ImageApiService", "🌐 Fetching images from $fullUrl")

        val connection = URL(fullUrl).openConnection() as? HttpURLConnection
            ?: throw Exception("❌ Invalid connection for image list")

        connection.requestMethod = "GET"
        connection.connect()

        if (connection.responseCode != 200) {
            Log.e("ImageApiService", "❌ Failed to fetch images: ${connection.responseCode}")
            throw Exception("❌ Failed to fetch image list: ${connection.responseCode}")
        }

        connection.inputStream.bufferedReader().use(BufferedReader::readText)
    }

    suspend fun fetchEstimationsForField(apiUrl: String, fieldId: Int): String = withContext(Dispatchers.IO) {
        val fullUrl = "$apiUrl/fields/$fieldId/estimations/"
        Log.d("ImageApiService", "🌐 Fetching estimations from $fullUrl")

        val connection = URL(fullUrl).openConnection() as? HttpURLConnection
            ?: throw Exception("❌ Invalid connection for estimations")

        connection.requestMethod = "GET"
        connection.connect()

        if (connection.responseCode != 200) {
            Log.e("ImageApiService", "❌ Failed to fetch estimations: ${connection.responseCode}")
            throw Exception("❌ Failed to fetch estimations: ${connection.responseCode}")
        }

        connection.inputStream.bufferedReader().use(BufferedReader::readText)
    }


    suspend fun fetchImageFromCloud(
        context: Context,
        rootUri: Uri,
        mediaUrl: String,
        imageUrl: String,
        filename: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val fullUrl = mediaUrl.trimEnd('/') + imageUrl
            Log.d("ImageApiService", "☁️ Fetching image from $fullUrl")

            val conn = URL(fullUrl).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connect()

            if (conn.responseCode != 200) {
                Log.e("ImageApiService", "❌ Failed to download image. Code: ${conn.responseCode}")
                return@withContext false
            }

            val bytes = conn.inputStream.readBytes()

            val success = StorageUtils.saveBinaryFile(
                context = context,
                baseUri = rootUri,
                relativePath = "images/$filename",
                data = bytes,
                mimeType = "image/jpeg"
            )


            if (success) {
                Log.d("ImageApiService", "✅ Saved cloud image to images/$filename")
            } else {
                Log.e("ImageApiService", "❌ Failed to save image locally")
            }

            return@withContext success
        } catch (e: Exception) {
            ErrorLogger.logError(context, rootUri, "❌ Error downloading image: $imageUrl", e)
            return@withContext false
        }
    }


}
