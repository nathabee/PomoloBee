package de.nathabee.pomolobee.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import de.nathabee.pomolobee.cache.OrchardCache
import de.nathabee.pomolobee.model.*

import java.io.File
import de.nathabee.pomolobee.data.UserPreferences
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first


// OrchardRepository.kt
object OrchardRepository {

    fun loadAllConfigFromPath(configDir: String): Boolean {
        return try {
            val locationsJson = File(configDir, "locations.json").readText()
            val fruitsJson = File(configDir, "fruits.json").readText()

            val locationResponse = Gson().fromJson(locationsJson, LocationResponse::class.java)
            val fruitResponse = Gson().fromJson(fruitsJson, FruitResponse::class.java)

            val validLocations = locationResponse.data.locations.filter {
                it.field.fieldId != null
            }

            if (validLocations.isEmpty()) {
                Log.e("ConfigLoad", "No valid fields in locations.json")
                return false
            }

            OrchardCache.locations = validLocations
            OrchardCache.fruits = fruitResponse.data.fruits

            true
        } catch (e: Exception) {
            Log.e("ConfigLoad", "Error loading config", e)
            false
        }
    }
}
