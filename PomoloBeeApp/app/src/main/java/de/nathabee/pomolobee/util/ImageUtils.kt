package de.nathabee.pomolobee.util

import android.util.Log
import org.json.JSONObject


import de.nathabee.pomolobee.model.Estimation
import de.nathabee.pomolobee.cache.OrchardCache

fun findEstimationForImage(imageId: Int?): Estimation? {
    if (imageId == null) {
        Log.d("ImageUtils", "❌ ImageId is null.")
        return null
    }
    val estimation = OrchardCache.estimations.find { it.imageId == imageId }
    if (estimation != null) {
        Log.d("ImageUtils", "✅ Found estimation for imageId=$imageId")
    } else {
        Log.d("ImageUtils", "❌ No estimation found for imageId=$imageId")
    }
    return estimation
}

data class Coordinates(val x: Float, val y: Float)

fun parseXYLocation(xyLocation: String?): Coordinates? {
    return try {
        if (xyLocation == null) return null
        val json = JSONObject(xyLocation)
        val x = json.getDouble("x").toFloat()
        val y = json.getDouble("y").toFloat()
        Coordinates(x, y)
    } catch (e: Exception) {
        null
    }
}
