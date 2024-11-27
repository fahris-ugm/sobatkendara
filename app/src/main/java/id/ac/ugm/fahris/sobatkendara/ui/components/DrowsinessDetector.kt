package id.ac.ugm.fahris.sobatkendara.ui.components

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlin.math.abs

class DrowsinessDetector(
    private val silenceThreshold: Int = 500, // Adjust based on audio amplitude
    private val silenceDurationThreshold: Long = 5000L, // 5 seconds of silence
    private val sampleRate: Int = 44100
) {
    private var isDrowsy = false
    private var lastActiveTime = System.currentTimeMillis()
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var onDrowsinessChanged: ((Boolean) -> Unit)? = null

    // Start capturing audio
    @SuppressLint("MissingPermission")
    fun start(onDrowsinessChanged: (Boolean) -> Unit) {
        this.onDrowsinessChanged = onDrowsinessChanged
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

        if (audioRecord != null) {
            audioRecord?.startRecording()
            isRecording = true

            // Start a thread to analyze audio data
            Thread {
                val buffer = ShortArray(bufferSize)
                while (isRecording) {
                    val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (readSize > 0) {
                        processAudioData(buffer, readSize)
                    }
                }
            }.start()
        }
    }

    // Stop capturing audio
    fun stop() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    // Process audio data to detect drowsiness
    private fun processAudioData(buffer: ShortArray, readSize: Int) {
        val currentTime = System.currentTimeMillis()
        val maxAmplitude = buffer.take(readSize).map { abs(it.toInt()) }.maxOrNull() ?: 0

        if (maxAmplitude > silenceThreshold) {
            // Active audio detected
            lastActiveTime = currentTime
            if (isDrowsy) {
                isDrowsy = false
                onDrowsinessChanged?.invoke(false) // Hide warning
            }
        } else {
            // Silence detected
            if (currentTime - lastActiveTime >= silenceDurationThreshold) {
                if (!isDrowsy) {
                    isDrowsy = true
                    onDrowsinessChanged?.invoke(true) // Show warning
                }
            }
        }
    }
}