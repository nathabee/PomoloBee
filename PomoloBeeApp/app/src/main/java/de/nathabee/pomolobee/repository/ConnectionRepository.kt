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
import java.net.HttpURLConnection
import java.net.URL

object ConnectionRepository {

    suspend fun testConnection(
        apiUrl: String,
        mediaUrl: String,
        onVersionRetrieved: (String) -> Unit
    ): Boolean {
        return try {
            val apiCheck = runCatching {
                val conn = URL("$apiUrl/api/ml/version/").openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connect()
                if (conn.responseCode == 200) {
                    val version = conn.inputStream.bufferedReader().readText().trim()
                    onVersionRetrieved(version)
                    true
                } else false
            }.getOrDefault(false)

            val mediaCheck = runCatching {
                val conn = URL("$mediaUrl/svg/fields/default_map.svg").openConnection() as HttpURLConnection
                conn.requestMethod = "HEAD"
                conn.connect()
                conn.responseCode == 200
            }.getOrDefault(false)

            apiCheck && mediaCheck
        } catch (e: Exception) {
            Log.e("ConnectionRepository", "Test failed", e)
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
