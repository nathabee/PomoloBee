package de.nathabee.pomolobee.navigation


sealed class Screen(val route: String, val title: String) {
    object Camera : Screen("camera", "Camera")
    object Processing : Screen("processing", "Processing")
    object Settings : Screen("settings", "Settings")
    object Orchard : Screen("orchard", "Orchard")
    object About : Screen("about", "About")
    object Location : Screen("location", "Location")
    object SvgMap : Screen("svgmap/{fieldId}", "SVG Map") {
        fun createRoute(fieldId: Int) = "svgmap/$fieldId"
    }

}
