package id.ac.ugm.fahris.sobatkendara.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
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
import id.ac.ugm.fahris.sobatkendara.service.GeocodingApiService


import id.ac.ugm.fahris.sobatkendara.ui.components.AppBar
import id.ac.ugm.fahris.sobatkendara.ui.components.FusionSpeedCalculator
import id.ac.ugm.fahris.sobatkendara.ui.components.SpeedCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DashboardScreen(
    drawerState: DrawerState,
) {

    val context = LocalContext.current
    var currentLocation by rememberSaveable { mutableStateOf("Fetching location...") }
    var speed by rememberSaveable { mutableStateOf("0") }
    var distance by rememberSaveable { mutableStateOf("0.0 km") }
    var timeElapsed by rememberSaveable { mutableStateOf("00:00:00") }
    var compassDirection by rememberSaveable { mutableStateOf("N") }
    var isPermissionGranted by remember { mutableStateOf(false) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val geocodingApi = remember { GeocodingApiService.create() }
    val coroutineScope = rememberCoroutineScope()

    // wrapper to handle bug of android studio failed to render permissions, sensormanager
    val isInspection = LocalInspectionMode.current
    lateinit var permissionState: MultiplePermissionsState

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
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
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
            }
        }
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
                        //speed = "${(location.speed * 3.6).roundToInt()}" // Convert m/s to km/h
                        compassDirection = getCompassDirection(location.bearing)
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = { AppBar(drawerState = drawerState) }
    ) {

        Column(
            modifier = Modifier.fillMaxSize().padding(it).padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Current Location
            Text(
                text = currentLocation,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            // Current Speed

            SpeedDisplay(speed)

            // Distance Information
            Text(
                text = "Distance: $distance",
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Time Elapsed Information
            Text(
                text = "Time Elapsed: $timeElapsed",
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Compass Direction
            Text(
                text = "Direction: $compassDirection",
                fontSize = 24.sp
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
        modifier = Modifier.padding(16.dp)
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

            Log.d("DashboardScreen", "onLocationResult ${loc.toString()}")
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

