package id.ac.ugm.fahris.sobatkendara.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import id.ac.ugm.fahris.sobatkendara.ui.components.AudioEventDetector
import id.ac.ugm.fahris.sobatkendara.ui.components.FusionSpeedCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DashboardScreen(
    drawerState: DrawerState,
) {

    val context = LocalContext.current
    //val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }
    val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

    var currentLocation by rememberSaveable { mutableStateOf(Location("")) }
    var currentLocationText by rememberSaveable { mutableStateOf("Fetching location...") }
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
    val audioEventDetector = remember { AudioEventDetector() }

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
    fun sendAlert(soundThreshold: Double = 0.0, soundValue: Double = 0.0, shakeThreshold: Double = 0.0, shakeValue: Double = 0.0) {
        coroutineScope.launch {

            val token = sharedPreferences.getString("auth_token", null)
            val location = currentLocation.latitude.toString() + "," + currentLocation.longitude.toString()
            Log.d("DashboardScreen", "soundThreshold: $soundThreshold, soundValue: $soundValue, shakeThreshold: $shakeThreshold, shakeValue: $shakeValue, location: $location")
            val alert = AlertRequest(
                eventTimestamp = Date(),
                soundDecibelValue =  soundValue,
                soundDecibelThreshold = soundThreshold,
                shakeValue = shakeValue,
                shakeThreshold = shakeThreshold,
                gpsLocation = location
            )

            ApiService.sendAlert(context, token?:"", alert, onSendAlertError = {message ->
                Toast.makeText(context, "Failed to send alert: $message", Toast.LENGTH_SHORT).show()
            })
        }
        isShowAccidentDialog = true
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
                Manifest.permission.RECORD_AUDIO,
                //Manifest.permission.NFC
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
                    currentLocation = location
                    coroutineScope.launch {
                        currentLocationText = getAddressFromLocation(context, location, geocodingApi = geocodingApi)
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
        // NFC
        /*
        if (isPermissionGranted) {
            DisposableEffect(Unit) {
                if (nfcAdapter != null && nfcAdapter.isEnabled) {
                    NFCReader(context) { cardBalance ->
                        //balance = cardBalance
                        Log.d("DashboardScreen", "cardBalance: $cardBalance")
                        Toast.makeText(context, "Card Balance: $cardBalance", Toast.LENGTH_LONG).show()
                    }
                }
                onDispose {

                }
            }

        }

         */
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

        val flagHeadlightAlert = sharedPreferences.getBoolean("flag_headlight_alert", true)
        if (flagHeadlightAlert) {
            val headlightThreshold = sharedPreferences.getFloat("headlight_threshold", 10f)
            val ambientLightMonitor = remember {
                AmbientLightMonitor(
                    context = context,
                    lowLightThreshold = headlightThreshold,
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
        }

        val flagDrowsinessAlert = sharedPreferences.getBoolean("flag_drowsiness_alert", true)
        val flagAccidentAudioAlert = sharedPreferences.getBoolean("flag_accident_audio_alert", true)
        val drowsinessThreshold = sharedPreferences.getFloat("drowsiness_threshold", 300f)
        val accidentSoundThreshold = sharedPreferences.getFloat("accident_sound_threshold", 20000f)
        // Start and stop drowsiness detection
        DisposableEffect(Unit) {
            audioEventDetector.start(
                silenceThreshold = drowsinessThreshold.toInt(),
                accidentThreshold = accidentSoundThreshold.toInt(),
                onDrowsinessChanged = { isDrowsy ->
                    isShowDrowsinessAlert = isDrowsy
                    if (isDrowsy && flagDrowsinessAlert) {
                        playAlarm(context)
                        startVoiceRecognition(context) {
                            // Stop the alarm
                            isShowDrowsinessAlert = false
                            audioEventDetector.setIsDrowsy(false)
                            stopAlarm()
                        }
                    } else {
                        audioEventDetector.setIsDrowsy(false)
                        stopAlarm()
                    }
                },
                onAccidentDetected = {
                        threshold, value ->
                    if (flagAccidentAudioAlert) {
                        sendAlert(soundThreshold = threshold, soundValue = value)
                    }
                }
            )
            onDispose {
                audioEventDetector.stop()
            }

        }

        val flagAccidentMovementAlert = sharedPreferences.getBoolean("flag_accident_movement_alert", true)
        if (flagAccidentMovementAlert) {
            val accidentShakinessThreshold = sharedPreferences.getFloat("accident_shakiness_threshold", 25f)
            val accidentDetector = remember { AccidentDetector(
                context,
                accelerationThreshold = accidentShakinessThreshold,
                angularVelocityThreshold = accidentShakinessThreshold
            ) }
            // Start and stop accident detection
            DisposableEffect(Unit) {
                accidentDetector.start {
                        threshold, value ->
                    // Accident detected
                    sendAlert(shakeThreshold = threshold, shakeValue = value)
                }
                onDispose {
                    accidentDetector.stop()
                }
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
                        audioEventDetector.setIsDrowsy(false)
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
                text = currentLocationText,
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

fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

fun NFCReader(context: Context, onBalanceRead: (String) -> Unit) {
    val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, context::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )

    val intentFilter = arrayOf(
        IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
        IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
        IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
    )

    val techList = arrayOf(
        arrayOf(android.nfc.tech.IsoDep::class.java.name)
    )

    nfcAdapter?.enableForegroundDispatch(
        context as Activity,
        pendingIntent,
        intentFilter,
        techList
    )

    context.registerReceiver(object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action == NfcAdapter.ACTION_TECH_DISCOVERED) {
                val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
                tag?.let {
                    val isoDep = IsoDep.get(it)
                    isoDep?.let {
                        try {
                            isoDep.connect()
                            val balance = readBalanceFromCard(isoDep)
                            onBalanceRead(balance)
                        } catch (e: Exception) {
                            onBalanceRead("Error reading balance")
                            e.printStackTrace()
                        } finally {
                            isoDep.close()
                        }
                    }
                }
            }
        }
    }, IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED))
}

fun readBalanceFromCard(isoDep: IsoDep): String {
    // Example APDU Command to Select Application
    val selectApplicationCommand = byteArrayOf(
        0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(), 0x07.toByte(),
        0xA0.toByte(), 0x00.toByte(), 0x00.toByte(), 0x06.toByte(), 0x47.toByte(), 0x2B.toByte(), 0x00.toByte()
    )

    val responseSelect = isoDep.transceive(selectApplicationCommand)
    if (!isSuccessResponse(responseSelect)) {
        return "Error: Failed to select application"
    }

    // Example APDU Command to Read Balance
    val readBalanceCommand = byteArrayOf(
        0x80.toByte(), 0x5C.toByte(), 0x00.toByte(), 0x02.toByte(), 0x04.toByte()
    )

    val responseBalance = isoDep.transceive(readBalanceCommand)
    if (!isSuccessResponse(responseBalance)) {
        return "Error: Failed to read balance"
    }

    // Parse balance (Assumes balance is in the first 4 bytes of response)
    val balanceBytes = responseBalance.copyOfRange(0, 4)
    //val balance = balanceBytes.toInt() / 100.0 // Example: Convert to currency
    val balance = toInt32(balanceBytes, 0) / 100.0
    return "Balance: $balance"
}

fun isSuccessResponse(response: ByteArray): Boolean {
    return response.size >= 2 && response[response.size - 2] == 0x90.toByte() && response[response.size - 1] == 0x00.toByte()
}

fun toInt32(bytes: ByteArray, index: Int): Int {
    require(bytes.size == 4) { "length must be 4, got: ${bytes.size}" }
    return ByteBuffer.wrap(bytes, index, 4).order(ByteOrder.LITTLE_ENDIAN).int
}