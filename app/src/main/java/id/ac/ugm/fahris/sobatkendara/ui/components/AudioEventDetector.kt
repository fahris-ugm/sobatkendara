package id.ac.ugm.fahris.sobatkendara.ui.components

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AudioEventDetector(
    private val accidentThreshold: Int = 20000,
    private val silenceThreshold: Int = 300, // Adjust based on audio amplitude
    private val silenceDurationThreshold: Long = 5000L, // 5 seconds of silence
    private val awakeDurationThreshold: Long = 2000L, // 5 seconds of silence
    private val sampleRate: Int = 44100
) {
    private var isDrowsy = false
    private var lastActiveTime = 0L //System.currentTimeMillis()
    private var lastSilenceTime = 0L
    private var lastAccidentTime = 0L
    private val accidentDurationThreshold = 1000L * 60 * 60 // 1 hour
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var onDrowsinessChanged: ((Boolean) -> Unit)? = null
    private var onAccidentDetected: ((threshold: Double, value: Double) -> Unit)? = null

    // Start capturing audio
    @SuppressLint("MissingPermission")
    fun start(
        silenceThreshold: Int = 300,
        accidentThreshold: Int = 20000,
        onDrowsinessChanged: (Boolean) -> Unit,
        onAccidentDetected: (threshold: Double, value: Double) -> Unit
    ) {
        this.onDrowsinessChanged = onDrowsinessChanged
        this.onAccidentDetected = onAccidentDetected
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
        try {
            audioRecord?.startRecording()
            isRecording = true

            CoroutineScope(Dispatchers.IO).launch {
                val buffer = ShortArray(bufferSize)
                while (isRecording) {
                    val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (readSize > 0) {
                        val currentTime = System.currentTimeMillis()
                        val maxAmplitude = buffer.take(readSize).map { kotlin.math.abs(it.toInt()) }.maxOrNull() ?: 0
                        //Log.d("DrowsinessDetector", "Max Amplitude: $maxAmplitude")
                        if (maxAmplitude > accidentThreshold) {
                            if (currentTime - lastAccidentTime >= accidentDurationThreshold) {
                                lastAccidentTime = currentTime
                                onAccidentDetected.invoke(accidentThreshold.toDouble(), maxAmplitude.toDouble())
                            }

                        }
                        if (maxAmplitude < silenceThreshold) {
                            lastActiveTime = 0L
                            if (lastSilenceTime == 0L) {
                                lastSilenceTime = currentTime
                            }
                            if (!isDrowsy) {
                                if (currentTime - lastSilenceTime >= silenceDurationThreshold) {
                                    // Silence detected for threshold duration
                                    isDrowsy = true
                                    onDrowsinessChanged.invoke(true)
                                }
                            }

                        } else {
                            lastSilenceTime = 0L
                            if (lastActiveTime == 0L) {
                                lastActiveTime = currentTime
                            }
                            if (isDrowsy) {
                                if (currentTime - lastActiveTime >= awakeDurationThreshold) {
                                    // Silence detected for threshold duration
                                    isDrowsy = false
                                    onDrowsinessChanged.invoke(false)
                                }
                            }
                        }
                    }
                    delay(100) // Check every 100ms
                }
            }
        } catch (e: Exception) {
            Log.d("DrowsinessDetector", "Error starting audio recording: ${e.message}")
        }
    }

    // Stop capturing audio
    fun stop() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
    fun setIsDrowsy(flag: Boolean) {
        isDrowsy = flag
        lastSilenceTime = 0L
        lastActiveTime = 0L
    }
}