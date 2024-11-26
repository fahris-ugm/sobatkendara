package id.ac.ugm.fahris.sobatkendara.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class SpeedCalculator(context: Context) : SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    private var lastUpdateTime: Long = 0
    private var velocity: Double = 0.0

    // Start listening to accelerometer data
    fun start() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    // Stop listening to accelerometer data
    fun stop() {
        sensorManager.unregisterListener(this)
    }

    // Get the current speed
    fun getSpeed(): Double {
        return velocity
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val currentTime = System.currentTimeMillis()
        if (lastUpdateTime == 0L) {
            lastUpdateTime = currentTime
            return
        }

        // Time difference (delta t) in seconds
        val deltaTime = (currentTime - lastUpdateTime) / 1000.0
        lastUpdateTime = currentTime

        // Get the linear acceleration (x, y, z)
        val ax = event.values[0].toDouble()
        val ay = event.values[1].toDouble()
        val az = event.values[2].toDouble()

        // Calculate the magnitude of acceleration
        val acceleration = sqrt(ax * ax + ay * ay + az * az)

        // Integrate acceleration to calculate velocity
        velocity += acceleration * deltaTime

        // Apply friction or damping to reduce error accumulation
        velocity *= 0.99 // Adjust damping factor as needed
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No implementation needed
    }
}