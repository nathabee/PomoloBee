package de.nathabee.pomolobee.util


import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import de.nathabee.pomolobee.cache.OrchardCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

object ErrorLogger {


    fun logError(context: Context, message: String, throwable: Throwable? = null) {
        val rootUri = OrchardCache.currentRootUri
        if (rootUri != null) {
            logError(context, rootUri, message, throwable)
        } else {
            Log.e("ErrorLogger", "⚠️ No rootUri available in cache")
            Log.e("ErrorLogger", message, throwable)
        }
    }

    fun logError(context: Context, rootUri: Uri?, message: String, throwable: Throwable? = null) {
            Log.d("ErrorLogger", "📌 Entered logError() with message: $message")

        if (rootUri == null) {
            Log.w("ErrorLogger", "⚠️ No rootUri provided, cannot write error.")
            return
        }

        val rootDir = DocumentFile.fromTreeUri(context, rootUri)
        val logsDir = rootDir?.findFile("logs") ?: rootDir?.createDirectory("logs")
        val logFile = logsDir?.findFile("errors.json")
            ?: logsDir?.createFile("application/json", "errors.json")

        if (logFile == null) {
            Log.e("ErrorLogger", "❌ Could not create or access errors.json")
            return
        }

        val resolver = context.contentResolver
        val logsArray = try {
            resolver.openInputStream(logFile.uri)?.use { input ->
                val existing = BufferedReader(InputStreamReader(input)).readText()
                JSONArray(existing)
            } ?: JSONArray()
        } catch (e: Exception) {
            JSONArray()
        }

        val entry = JSONObject().apply {
            put("timestamp", getCurrentTime())
            put("message", message)
            put("exception", throwable?.stackTraceToString() ?: "")
        }

        logsArray.put(entry)

        resolver.openOutputStream(logFile.uri, "w")?.use { output ->
            output.write(logsArray.toString(2).toByteArray())
        }
    }

    private fun getCurrentTime(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }
}


// in util/ErrorLogger.kt
fun readErrors(context: Context, storageRoot: Uri? = OrchardCache.currentRootUri): List<String> {

        if (storageRoot == null) return listOf("❌ No storage root set.")

    val rootDir = DocumentFile.fromTreeUri(context, storageRoot)
    val logsDir = rootDir?.findFile("logs")
    val logFile = logsDir?.findFile("errors.json") ?: return listOf("📭 No error log found.")

    val resolver = context.contentResolver
    return try {
        resolver.openInputStream(logFile.uri)?.use { input ->
            val json = BufferedReader(InputStreamReader(input)).readText()
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                "🕒 ${obj.getString("timestamp")}\n📄 ${obj.getString("message")}\n${obj.getString("exception")}"
            }
        } ?: listOf("📭 Log file empty")
    } catch (e: Exception) {
        listOf("⚠ Failed to read log: ${e.message}")
    }
}

fun safeLaunch(
    context: Context,
    storageRoot: Uri?,
    block: suspend () -> Unit
) = CoroutineScope(Dispatchers.IO).launch {
    try {
        block()
    } catch (e: Exception) {
        ErrorLogger.logError(context, storageRoot, "❌ Operation failed", e)
    }
}


