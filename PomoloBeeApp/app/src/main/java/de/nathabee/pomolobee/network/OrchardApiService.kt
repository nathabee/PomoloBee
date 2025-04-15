package de.nathabee.pomolobee.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object OrchardApiService {
    suspend fun fetchLocations(apiUrl: String): String = withContext(Dispatchers.IO) {
        val fullUrl = "$apiUrl/locations/"
        Log.d("ApiService", "🌐 Fetching locations from $fullUrl")

        (URL(fullUrl).openConnection() as? HttpURLConnection)?.run {
            requestMethod = "GET"
            connect()

            if (responseCode != 200) {
                Log.e("ApiService", "❌ Failed to fetch locations: $responseCode")
                throw Exception("❌ Failed to fetch locations: $responseCode")
            }

            inputStream.bufferedReader().use { it.readText() }
        } ?: throw Exception("❌ Invalid connection for locations")
    }

    suspend fun fetchFruits(apiUrl: String): String = withContext(Dispatchers.IO) {
        val fullUrl = "$apiUrl/fruits/"
        Log.d("ApiService", "🌐 Fetching fruits from $fullUrl")

        (URL(fullUrl).openConnection() as? HttpURLConnection)?.run {
            requestMethod = "GET"
            connect()

            if (responseCode != 200) {
                Log.e("ApiService", "❌ Failed to fetch fruits: $responseCode")
                throw Exception("❌ Failed to fetch fruits: $responseCode")
            }

            inputStream.bufferedReader().use { it.readText() }
        } ?: throw Exception("❌ Invalid connection for fruits")
    }


    suspend fun fetchBinaryFromUrl(url: String): ByteArray = withContext(Dispatchers.IO) {
        Log.d("fetchBinaryFromUrl", "🌐 Fetching Binary from $url")
        val connection = URL(url).openConnection() as? HttpURLConnection
            ?: throw Exception("❌ Invalid connection for: $url")

        connection.requestMethod = "GET"
        connection.connect()

        if (connection.responseCode != 200) {
            Log.e("fetchBinaryFromUrl", "❌ Failed to fetch binary: ${connection.responseCode}")
            throw Exception("❌ Failed to fetch file: ${connection.responseCode}")
        }

        readBytes(connection.inputStream)
    }


    private fun readBytes(inputStream: InputStream): ByteArray {
        val buffer = ByteArrayOutputStream()
        inputStream.use { input ->
            val data = ByteArray(4096)
            var count: Int
            while (input.read(data).also { count = it } != -1) {
                buffer.write(data, 0, count)
            }
        }
        return buffer.toByteArray()
    }
}
