package id.ac.ugm.fahris.sobatkendara.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.google.android.gms.location.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import id.ac.ugm.fahris.sobatkendara.R
import id.ac.ugm.fahris.sobatkendara.service.AlertRequest
import id.ac.ugm.fahris.sobatkendara.service.ApiService
import id.ac.ugm.fahris.sobatkendara.service.GeocodingApiService
import id.ac.ugm.fahris.sobatkendara.ui.components.AccidentDetector
import id.ac.ugm.fahris.sobatkendara.ui.components.AmbientLightMonitor


import id.ac.ugm.fahris.sobatkendara.ui.components.AppBar
import id.ac.ugm.fahris.sobatkendara.ui.components.DirectionCalculator
import id.ac.ugm.fahris.sobatkendara.ui.components.DrowsinessDetector
import id.ac.ugm.fahris.sobatkendara.ui.components.FusionSpeedCalculator
import id.ac.ugm.fahris.sobatkendara.ui.components.SpeedCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DashboardScreen(
    drawerState: DrawerState,
) {

    val context = LocalContext.current
    var currentLocation by rememberSaveable { mutableStateOf("Fetching location...") }
    var speed by rememberSaveable { mutableStateOf("0") }
    var distance by rememberSaveable { mutableStateOf("0.0") }
    var timeElapsed by rememberSaveable { mutableStateOf("00:00:00") }
    var compassDirection by rememberSaveable { mutableStateOf("N") }
    var compassBearing by rememberSaveable { mutableStateOf(0f) }
    var isPermissionGranted by remember { mutableStateOf(false) }
    var isShowHeadlightAlert by rememberSaveable { mutableStateOf(false)}
    var isShowDrowsinessAlert by rememberSaveable { mutableStateOf(false)}
    var isShowAccidentDialog by rememberSaveable { mutableStateOf(false) }

    var isJourneyActive by rememberSaveable { mutableStateOf(false) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val geocodingApi = remember { GeocodingApiService.create() }
    val coroutineScope = rememberCoroutineScope()
    val drowsinessDetector = remember { DrowsinessDetector() }

    // wrapper to handle bug of android studio failed to render permissions, sensormanager
    val isInspection = LocalInspectionMode.current
    lateinit var permissionState: MultiplePermissionsState

    // Format elapsed time
    fun formatElapsedTime(timeMillis: Long): String {
        val seconds = (timeMillis / 1000) % 60
        val minutes = (timeMillis / (1000 * 60)) % 60
        val hours = (timeMillis / (1000 * 60 * 60))
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
    // Timer Coroutine
    suspend fun startTimer() {
        var elapsedTime = 0L
        while (isJourneyActive) {
            delay(1000)
            elapsedTime += 1000
            timeElapsed = formatElapsedTime(elapsedTime)
        }
    }

    if (isInspection) {
        permissionState = object : MultiplePermissionsState {
            override val allPermissionsGranted: Boolean = false
            override val permissions: List<PermissionState> = emptyList()
            override val revokedPermissions: List<PermissionState> = emptyList()
            override val shouldShowRationale: Boolean = false
            override fun launchMultiplePermissionRequest() {}
        }
    } else {
        permissionState = rememberMultiplePermissionsState(
            permissions = listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.RECORD_AUDIO
            )
        )

    }

    // Check permissions
    LaunchedEffect(Unit) {
        isPermissionGranted = permissionState.allPermissionsGranted
        if (!isPermissionGranted) {
            permissionState.launchMultiplePermissionRequest()
        }
    }
    // Start listening for location updates if permission is granted
    LaunchedEffect(isPermissionGranted) {
        if (isPermissionGranted) {
            startLocationUpdates(
                fusedLocationClient = fusedLocationClient,
                onLocationUpdate = { location ->
                    coroutineScope.launch {
                        currentLocation = getAddressFromLocation(context, location, geocodingApi = geocodingApi)
                        // Delegated to FusionSpeedCalculator
                        //speed = "${(location.speed * 3.6).roundToInt()}" // Convert m/s to km/h
                        // Delegated to DirectionCalculator
                        //compassDirection = getCompassDirection(location.bearing)
                        //compassBearing = 360 - location.bearing
                    }
                }
            )
        }
    }
    if (!isInspection) {
        //val speedCalculator = remember { SpeedCalculator(context) }
        val speedCalculator = remember { FusionSpeedCalculator(context) }
        // Start and stop speed calculation
        DisposableEffect(Unit) {
            speedCalculator.start()
            onDispose {
                speedCalculator.stop()
            }
        }
        // Update speed periodically
        LaunchedEffect(Unit) {
            while (true) {
                delay(1000) // Update every second
                speed = "${speedCalculator.getSpeed().toInt()}"

                distance = String.format("%.3f",speedCalculator.getDistance())
                // Update journey state
                val journeyActive = speedCalculator.isJourneyActive()
                if (journeyActive != isJourneyActive) {
                    isJourneyActive = journeyActive
                    if (isJourneyActive) {
                        coroutineScope.launch { startTimer() }
                    }
                }
            }
        }

        val directionCalculator = remember { DirectionCalculator(context) }
        // Start and stop direction calculation
        DisposableEffect(Unit) {
            directionCalculator.start { direction ->
                compassDirection = getCompassDirection(direction)
                compassBearing = 360 - direction
            }
            onDispose {
                directionCalculator.stop()
            }
        }

        val ambientLightMonitor = remember {
            AmbientLightMonitor(
                context = context,
                lowLightThreshold = 10.0f, // Adjust threshold as needed
                durationThreshold = 5000L // 5 seconds
            )
        }
        // Start and stop ambient light monitoring
        DisposableEffect(Unit) {
            ambientLightMonitor.start { warningState ->
                Log.d("DashboardScreen", "Warning state changed: $warningState")
                isShowHeadlightAlert = warningState
            }
            onDispose {
                ambientLightMonitor.stop()
            }
        }


        // Start and stop drowsiness detection
        DisposableEffect(Unit) {
            drowsinessDetector.start { isDrowsy ->
                isShowDrowsinessAlert = isDrowsy
                if (isDrowsy) {
                    playAlarm(context)
                    startVoiceRecognition(context) {
                        // Stop the alarm
                        isShowDrowsinessAlert = false
                        drowsinessDetector.setIsDrowsy(false)
                        stopAlarm()
                    }
                } else {
                    drowsinessDetector.setIsDrowsy(false)
                    stopAlarm()
                }
            }
            onDispose {
                drowsinessDetector.stop()
            }
        }

        val accidentDetector = remember { AccidentDetector(context) }
        // Start and stop accident detection
        DisposableEffect(Unit) {
            accidentDetector.start {
                // Accident detected
                coroutineScope.launch {
                    val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                    val token = sharedPreferences.getString("auth_token", null)
                    val alert: AlertRequest = AlertRequest(
                        Date(),
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        "unknown"
                    )

                    ApiService.sendAlert(context, token?:"", alert, onSendAlertError = {message ->
                        Toast.makeText(context, "Failed to send alert: $message", Toast.LENGTH_SHORT).show()
                    })
                }
                isShowAccidentDialog = true
            }
            onDispose {
                accidentDetector.stop()
            }
        }
    }

    Scaffold(
        topBar = { AppBar(drawerState = drawerState, title = R.string.app_name) }
    ) {

        Column(
            modifier = Modifier.fillMaxSize().padding(it).padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            if (isShowAccidentDialog) {
                AccidentWarningDialog { isShowAccidentDialog = false }
            }
            // Warning Text
            if (isShowHeadlightAlert) {
                Box(
                    modifier = Modifier.padding(
                        start = 4.dp,
                        end = 4.dp,
                        top = 4.dp,
                        bottom = 4.dp
                    ).background(Color.Red)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        //modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,

                        ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_carlight_white_24dp),
                            contentDescription = "Car Light",
                            modifier = Modifier.padding(start = 16.dp, end = 8.dp).size(24.dp)
                        )
                        Text(
                            text = "Please switch on your headlights!",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,

                            color = Color.White,
                            modifier = Modifier.padding(end = 16.dp),

                            )
                    }
                }
            }

            if (isShowDrowsinessAlert) {
                Box(
                    modifier = Modifier.padding(
                        start = 4.dp,
                        end = 4.dp,
                        top = 4.dp,
                        bottom = 4.dp
                    ).background(Color.Red).clickable {
                        isShowDrowsinessAlert = false
                        drowsinessDetector.setIsDrowsy(false)
                        stopAlarm()
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,

                        ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_sleep_white_24dp),
                            contentDescription = "Car Light",
                            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 2.dp, bottom = 2.dp).size(24.dp)
                        )
                        Text(
                            text = "Drowsiness detected! Please take a break.",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,

                            color = Color.White,
                            modifier = Modifier.padding(end = 16.dp),

                            )
                    }
                }
            }

            // Current Location
            Text(
                text = currentLocation,
                fontSize = 14.sp,
                minLines = 3,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            // Current Speed

            SpeedDisplay(speed)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Time Elapsed Information
                Text(
                    text = buildAnnotatedString {
                        append("Time elapsed\n")
                        withStyle(style = SpanStyle(fontSize = 32.sp)) { // Smaller font size for "km/h"
                            append(timeElapsed)
                        }
                    },
                    lineHeight = 40.sp,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                // Distance Information
                Text(
                    text = buildAnnotatedString {
                        append("Distance\n")
                        withStyle(style = SpanStyle(fontSize = 32.sp)) { // Smaller font size for "km/h"
                            append(distance)
                        }
                        append(" km")
                    },
                    lineHeight = 40.sp,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Compass Direction
            Text(
                text = compassDirection,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Image(
                painter = painterResource(id = R.drawable.ic_compass_rect),
                contentDescription = "compass",
                modifier = Modifier.padding(bottom = 16.dp, start = 32.dp, end = 32.dp).size(120.dp).rotate(compassBearing),
                contentScale = ContentScale.Fit
            )
        }

    }
}
@Preview
@Composable
fun DashboardScreenPreview() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    DashboardScreen(drawerState)
}

@Composable
fun SpeedDisplay(speed: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(2.dp)
    ) {
        Text(
            text = speed,
            fontSize = 96.sp, // Large font size for the speed number
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = "km/h",
            fontSize = 16.sp // Smaller font size for "km/h"
        )
    }
}
// Function to start location updates
@SuppressLint("MissingPermission")
fun startLocationUpdates(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationUpdate: (Location) -> Unit
) {
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000).setMinUpdateIntervalMillis(1000).build()

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val loc = locationResult.lastLocation

            //Log.d("DashboardScreen", "onLocationResult ${loc.toString()}")
            locationResult.lastLocation?.let(onLocationUpdate)
        }
    }

    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
}

suspend fun getAddressFromLocation(context: Context, location: Location, geocodingApi: GeocodingApiService, isGoogleAPI: Boolean = false): String {
    if (isGoogleAPI) {
        return getAddressFromGoogleApi(context, location, geocodingApi)
    } else {
        return getAddressFromGeocoder(context, location)
    }
}
// Function to get a human-readable address from a location
fun getAddressFromGeocoder(context: Context, location: Location): String {
    val geocoder = Geocoder(context, Locale.getDefault())
    return try {
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        //addresses?.firstOrNull()?.featureName ?: "Unknown location"
        val province = addresses?.firstOrNull()?.adminArea?:""
        val feature = addresses?.firstOrNull()?.featureName?:""
        val subLocality = addresses?.firstOrNull()?.subLocality?:""
        val thoroughfare = addresses?.firstOrNull()?.thoroughfare?:""
        val subThoroughfare = addresses?.firstOrNull()?.subThoroughfare?:""
        //addresses?.firstOrNull()?.thoroughfare ?: "Unknown location"
        "$province\n $feature $subLocality $thoroughfare $subThoroughfare"
    } catch (e: Exception) {
        Log.d("DashboardScreen", "error: ${e.message}")
        return "Unknown location"
    }
}
suspend fun getAddressFromGoogleApi(
    context: Context,
    location: Location,
    geocodingApi: GeocodingApiService
): String {
    return try {
        val latitude = location.latitude
        val longitude = location.longitude
        val metadata = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA).metaData
        val googleApiKey = metadata.getString("com.google.android.geo.API_KEY")
        val response = geocodingApi.getAddress("$latitude,$longitude", googleApiKey?:"")
        if (response.status == "OK" && response.results.isNotEmpty()) {
            response.results.first().formatted_address
        } else {
            "Unknown location"
        }
    } catch (e: Exception) {
        "Error fetching location"
    }
}

// Function to convert bearing to compass direction
fun getCompassDirection(bearing: Float): String {
    return when (((bearing + 360) % 360) / 45) {
        in 0.0..1.0 -> "N"
        in 1.0..2.0 -> "NE"
        in 2.0..3.0 -> "E"
        in 3.0..4.0 -> "SE"
        in 4.0..5.0 -> "S"
        in 5.0..6.0 -> "SW"
        in 6.0..7.0 -> "W"
        else -> "NW"
    }
}

@Composable
fun AccidentWarningDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Accident Detected!") },
        text = { Text("An accident has been detected. Emergency services have been notified.") },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
private var mediaPlayer: MediaPlayer? = null

fun playAlarm(context: Context) {
    Log.d("DashboardScreen", "playAlarm")
    if (mediaPlayer == null) {
        var alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (alert == null) {
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            if (alert == null) {
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }
        }
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setAudioStreamType(AudioManager.STREAM_ALARM)
        mediaPlayer?.setDataSource(context, alert)
        mediaPlayer?.prepare()
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

}
fun stopAlarm() {
    mediaPlayer?.let {
        if (it.isPlaying) {
            it.stop() // Stop playback
            it.release() // Release resources
        }
    }
    mediaPlayer = null // Reset the MediaPlayer instance
}

fun startVoiceRecognition(context: Context, onStopAlarm: () -> Unit) {
    val mainHandler = Handler(Looper.getMainLooper())
    mainHandler.post {
        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.forEach {
                    Log.d("DashboardScreen", "onResults: $it")
                }
                if (matches?.any { it.contains("awake", ignoreCase = true) } == true) {
                    onStopAlarm() // Stop the alarm
                }
            }

            // Implement other methods
            override fun onError(error: Int) {
                println("SpeechRecognizer error: $error")
            }

            override fun onBeginningOfSpeech() {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
        })

        speechRecognizer.startListening(intent)
    }
}