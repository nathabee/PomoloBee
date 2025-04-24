import de.nathabee.pomolobee.viewmodel.*

// SharedViewModel.kt	Temporarily holds selected image + row/location before it's saved. Used across Camera/Location.



data class PomolobeeViewModels(
    val settings: SettingsViewModel,
    val orchard: OrchardViewModel,
    val image: ImageViewModel
)