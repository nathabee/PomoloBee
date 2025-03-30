/**
 * SVG MAP SCREEN  
 *
 * Description:
 * Displays an SVG field map for the selected field. Users can tap a row to auto-select it.
 *
 * Inputs:
 * - Field ID
 * - Path to the SVG asset for this field
 *
 * Outputs:
 * - Raw ID selected by the user, sent back to OrchardSelectionScreen
 *
 * Tech Considerations:
 * - Use a library like AndroidSVG or Coil for SVG rendering
 * - Support touch/tap interaction to identify selected element (use 'id' or 'title' tags in SVG)
 *
 * UX Considerations:
 * - Back button to return to selection screen
 * - Highlight selected row visually
 */


@Composable
fun SvgMapScreen(
    svgResId: Int,
    onRawSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Tap on the row to select it", style = MaterialTheme.typography.titleMedium)

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            factory = { context ->
                val svgView = ImageView(context)

                // Load SVG using Coil
                svgView.load(svgResId) {
                    crossfade(true)
                    placeholder(android.R.drawable.ic_menu_gallery)
                }

                // TODO: Add actual hit detection on SVG regions (e.g., with AndroidSVG)
                svgView.setOnClickListener {
                    // For demo: assume "raw_4" was clicked
                    onRawSelected("raw_4")
                }

                svgView
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onBack() }) {
            Text("Back")
        }
    }
}
