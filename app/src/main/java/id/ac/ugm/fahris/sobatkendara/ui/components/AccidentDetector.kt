package id.ac.ugm.fahris.sobatkendara.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.*

class AccidentDetector(
    context: Context,
    private val accelerationThreshold: Float = 25.0f, // Adjust as needed (m/s²)
    private val angularVelocityThreshold: Float = 15.0f // Adjust as needed (rad/s)
) : SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private var onAccidentDetected: (() -> Unit)? = null

    fun start(onAccidentDetected: () -> Unit) {
        this.onAccidentDetected = onAccidentDetected
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                val magnitude = event.values.map { it * it }.sum().let { Math.sqrt(it.toDouble()) }
                if (magnitude > accelerationThreshold) {
                    onAccidentDetected?.invoke()
                }
            }
            Sensor.TYPE_GYROSCOPE -> {
                val angularVelocity = event.values.map { it * it }.sum().let { Math.sqrt(it.toDouble()) }
                if (angularVelocity > angularVelocityThreshold) {
                    onAccidentDetected?.invoke()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No implementation needed
    }
}