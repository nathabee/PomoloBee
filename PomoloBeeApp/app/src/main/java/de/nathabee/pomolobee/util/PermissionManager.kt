package de.nathabee.pomolobee.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat



object PermissionManager {

    // 📸 Required for camera, storage access
    val REQUIRED_PERMISSIONS = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    fun allGranted(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}
