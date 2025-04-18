package de.nathabee.pomolobee.util

import kotlinx.serialization.json.*

fun Map<String, Any?>.toJsonObject(): JsonObject {
    return buildJsonObject {
        for ((key, value) in this@toJsonObject) {
            put(
                key,
                when (value) {
                    null -> JsonNull
                    is Boolean -> JsonPrimitive(value)
                    is Number -> JsonPrimitive(value)
                    is String -> JsonPrimitive(value)
                    else -> JsonPrimitive(value.toString()) // fallback for unsupported types
                }
            )
        }
    }
}
