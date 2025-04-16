package de.nathabee.pomolobee.model

import android.net.Uri

data class PendingImage(
    val fileName: String,              // e.g., C1_R1_1713190000000.jpg
    val uri: Uri,                      // Optional: full SAF uri for rendering
    val fieldId: Int,
    val rowId: Int,
    val imageId: String?,              // Set after upload
    val date: String,
    val isSynced: Boolean = false,
    val failedSync: Boolean = false
)