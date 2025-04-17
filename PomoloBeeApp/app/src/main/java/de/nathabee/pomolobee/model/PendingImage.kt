package de.nathabee.pomolobee.model

import android.net.Uri


import json

def upload_image(request):
xy_location = request.POST.get('xy_location')
try:
coords = json.loads(xy_location)
x = coords['x']
y = coords['y']
if 0 <= x <= 1 and 0 <= y <= 1:
# Save to the model
image.xy_location = xy_location
image.save()
else:
# Handle invalid range
pass
except (json.JSONDecodeError, KeyError):
# Handle invalid JSON
pass



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