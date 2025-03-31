package de.nathabee.pomolobee.repository

import de.nathabee.pomolobee.data.UserPreferences
import android.content.Context
import android.util.Log
import de.nathabee.pomolobee.cache.OrchardCache
import de.nathabee.pomolobee.model.FruitResponse
import de.nathabee.pomolobee.model.LocationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import com.google.gson.Gson
import kotlinx.coroutines.flow.firstOrNull


object ConnectionRepository {

    suspend fun testConnection(context: Context): Boolean {
        val prefs = UserPreferences(context)
        val apiUrl = prefs.getApiEndpoint().firstOrNull()
        val mediaUrl = prefs.getMediaEndpoint().firstOrNull()

        return try {
            val apiCheck = apiUrl?.let {
                val conn = URL("$it/api/ml/version/").openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connect()
                if (conn.responseCode == 200) {
                    val version = conn.inputStream.bufferedReader().readText().trim()
                    prefs.setApiVersion(version) // ✅ Save version
                    true
                } else false
            } ?: false

            val mediaCheck = mediaUrl?.let {
                val conn = URL("$it/svg/fields/default_map.svg").openConnection() as HttpURLConnection
                conn.requestMethod = "HEAD"
                conn.connect()
                conn.responseCode == 200
            } ?: false

            apiCheck && mediaCheck
        } catch (e: Exception) {
            Log.e("Repository", "Connection test failed", e)
            false
        }
    }

    suspend fun syncOrchard(context: Context): String = withContext(Dispatchers.IO) {
        val prefs = UserPreferences(context)
        val mode = prefs.getSyncMode().firstOrNull()

        return@withContext if (mode == "local") {
            try {
                val configDir = "/sdcard/PomoloBee/config/"
                val fruitsJson = File(configDir, "fruits.json").readText()
                val locationsJson = File(configDir, "locations.json").readText()

                val fruits = Gson().fromJson(fruitsJson, FruitResponse::class.java)
                val locations = Gson().fromJson(locationsJson, LocationResponse::class.java)

                OrchardCache.fruits = fruits.data.fruits
                OrchardCache.locations = locations.data.locations

                "✅ Synced from local files"
            } catch (e: Exception) {
                Log.e("Repository", "Local sync failed", e)
                "❌ Failed to load local data"
            }
        } else {
            "❌ Cloud sync not supported yet"
        }
    }
}
