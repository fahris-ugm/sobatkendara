package id.ac.ugm.fahris.sobatkendara.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class AmbientLightMonitor(
    context: Context,
    private val lowLightThreshold: Float = 10.0f, // Example threshold (in lux)
    private val durationThreshold: Long = 5000L // Time in milliseconds
) : SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    private var isLowLight = false
    private var lastLowLightTime = 0L
    private var lastHighLightTime = 0L
    private var onWarningChanged: ((Boolean) -> Unit)? = null

    // Start monitoring light levels
    fun start(onWarningChanged: (Boolean) -> Unit) {
        this.onWarningChanged = onWarningChanged
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    // Stop monitoring light levels
    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_LIGHT) return

        val currentTime = System.currentTimeMillis()
        val lightLevel = event.values[0] // Ambient light level in lux

        //Log.d("AmbientLightMonitor", "Light Level: $lightLevel")
        if (lightLevel < lowLightThreshold) {
            //Log.d("AmbientLightMonitor", "Light Level: $lightLevel < $lowLightThreshold current: $currentTime last: $lastLowLightTime duration: ${currentTime - lastLowLightTime}")
            lastHighLightTime = 0L
            if (lastLowLightTime == 0L) {
                lastLowLightTime = currentTime
            }
            if (!isLowLight) {
                if (currentTime - lastLowLightTime >= durationThreshold) {
                    isLowLight = true
                    onWarningChanged?.invoke(true) // Show warning
                }
            }
        } else {
            //Log.d("AmbientLightMonitor", "Light Level: $lightLevel > $lowLightThreshold lastHighLightTime: $lastHighLightTime")
            lastLowLightTime = 0L
            if (lastHighLightTime == 0L) {
                lastHighLightTime = currentTime
            }
            if (isLowLight) {
                if (currentTime - lastHighLightTime >= durationThreshold) {
                    isLowLight = false
                    onWarningChanged?.invoke(false) // Hide warning
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No implementation needed
    }
}