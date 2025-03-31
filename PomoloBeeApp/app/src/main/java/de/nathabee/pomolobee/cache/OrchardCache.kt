package de.nathabee.pomolobee.cache

import de.nathabee.pomolobee.model.Location
import de.nathabee.pomolobee.model.FruitType

// cache/OrchardCache.kt
object OrchardCache {
    var fruits: List<FruitType> = emptyList()
    var locations: List<Location> = emptyList()

    fun isInitialized(): Boolean =
        fruits.isNotEmpty() && locations.isNotEmpty()

    fun clear() {
        fruits = emptyList()
        locations = emptyList()
    }
}
