package de.nathabee.pomolobee.util

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

fun copyAssetsIfNotExists(context: Context, treeUri: Uri) {
    val assetManager = context.assets
    val assetStructure = listOf(
        "config/fruits.json",
        "config/locations.json",
        "fields/svg/C1_map.svg",
        "fields/svg/default_map.svg",
        "fields/background/C1.jpeg"
    )

    val rootDoc = DocumentFile.fromTreeUri(context, treeUri)

    for (assetPath in assetStructure) {
        val parts = assetPath.split("/")
        val fileName = parts.last()
        val subDirs = parts.dropLast(1)

        var currentDir = rootDoc
        for (dir in subDirs) {
            currentDir = currentDir?.findFile(dir)?.takeIf { it.isDirectory }
                ?: currentDir?.createDirectory(dir)
        }

        val existingFile = currentDir?.findFile(fileName)
        if (existingFile == null) {
            val mimeType = guessMimeType(fileName)
            val destFile = currentDir?.createFile(mimeType, fileName)

            if (destFile != null) {
                assetManager.open(assetPath).use { input ->
                    context.contentResolver.openOutputStream(destFile.uri)?.use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }
}

fun guessMimeType(fileName: String): String {
    return when {
        fileName.endsWith(".json") -> "application/json"
        fileName.endsWith(".svg") -> "image/svg+xml"
        fileName.endsWith(".jpeg") || fileName.endsWith(".jpg") -> "image/jpeg"
        else -> "application/octet-stream"
    }
}
