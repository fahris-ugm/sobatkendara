package id.ac.ugm.fahris.sobatkendara.service

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class UserProfile(
    val id: Int,
    val email: String,
    val additionalEmail: String?,
    val createdAt: Date
) {
    // Alternate constructor
    constructor(id: Int, email: String, additionalEmail: String?, timestampString: String) : this(
        id,
        email,
        additionalEmail,
        parseTimestamp(timestampString)
    )

    companion object {
        // Helper method to parse the timestamp string
        private fun parseTimestamp(timestampString: String): Date {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            return try {
                dateFormat.parse(timestampString) ?: throw IllegalArgumentException("Invalid timestamp format")
            } catch (e: Exception) {
                throw IllegalArgumentException("Error parsing timestamp: $e")
            }
        }
    }
}