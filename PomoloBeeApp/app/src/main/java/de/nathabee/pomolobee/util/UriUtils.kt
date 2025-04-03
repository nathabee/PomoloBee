package de.nathabee.pomolobee.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import de.nathabee.pomolobee.model.Location

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


fun getFriendlyFolderName(context: Context, uri: Uri): String {
    val docFile = DocumentFile.fromTreeUri(context, uri)
    val folderName = docFile?.name ?: "Unknown Folder"

    return if (uri.toString().contains("externalstorage")) {
        "SD-Karte/$folderName"
    } else {
        "Interner Speicher/$folderName"
    }
}
