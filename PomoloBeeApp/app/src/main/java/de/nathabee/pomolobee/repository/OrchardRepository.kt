package de.nathabee.pomolobee.repository

import android.util.Log

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.nathabee.pomolobee.cache.OrchardCache
import de.nathabee.pomolobee.model.*
import java.io.File

object OrchardRepository {
    private const val CONFIG_PATH = "/sdcard/PomoloBee/config"

    fun loadAllConfig(): Boolean {
        return try {
            val locationsJson = File("$CONFIG_PATH/locations.json").readText()
            val locationResponse = Gson().fromJson(locationsJson, LocationResponse::class.java)

            val validLocations = locationResponse.data.locations.filter {
                it.field.fieldId != null // ensure fieldId is there
            }

            if (validLocations.isEmpty()) {
                Log.e("ConfigLoad", "No valid fields in locations.json")
                return false
            }

            OrchardCache.locations = validLocations

            val fruitsJson = File("$CONFIG_PATH/fruits.json").readText()
            val fruitResponse = Gson().fromJson(fruitsJson, FruitResponse::class.java)

            OrchardCache.fruits = fruitResponse.data.fruits

            true
        } catch (e: Exception) {
            Log.e("ConfigLoad", "Error loading config", e)
            false
        }
    }

}
