package de.nathabee.pomolobee.cache

import de.nathabee.pomolobee.model.Fruit
import de.nathabee.pomolobee.model.Location

object OrchardCache {
    var fruits: List<Fruit> = emptyList()
    var locations: List<Location> = emptyList()

    fun isInitialized(): Boolean =
        fruits.isNotEmpty() && locations.isNotEmpty()


    fun clear() {
        fruits = emptyList()
        locations = emptyList()
    }



    fun load(fruitList: List<Fruit>, locationList: List<Location>) {
        fruits = fruitList
        locations = locationList
    }
}
