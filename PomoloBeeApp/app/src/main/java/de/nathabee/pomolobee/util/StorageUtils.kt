package de.nathabee.pomolobee.util

import android.content.Context
import android.content.res.AssetManager
import android.net.Uri
import androidx.documentfile.provider.DocumentFile


fun copyAssetsIfNotExists(context: Context, treeUri: Uri) {
    val assetManager = context.assets

    val staticAssets = listOf(
        "config/fruits.json",
        "config/locations.json"
    )

    val assetFoldersToCopy = listOf(
        "fields/svg",
        "fields/background"
    )

    val foldersToCreateEmpty = listOf(
        "images", "logs", "results"
    )

    val rootDoc = DocumentFile.fromTreeUri(context, treeUri)

    // Copy static files
    for (assetPath in staticAssets) {
        copyAssetFile(context, assetManager, rootDoc, assetPath)
    }

    // Copy all files from asset folders
    for (folder in assetFoldersToCopy) {
        val files = assetManager.list(folder) ?: continue
        for (file in files) {
            if (file.isNullOrBlank()) continue
            val fullPath = "$folder/$file"
            copyAssetFile(context, assetManager, rootDoc, fullPath)
        }
    }

    // Create empty folders
    for (folderName in foldersToCreateEmpty) {
        rootDoc?.findFile(folderName) ?: rootDoc?.createDirectory(folderName)
    }
}


private fun copyAssetFile(
    context: Context,
    assetManager: AssetManager,
    rootDoc: DocumentFile?,
    assetPath: String
)
 {
    val parts = assetPath.split("/")
    val fileName = parts.last()
    val subDirs = parts.dropLast(1)


    var currentDir = rootDoc
    for (dir in subDirs) {
        currentDir = currentDir?.findFile(dir)?.takeIf { it.isDirectory }
            ?: currentDir?.createDirectory(dir)
    }

    // Skip if it’s a directory placeholder
    if (assetPath.endsWith("/")) {
        currentDir?.findFile(fileName) ?: currentDir?.createDirectory(fileName)
        return
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
