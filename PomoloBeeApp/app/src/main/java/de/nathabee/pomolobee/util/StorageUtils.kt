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
        "fields/background/C1.jpeg",
        "images/",
        "logs/",
        "results/"
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

        // If path ends with "/", it's an empty folder — create and continue
        if (assetPath.endsWith("/")) {
            currentDir?.findFile(fileName) ?: currentDir?.createDirectory(fileName)
            continue
        }

        // Otherwise, copy file if it doesn't exist
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


fun readAssetFromStorage(context: Context, rootUri: Uri, relativePath: String): String? {
    val parts = relativePath.split("/")
    val fileName = parts.last()
    val subDirs = parts.dropLast(1)

    var currentDoc = DocumentFile.fromTreeUri(context, rootUri)
    for (dir in subDirs) {
        currentDoc = currentDoc?.findFile(dir)?.takeIf { it.isDirectory }
    }

    val targetFile = currentDoc?.findFile(fileName)

    return targetFile?.uri?.let { uri ->
        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
    }
}
