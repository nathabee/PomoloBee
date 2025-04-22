package de.nathabee.pomolobee.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import de.nathabee.pomolobee.cache.OrchardCache
import de.nathabee.pomolobee.model.*

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import de.nathabee.pomolobee.util.ErrorLogger

object OrchardRepository {
    fun loadAllConfigFromUri(context: Context, rootUri: Uri): Boolean {
        Log.d("ConfigLoad", "🚀 Starting config load from rootUri = $rootUri")

        return try {
            val rootDoc = DocumentFile.fromTreeUri(context, rootUri)
            if (rootDoc == null) {
                ErrorLogger.logError(context, rootUri, "❌ Failed to create DocumentFile from rootUri")
                return false
            }

            val configDir = rootDoc.findFile("config")
            if (configDir == null || !configDir.isDirectory) {
                ErrorLogger.logError(context, rootUri, "❌ 'config' folder not found under $rootUri")
                return false
            } else {
                Log.d("ConfigLoad", "📁 Found config directory: ${configDir.uri}")
            }

            val locationsFile = configDir.findFile("locations.json")
            if (locationsFile == null) {
                ErrorLogger.logError(context, rootUri, "❌ locations.json not found in config directory")
            } else {
                Log.d("ConfigLoad", "📄 Found locations.json at: ${locationsFile.uri}")
            }

            val fruitsFile = configDir.findFile("fruits.json")
            if (fruitsFile == null) {
                ErrorLogger.logError(context, rootUri, "❌ fruits.json not found in config directory")
            } else {
                Log.d("ConfigLoad", "📄 Found fruits.json at: ${fruitsFile.uri}")
            }

            if (locationsFile == null || fruitsFile == null) {
                return false // errors already logged above
            }

            val locationsJson = context.contentResolver.openInputStream(locationsFile.uri)
                ?.bufferedReader().use { it?.readText() }

            if (locationsJson == null) {
                ErrorLogger.logError(context, rootUri, "❌ Failed to read locations.json")
                return false
            }

            val fruitsJson = context.contentResolver.openInputStream(fruitsFile.uri)
                ?.bufferedReader().use { it?.readText() }

            if (fruitsJson == null) {
                ErrorLogger.logError(context, rootUri, "❌ Failed to read fruits.json")
                return false
            }

            Log.d("ConfigLoad", "✅ Successfully read both config files")

            val locationResponse = Gson().fromJson(locationsJson, LocationResponse::class.java)
            val fruitResponse = Gson().fromJson(fruitsJson, FruitResponse::class.java)

            val validLocations = locationResponse.data.locations.filter { it.field.fieldId != null }

            Log.d("ConfigLoad", "🧭 Loaded ${validLocations.size} valid locations")
            Log.d("ConfigLoad", "🍎 Loaded ${fruitResponse.data.fruits.size} fruits")

            if (validLocations.isEmpty()) {
                ErrorLogger.logError(context, rootUri, "❌ No valid fields in locations.json")
                return false
            }

            OrchardCache.locations = validLocations
            OrchardCache.fruits = fruitResponse.data.fruits

            Log.d("ConfigLoad", "🎉 Config successfully loaded and cached")
            true

        } catch (e: Exception) {
            ErrorLogger.logError(context, rootUri, "💥 Exception while loading config from Uri", e)
            false
        }
    }
}
