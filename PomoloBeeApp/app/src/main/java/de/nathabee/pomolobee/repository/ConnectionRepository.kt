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
import java.net.HttpURLConnection
import java.net.URL

object ConnectionRepository {

    suspend fun testConnection(
        context: Context,
        apiUrl: String,
        mediaUrl: String,
        onVersionRetrieved: (String) -> Unit
    ): Boolean {
        Log.d("ConnectionTest", "🌐 Starting connection test...")

        return try {
            // --- API Check ---
            Log.d("ConnectionTest", "🔌 Testing API Endpoint: $apiUrl/ml/version/")

            val apiCheck = try {
                val conn = URL("$apiUrl/ml/version/").openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connect()

                if (conn.responseCode == 200) {
                    val version = conn.inputStream.bufferedReader().readText().trim()
                    Log.d("ConnectionTest", "✅ API version: $version")
                    onVersionRetrieved(version)
                    true
                } else {
                    Log.w("ConnectionTest", "⚠️ API responded with ${conn.responseCode}")
                    false
                }
            } catch (e: Exception) {
                Log.e("ConnectionTest", "❌ API check failed", e)
                ErrorLogger.logError(context, null, "❌ API connection error", e)
                false
            }

            // --- Media Check ---
            Log.d("ConnectionTest", "🖼️ Testing Media Endpoint: $mediaUrl/svg/fields/default_map.svg")

            val mediaCheck = try {
                val conn = URL("$mediaUrl/svg/fields/default_map.svg").openConnection() as HttpURLConnection
                conn.requestMethod = "HEAD"
                conn.connect()

                val ok = conn.responseCode == 200
                if (!ok) Log.w("ConnectionTest", "⚠️ Media check failed: ${conn.responseCode}")
                ok
            } catch (e: Exception) {
                Log.e("ConnectionTest", "❌ Media check failed", e)
                ErrorLogger.logError(context, null, "❌ Media connection error", e)
                false
            }

            val result = apiCheck && mediaCheck
            Log.d("ConnectionTest", if (result) "✅ Connection test passed" else "❌ Connection test failed")
            result
        } catch (e: Exception) {
            Log.e("ConnectionTest", "🔥 Unexpected connection test failure", e)
            ErrorLogger.logError(context, null, "🔥 Unexpected error in connection test", e)
            false
        }
    }



    fun syncOrchard(context: Context, configDirUri: Uri): String {
        val configDir = DocumentFile.fromTreeUri(context, configDirUri)
        if (configDir == null || !configDir.isDirectory) {
            return "❌ Invalid config directory"
        }

        val fruitsFile = configDir.findFile("fruits.json")
        val locationsFile = configDir.findFile("locations.json")

        if (fruitsFile == null || locationsFile == null) {
            return "❌ Required config files missing"
        }

        val gson = Gson()
        val fruitsList = readListFromJson<Fruit>(context, fruitsFile.uri, gson)
        val locationsList = readListFromJson<Location>(context, locationsFile.uri, gson)

        if (fruitsList == null || locationsList == null) {
            return "❌ Failed to parse config files"
        }

        OrchardCache.load(fruitsList, locationsList)
        return "✅ Orchard configuration synced"
    }
}

// Generic list reader
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
