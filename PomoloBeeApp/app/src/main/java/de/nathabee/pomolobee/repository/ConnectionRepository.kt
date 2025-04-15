package de.nathabee.pomolobee.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.nathabee.pomolobee.cache.OrchardCache
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




object ConnectionRepository {

    suspend fun testConnection(
        apiUrl: String,
        mediaUrl: String,
        onVersionRetrieved: (String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        Log.d("ConnectionTest", "🌐 Starting connection test...")

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
            throw Exception("❌ API connection failed", e)
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
            throw Exception("❌ Media connection failed", e)
        }

        Log.d("ConnectionTest", "✅ Connection test passed")
        true
    }


    //CLOUD


    suspend fun performCloudSync(context: Context, rootUri: Uri, apiUrl: String, mediaUrl: String): String {
        return try {
            val gson = Gson()

            // 🔗 Step 1: Fetch config JSONs
            val locationsJson = try {
                OrchardApiService.fetchLocations(apiUrl)
            } catch (e: Exception) {
                Log.e("CloudSync", "❌ Failed to fetch locations", e)
                return "❌ Failed to fetch locations: ${e.message}"
            }

            val fruitsJson = try {
                OrchardApiService.fetchFruits(apiUrl)
            } catch (e: Exception) {
                Log.e("CloudSync", "❌ Failed to fetch fruits", e)
                return "❌ Failed to fetch fruits: ${e.message}"
            }



            // 💾 Step 2: Save to /config/
            val savedLocations = StorageUtils.saveTextFile(context, rootUri, "config/locations.json", locationsJson)
            val savedFruits = StorageUtils.saveTextFile(context, rootUri, "config/fruits.json", fruitsJson)

            if (!savedLocations || !savedFruits) {
                return "❌ Failed to save config files to local storage"
            }

            // 🧠 Step 3: Parse locations JSON
            val locationResponse = gson.fromJson(locationsJson, LocationResponse::class.java)

            // Collect unique SVG names
            val svgNames = locationResponse.data.locations.mapNotNull {
                it.field.svgMapUrl?.substringAfterLast("/")?.takeIf { name -> name.endsWith(".svg") }
            }.distinct()

            // 📥 Step 4: Download SVGs
            for (svgName in svgNames) {
                try {
                    val svgBytes = OrchardApiService.fetchBinaryFromUrl("$mediaUrl/fields/svg/$svgName")
                    val saved = StorageUtils.saveBinaryFile(
                        context,
                        rootUri,
                        "fields/svg/$svgName",
                        svgBytes,
                        "image/svg+xml"
                    )
                    if (!saved) {
                        Log.w("CloudSync", "⚠️ Failed to save SVG: $svgName")
                    }
                } catch (e: Exception) {
                    Log.e("CloudSync", "❌ Error downloading SVG $svgName", e)
                }
            }

            // 🖼 Step 5: Download Background Images
            val backgroundNames = locationResponse.data.locations.mapNotNull {
                it.field.backgroundImageUrl?.substringAfterLast("/")?.takeIf { name ->
                    name.endsWith(".jpeg", ignoreCase = true) || name.endsWith(".jpg", ignoreCase = true)
                }
            }.distinct()

            for (bgName in backgroundNames) {
                try {
                    val bgBytes = OrchardApiService.fetchBinaryFromUrl("$mediaUrl/fields/background/$bgName")
                    val saved = StorageUtils.saveBinaryFile(
                        context,
                        rootUri,
                        "fields/background/$bgName",
                        bgBytes,
                        "image/jpeg"
                    )
                    if (!saved) {
                        Log.w("CloudSync", "⚠️ Failed to save background: $bgName")
                    }
                } catch (e: Exception) {
                    Log.e("CloudSync", "❌ Error downloading background $bgName", e)
                }
            }

            // ✅ Step 6: Load config into cache
            val success = OrchardRepository.loadAllConfigFromUri(context, rootUri)
            if (!success) {
                return "❌ Failed to load configuration after sync"
            }

            return "✅ Cloud sync completed successfully"
        } catch (e: Exception) {
            Log.e("CloudSync", "💥 Cloud sync failed", e)
            return "❌ Cloud sync failed: ${e.message}"
        }
    }


}

// Generic list reader NOT USED ANYMORE
inline fun <reified T> readListFromJson(context: Context, fileUri: Uri, gson: Gson): List<T>? {
    return try {
        context.contentResolver.openInputStream(fileUri)?.use { stream ->
            val json = stream.bufferedReader().readText()
            val type = TypeToken.getParameterized(List::class.java, T::class.java).type
            gson.fromJson(json, type)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

}


