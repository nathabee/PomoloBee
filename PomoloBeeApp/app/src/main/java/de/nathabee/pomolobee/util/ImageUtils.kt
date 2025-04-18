package de.nathabee.pomolobee.util

import org.json.JSONObject


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
