package id.ac.ugm.fahris.sobatkendara.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import kotlin.math.roundToInt
import kotlin.math.sqrt

class FusionSpeedCalculator(context: Context) : SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    private var lastUpdateTime: Long = 0L
    private var velocity: Double = 0.0
    private var gpsSpeed: Double = 0.0

    private var isGpsAvailable = false

    private var lastLocation: Location? = null
    private var distance: Double = 0.0
    private var isJourneyActive = false
    private var stationaryTime = 0L // Time spent stationary

    private val stationThreshold = 10000L // 10 seconds (adjust as needed)
    private val movementThreshold = 0.3 // m/s (adjust for "start moving" detection)

    // Start listening to sensors and GPS
    fun start() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        startGpsUpdates()
    }

    // Stop listening to sensors and GPS
    fun stop() {
        sensorManager.unregisterListener(this)
        stopGpsUpdates()
    }

    // Get the current fused speed
    fun getSpeed(): Double {
        return if (isGpsAvailable) gpsSpeed else velocity
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_LINEAR_ACCELERATION) return

        val currentTime = System.currentTimeMillis()
        if (lastUpdateTime == 0L) {
            lastUpdateTime = currentTime
            return
        }

        // Time difference (delta t) in seconds
        val deltaTime = (currentTime - lastUpdateTime) / 1000.0
        lastUpdateTime = currentTime

        // Get linear acceleration (x, y, z)
        val ax = event.values[0].toDouble()
        val ay = event.values[1].toDouble()
        val az = event.values[2].toDouble()

        // Calculate the magnitude of acceleration
        val acceleration = sqrt(ax * ax + ay * ay + az * az)

        // Integrate acceleration to calculate velocity
        velocity += acceleration * deltaTime

        // Apply damping to reduce error
        velocity *= 0.99
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No implementation needed
    }

    @Suppress("MissingPermission")
    private fun startGpsUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 2000 // 2 seconds
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation ?: return
                    gpsSpeed = (location.speed * 3.6).roundToInt().toDouble() // Convert m/s to km/h
                    isGpsAvailable = true
                    onLocationUpdate(location)
                }
            },
            Looper.getMainLooper()
        )
    }

    private fun stopGpsUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(object : LocationCallback() {})
    }
    // Update this to calculate distance
    fun onLocationUpdate(location: Location) {
        if (lastLocation != null) {
            val speed = location.speed // Speed in m/s
            if (speed > movementThreshold) {
                isJourneyActive = true
                stationaryTime = 0L
            } else {
                if (isJourneyActive) {
                    stationaryTime += 2000L // Location update interval
                    if (stationaryTime >= stationThreshold) {
                        // Journey has ended
                        isJourneyActive = false
                        resetJourney()
                    }
                }
            }

            if (isJourneyActive) {
                distance += lastLocation!!.distanceTo(location) / 1000.0 // Convert to kilometers
            }
        }

        lastLocation = location
    }

    fun getDistance(): Double {
        return distance
    }
    fun isJourneyActive(): Boolean {
        return isJourneyActive
    }

    private fun resetJourney() {
        distance = 0.0
        stationaryTime = 0L
    }
}