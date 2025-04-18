package de.nathabee.pomolobee.cache

import de.nathabee.pomolobee.model.*

object OrchardCache {
    var fruits: List<Fruit> = emptyList()
    var locations: List<Location> = emptyList()
    var images: List<ImageRecord> = emptyList()
    var estimations: List<Estimation> = emptyList()

    fun isInitialized(): Boolean =
        fruits.isNotEmpty() && locations.isNotEmpty()

    fun clear() {
        fruits = emptyList()
        locations = emptyList()
        images = emptyList()
        estimations = emptyList()
    }

    fun load(
        fruitList: List<Fruit>,
        locationList: List<Location>,
        imageList: List<ImageRecord> = emptyList(),
        estimationList: List<Estimation> = emptyList()
    ) {
        fruits = fruitList
        locations = locationList
        images = imageList
        estimations = estimationList
    }

    fun loadImages(imageList: List<ImageRecord>) {
        images = imageList
    }

    fun loadEstimations(estimationList: List<Estimation>) {
        estimations = estimationList
    }
}
