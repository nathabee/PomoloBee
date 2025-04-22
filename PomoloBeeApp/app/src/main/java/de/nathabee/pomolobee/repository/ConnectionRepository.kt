package de.nathabee.pomolobee.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.nathabee.pomolobee.cache.OrchardCache
import de.nathabee.pomolobee.model.EstimationData
import de.nathabee.pomolobee.model.Fruit
import de.nathabee.pomolobee.model.Location
import de.nathabee.pomolobee.util.ErrorLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import de.nathabee.pomolobee.model.LocationResponse
import de.nathabee.pomolobee.network.OrchardApiService
import de.nathabee.pomolobee.util.StorageUtils
import de.nathabee.pomolobee.network.ImageApiService
import de.nathabee.pomolobee.model.ImageListResponse
import de.nathabee.pomolobee.model.EstimationResponse
import de.nathabee.pomolobee.util.parseXYLocation


object ConnectionRepository {
    suspend fun testConnection(
        context: Context,
        rootUri: Uri?,
        apiUrl: String,
        mediaUrl: String,
        onVersionRetrieved: (String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        Log.d("ConnectionTest", "🌐 Starting connection test...")

        try {
            // --- API Check ---
            val version = try {
                val conn = URL("$apiUrl/ml/version/").openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connect()

                if (conn.responseCode != 200) {
                    throw Exception("API responded with code ${conn.responseCode}")
                }

                val raw = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(raw)
                val modelVersion = json.getJSONObject("data").getString("model_version")

                Log.d("ConnectionTest", "✅ API model version: $modelVersion")
                modelVersion
            } catch (e: Exception) {
                ErrorLogger.logError(context, rootUri, "❌ API connection failed", e)
                return@withContext false
            }

            onVersionRetrieved(version)

            // --- Media Check ---
            try {
                val conn = URL("$mediaUrl/fields/svg/default_map.svg").openConnection() as HttpURLConnection
                conn.requestMethod = "HEAD"
                conn.connect()

                if (conn.responseCode != 200) {
                    throw Exception("Media responded with code ${conn.responseCode}")
                }
            } catch (e: Exception) {
                ErrorLogger.logError(context, rootUri, "❌ Media connection failed", e)
                return@withContext false
            }

            Log.d("ConnectionTest", "✅ Connection test passed")
            true
        } catch (e: Exception) {
            ErrorLogger.logError(context, rootUri, "💥 Unexpected connection error", e)
            false
        }
    }


    //CLOUD
    suspend fun performCloudSync(
        context: Context,
        rootUri: Uri,
        apiUrl: String,
        mediaUrl: String
    ): Boolean = withContext(Dispatchers.IO) {

        val gson = Gson()

        try {
            // Step 1: Fetch config
            val locationsJson = try {
                OrchardApiService.fetchLocations(apiUrl)
            } catch (e: Exception) {
                ErrorLogger.logError(context, rootUri, "❌ Failed to fetch locations", e)
                return@withContext false
            }

            val fruitsJson = try {
                OrchardApiService.fetchFruits(apiUrl)
            } catch (e: Exception) {
                ErrorLogger.logError(context, rootUri, "❌ Failed to fetch fruits", e)
                return@withContext false
            }

            // Step 2: Save
            if (!StorageUtils.saveTextFile(context, rootUri, "config/locations.json", locationsJson) ||
                !StorageUtils.saveTextFile(context, rootUri, "config/fruits.json", fruitsJson)) {
                ErrorLogger.logError(context, rootUri, "❌ Failed to save config files")
                return@withContext false
            }

            val locationResponse = gson.fromJson(locationsJson, LocationResponse::class.java)

            // Step 3: SVGs
            val svgNames = locationResponse.data.locations.mapNotNull {
                it.field.svgMapUrl?.substringAfterLast("/")?.takeIf { name -> name.endsWith(".svg") }
            }.distinct()

            for (svgName in svgNames) {
                try {
                    val svgBytes = OrchardApiService.fetchBinaryFromUrl("$mediaUrl/fields/svg/$svgName")
                    val saved = StorageUtils.saveBinaryFile(context, rootUri, "fields/svg/$svgName", svgBytes, "image/svg+xml")
                    if (!saved) {
                        ErrorLogger.logError(context, rootUri, "⚠️ Failed to save SVG: $svgName")
                    }
                } catch (e: Exception) {
                    ErrorLogger.logError(context, rootUri, "❌ Failed to download SVG $svgName", e)
                }
            }

            // Step 4: Backgrounds
            val bgNames = locationResponse.data.locations.mapNotNull {
                it.field.backgroundImageUrl?.substringAfterLast("/")?.takeIf { it.endsWith(".jpg", true) || it.endsWith(".jpeg", true) }
            }.distinct()

            for (bgName in bgNames) {
                try {
                    val bgBytes = OrchardApiService.fetchBinaryFromUrl("$mediaUrl/fields/background/$bgName")
                    val saved = StorageUtils.saveBinaryFile(context, rootUri, "fields/background/$bgName", bgBytes, "image/jpeg")
                    if (!saved) {
                        ErrorLogger.logError(context, rootUri, "⚠️ Failed to save background: $bgName")
                    }
                } catch (e: Exception) {
                    ErrorLogger.logError(context, rootUri, "❌ Failed to download background $bgName", e)
                }
            }

            // Step 5: Save image list
            val imagesJson = try {
                ImageApiService.fetchImagesList(apiUrl)
            } catch (e: Exception) {
                ErrorLogger.logError(context, rootUri, "❌ Failed to fetch image list", e)
                return@withContext false
            }

            if (!StorageUtils.saveTextFile(context, rootUri, "image_data/images.json", imagesJson)) {
                ErrorLogger.logError(context, rootUri, "❌ Failed to save images.json")
                return@withContext false
            }

            // Step 6: Estimations
            val fieldIds = locationResponse.data.locations.map { it.field.fieldId }.distinct()
            val allEstimations = mutableListOf<String>()

            for (fieldId in fieldIds) {
                try {
                    allEstimations.add(ImageApiService.fetchEstimationsForField(apiUrl, fieldId))
                } catch (e: Exception) {
                    ErrorLogger.logError(context, rootUri, "❌ Failed to fetch estimations for field $fieldId", e)
                }
            }

            val parsedEstimations = allEstimations.mapNotNull { json ->
                try {
                    gson.fromJson(json, EstimationResponse::class.java).data.estimations
                } catch (e: Exception) {
                    ErrorLogger.logError(context, rootUri, "❌ Failed parsing estimation response", e)
                    null
                }
            }.flatten()


            val estimJson = gson.toJson(EstimationResponse("success", EstimationData(parsedEstimations)))
            if (!StorageUtils.saveTextFile(context, rootUri, "image_data/estimations.json", estimJson)) {
                ErrorLogger.logError(context, rootUri, "❌ Failed to save estimations.json")
                return@withContext false
            }

            // Step 7: Final cache reloads
            val configOk = OrchardRepository.loadAllConfigFromUri(context, rootUri)
            val imageOk = ImageRepository.loadAllImageDataFromUri(context, rootUri)

            if (!configOk) ErrorLogger.logError(context, rootUri, "❌ Reloading orchard config failed")
            if (!imageOk) ErrorLogger.logError(context, rootUri, "❌ Reloading image data failed")

            if (configOk && imageOk) {
                Log.d("CloudSync", "✅ Cloud sync completed with minor warnings (see logs)")
            }

            return@withContext configOk && imageOk

        } catch (e: Exception) {
            ErrorLogger.logError(context, rootUri, "❌ Unexpected Cloud Sync error", e)
            return@withContext false
        }
    }


}


