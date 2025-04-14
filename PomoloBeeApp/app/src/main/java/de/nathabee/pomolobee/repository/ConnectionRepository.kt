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


    fun syncOrchard(context: Context, configDirUri: Uri) {
        val configDir = DocumentFile.fromTreeUri(context, configDirUri)
            ?: throw IllegalArgumentException("❌ Invalid config directory URI")

        if (!configDir.isDirectory) {
            throw IllegalStateException("❌ Provided URI is not a directory")
        }

        val fruitsFile = configDir.findFile("fruits.json")
            ?: throw IllegalStateException("❌ Missing fruits.json")

        val locationsFile = configDir.findFile("locations.json")
            ?: throw IllegalStateException("❌ Missing locations.json")

        val gson = Gson()
        val fruitsList = readListFromJson<Fruit>(context, fruitsFile.uri, gson)
            ?: throw IllegalStateException("❌ Failed to parse fruits.json")

        val locationsList = readListFromJson<Location>(context, locationsFile.uri, gson)
            ?: throw IllegalStateException("❌ Failed to parse locations.json")

        OrchardCache.load(fruitsList, locationsList)
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
