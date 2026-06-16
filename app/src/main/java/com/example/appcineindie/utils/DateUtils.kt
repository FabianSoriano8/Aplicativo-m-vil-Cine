package com.example.appcineindie.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    /**
     * Formatea un timestamp de Firebase o un Long a un string legible.
     */
    fun formatTimestamp(timestamp: Any?): String {
        return try {
            val millis = when (timestamp) {
                is String -> timestamp.toLongOrNull() ?: 0L
                is Long -> timestamp
                is Timestamp -> timestamp.toDate().time
                else -> 0L
            }
            if (millis <= 0L) return ""

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            sdf.format(Date(millis))
        } catch (e: Exception) {
            ""
        }
    }
}
