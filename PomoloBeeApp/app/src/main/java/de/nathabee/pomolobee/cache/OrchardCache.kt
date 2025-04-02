package de.nathabee.pomolobee.cache

import de.nathabee.pomolobee.model.FruitType
import de.nathabee.pomolobee.model.Location

object OrchardCache {
    var fruits: List<FruitType> = emptyList()
    var locations: List<Location> = emptyList()

    fun isInitialized(): Boolean =
        fruits.isNotEmpty() && locations.isNotEmpty()

    fun clear() {
        fruits = emptyList()
        locations = emptyList()
    }

    fun load(fruitList: List<FruitType>, locationList: List<Location>) {
        fruits = fruitList
        locations = locationList
    }
}
