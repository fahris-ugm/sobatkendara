package id.ac.ugm.fahris.sobatkendara.service

import java.text.SimpleDateFormat
import java.util.*

data class AlertRequest(
    val eventTimestamp: Date,
    val soundDecibelValue: Double,
    val soundDecibelThreshold: Double,
    val shakeValue: Double,
    val shakeThreshold: Double,
    val gpsLocation: String
) {
    // Alternate constructor to parse timestamp string
    constructor(
        eventTimestampString: String,
        soundDecibelValue: Double,
        soundDecibelThreshold: Double,
        shakeValue: Double,
        shakeThreshold: Double,
        gpsLocation: String
    ) : this(
        eventTimestamp = parseTimestamp(eventTimestampString),
        soundDecibelValue = soundDecibelValue,
        soundDecibelThreshold = soundDecibelThreshold,
        shakeValue = shakeValue,
        shakeThreshold = shakeThreshold,
        gpsLocation = gpsLocation
    )

    companion object {
        // Helper method to parse the timestamp string
        private fun parseTimestamp(timestampString: String): Date {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            return try {
                dateFormat.parse(timestampString) ?: throw IllegalArgumentException("Invalid timestamp format")
            } catch (e: Exception) {
                throw IllegalArgumentException("Error parsing timestamp: ${e.message}")
            }
        }
    }
}