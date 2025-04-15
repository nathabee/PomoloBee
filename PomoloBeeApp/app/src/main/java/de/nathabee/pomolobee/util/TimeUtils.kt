package de.nathabee.pomolobee.util
import java.text.SimpleDateFormat
import java.util.*


object TimeUtils {
    fun formatTimestamp(timestamp: Long?): String {
        if (timestamp == null) return "Never"
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}




