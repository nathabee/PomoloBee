package de.nathabee.pomolobee.util

import android.content.Context
import android.content.res.AssetManager
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import de.nathabee.pomolobee.model.Location
//import kotlinx.serialization.json.*
//import java.io.File

/*
Function	                Purpose
resolveSubDirectory	        Creates or returns a folder (used for navigation/setup)
resolveOrCreateFile	        Navigates and creates a file (used for saving content)
saveTextFile, saveBinaryFile	File I/O entry points, high-level save logic
getSvgUriForLocation, getBackgroundUriForLocation	Resolves existing file URIs for loading assets
copyAssetsIfNotExists	    Bootstrap local folders from APK assets
readAssetFromStorage	    Reads text content from a specific file
hasAccessToUri	            SAF access check
guessMimeType	            Internal helper, small and useful
 */
object StorageUtils {
    //==================
// 📂 URI Access & Checking
//==================
    fun hasAccessToUri(context: Context, uri: Uri): Boolean {
        return try {
            val docFile = DocumentFile.fromTreeUri(context, uri)
            val exists = docFile != null && docFile.exists() && docFile.isDirectory
            if (!exists) Log.e("UriAccess", "❌ No access or invalid doc file for: $uri")
            exists
        } catch (e: Exception) {
            Log.e("UriAccess", "❌ Exception checking URI: ${e.message}")
            false
        }
    }

    fun getFriendlyFolderName(context: Context, uri: Uri): String {
        val docFile = DocumentFile.fromTreeUri(context, uri)
        val folderName = docFile?.name ?: "Unknown Folder"

        return if (uri.toString().contains("externalstorage")) {
            "SD-Karte/$folderName"
        } else {
            "Interner Speicher/$folderName"
        }
    }





//==================
// 🗂️ Subdirectory & File Resolution
//==================

    fun resolveSubDirectory(context: Context, baseUri: Uri?, subPath: String): DocumentFile? {
        val root = baseUri?.let { DocumentFile.fromTreeUri(context, it) } ?: return null
        val existing = root.findFile(subPath)
        return if (existing?.isDirectory == true) existing else root.createDirectory(subPath)


    }


    private fun resolveOrCreateFile(
        context: Context,
        baseUri: Uri,
        relativePath: String,
        mimeType: String
    ): DocumentFile? {
        val pathParts = relativePath.split("/")
        val fileName = pathParts.last()
        val subDirs = pathParts.dropLast(1)

        var currentDir = DocumentFile.fromTreeUri(context, baseUri)
        for (dir in subDirs) {
            currentDir = currentDir?.findFile(dir)?.takeIf { it.isDirectory }
                ?: currentDir?.createDirectory(dir)
        }

        // Remove old file if it exists (to ensure overwrite works properly)
        currentDir?.findFile(fileName)?.delete()

        return currentDir?.createFile(mimeType, fileName)
    }


//==================
// 💾 File Saving (Text/Binary)
//==================


    fun saveTextFile(
        context: Context,
        baseUri: Uri,
        relativePath: String,
        content: String
    ): Boolean {
        return try {
            val targetFile = resolveOrCreateFile(context, baseUri, relativePath, "application/json")
            if (targetFile == null) {
                Log.e("StorageUtils", "❌ Failed to resolve target file for $relativePath")
                return false
            }

            context.contentResolver.openOutputStream(targetFile.uri, "wt")?.use { output ->
                output.write(content.toByteArray())
            }

            Log.d("StorageUtils", "✅ Saved text file to $relativePath")
            true
        } catch (e: Exception) {
            Log.e("StorageUtils", "💥 Error saving text file to $relativePath", e)
            false
        }
    }

    fun saveBinaryFile(
        context: Context,
        baseUri: Uri,
        relativePath: String,
        data: ByteArray,
        mimeType: String
    ): Boolean {
        return try {
            val targetFile = resolveOrCreateFile(context, baseUri, relativePath, mimeType)
            if (targetFile == null) {
                Log.e("StorageUtils", "❌ Failed to resolve target file for $relativePath")
                return false
            }

            context.contentResolver.openOutputStream(targetFile.uri, "w")?.use { output ->
                output.write(data)
            }

            Log.d("StorageUtils", "✅ Saved binary file to $relativePath")
            true
        } catch (e: Exception) {
            Log.e("StorageUtils", "💥 Error saving binary file to $relativePath", e)
            false
        }
    }

    //==================
// 📖 File Reading
//==================


    private fun resolveFile(context: Context, baseUri: Uri, relativePath: String): DocumentFile? {
        val parts = relativePath.split("/")
        val fileName = parts.last()
        val subDirs = parts.dropLast(1)

        var currentDir = DocumentFile.fromTreeUri(context, baseUri)
        for (dir in subDirs) {
            currentDir = currentDir?.findFile(dir)?.takeIf { it.isDirectory }
                ?: return null
        }

        return currentDir?.findFile(fileName)
    }


    fun readJsonFileFromStorage(context: Context, baseUri: Uri, path: String): String? {
        val file = resolveFile(context, baseUri, path) ?: return null
        return context.contentResolver.openInputStream(file.uri)
            ?.bufferedReader()
            ?.use { it.readText() }
    }



    //==================
// 🧳 Asset Copy (Initial Bootstrap)
//==================
    private fun copyAssetFile(
        context: Context,
        assetManager: AssetManager,
        rootDoc: DocumentFile?,
        assetPath: String
    ) {
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

    fun copyAssetsIfNotExists(context: Context, treeUri: Uri) {
        val assetManager = context.assets

        val staticAssets = listOf(
            "config/fruits.json",
            "config/locations.json",
            "image_data/estimations.json",
            "image_data/images.json",
            "image_data/pending_images.json",
            "images/image_default.jpg"
        )

        val assetFoldersToCopy = listOf(
            "fields/svg",
            "fields/background"
        )

        val foldersToCreateEmpty = listOf(
            "logs", "results"
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


    fun guessMimeType(fileName: String): String {
        return when {
            fileName.endsWith(".json") -> "application/json"
            fileName.endsWith(".svg") -> "image/svg+xml"
            fileName.endsWith(".jpeg") || fileName.endsWith(".jpg") -> "image/jpeg"
            else -> "application/octet-stream"
        }
    }


//==================
// 📍 Location File Resolvers
//==================

    fun getBackgroundUriForLocation(context: Context, baseUri: Uri, location: Location): Uri? {
        val fileName = location.field.backgroundImageUrl
            ?.takeIf { it.isNotBlank() }
            ?.substringAfterLast("/")
            ?: return null // nothing to load or inject

        val rootDoc = DocumentFile.fromTreeUri(context, baseUri)
        val backgroundDir = rootDoc?.findFile("fields")?.findFile("background")

        val target = backgroundDir?.findFile(fileName)
        if (target == null) {
            Log.w("BackgroundUri", "⚠️ Background image '$fileName' not found.")
        }

        return target?.uri
    }


    fun getSvgUriForLocation(context: Context, baseUri: Uri, location: Location): Uri? {
        val fileName = location.field.svgMapUrl?.substringAfterLast("/") ?: "default_map.svg"
        val rootDoc = DocumentFile.fromTreeUri(context, baseUri)
        val svgDir = rootDoc?.findFile("fields")?.findFile("svg")

        val target = svgDir?.findFile(fileName)
        if (target == null) {
            Log.e("SvgUri", "❌ SVG file '$fileName' not found. Falling back to default.")
        }

        return target?.uri ?: svgDir?.findFile("default_map.svg")?.uri
    }








}