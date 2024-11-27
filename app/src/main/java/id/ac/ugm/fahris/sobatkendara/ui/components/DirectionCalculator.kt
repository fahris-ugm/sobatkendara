package id.ac.ugm.fahris.sobatkendara.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.roundToInt

class DirectionCalculator(context: Context) : SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magneticField: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var azimuth: Float = 0.0f
    private var onDirectionChanged: ((Float) -> Unit)? = null

    // Start listening to sensors
    fun start(onDirectionChanged: (Float) -> Unit) {
        this.onDirectionChanged = onDirectionChanged
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magneticField?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    // Stop listening to sensors
    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, gravity, 0, gravity.size)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, geomagnetic, 0, geomagnetic.size)
            }
        }

        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
            SensorManager.getOrientation(rotationMatrix, orientation)
            azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            if (azimuth < 0) {
                azimuth += 360
            }

            // Convert azimuth to compass direction
            onDirectionChanged?.invoke(azimuth)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No implementation needed
    }

}