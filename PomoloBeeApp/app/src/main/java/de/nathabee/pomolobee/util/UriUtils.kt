package de.nathabee.pomolobee.util

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import de.nathabee.pomolobee.model.Location

fun getSvgUriForLocation(context: Context, baseUri: Uri, location: Location): Uri? {
    val fileName = location.field.svgMapUrl?.substringAfterLast("/") ?: "default_map.svg"
    val rootDoc = DocumentFile.fromTreeUri(context, baseUri)
    val svgDir = rootDoc?.findFile("fields")?.findFile("svg")

    // TODO: If fileDoc is null and svgMapUrl is defined, try downloading from backend later.


    return svgDir?.findFile(fileName)?.uri ?: svgDir?.findFile("default_map.svg")?.uri
}