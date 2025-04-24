package de.nathabee.pomolobee.navigation



sealed class Screen(val route: String, val title: String) {
    object Camera : Screen("camera", "Camera")
    object Processing : Screen("processing", "Processing")
    object Settings : Screen("settings", "Settings")
    object Orchard : Screen("orchard", "Orchard")
    object About : Screen("about", "About")
    object Location : Screen("location", "Location")
    object Init : Screen("init", "Initialisation")
    object ImageHistory : Screen("imagehistory", "Image History")

    object ImagePreviewMap : Screen("svgPreviewMap", "Image Preview Map")


    // Parametrized screen


    object SvgMap : Screen("svgmap", "SVG Map") {


        fun createRoute(fieldId: Int) = "svgmap?fieldId=$fieldId"

        fun withArgs(vararg args: Pair<String, String?>): String {
            return buildString {
                append(route)
                if (args.isNotEmpty()) {
                    append("?")
                    append(args.joinToString("&") { (key, value) -> "$key=${value ?: ""}" })
                }
            }
        }
    }



    object ErrorLog : Screen("errorlog", "Error Log")


}
