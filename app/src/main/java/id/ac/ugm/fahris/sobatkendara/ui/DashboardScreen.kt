package id.ac.ugm.fahris.sobatkendara.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.google.android.gms.location.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


import id.ac.ugm.fahris.sobatkendara.ui.components.AppBar
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DashboardScreen(
    drawerState: DrawerState,
) {
    val context = LocalContext.current
    var currentLocation by rememberSaveable { mutableStateOf("Fetching location...") }
    var speed by rememberSaveable { mutableStateOf("0 km/h") }
    var distance by rememberSaveable { mutableStateOf("0.0 km") }
    var timeElapsed by rememberSaveable { mutableStateOf("00:00:00") }
    var compassDirection by rememberSaveable { mutableStateOf("N") }
    var isPermissionGranted by remember { mutableStateOf(false) }

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

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
                    currentLocation = getAddressFromLocation(context, location)
                    speed = "${(location.speed * 3.6).roundToInt()} km/h" // Convert m/s to km/h
                    compassDirection = getCompassDirection(location.bearing)
                }
            )
        }
    }

    Scaffold(
        topBar = { AppBar(drawerState = drawerState) }
    ) {

        Column(
            modifier = Modifier.fillMaxSize().padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Current Location
            Text(
                text = currentLocation,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            // Current Speed
            Text(
                text = speed,
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

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

// Function to start location updates
@SuppressLint("MissingPermission")
fun startLocationUpdates(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationUpdate: (Location) -> Unit
) {
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000).setMinUpdateIntervalMillis(1000).build()
/*
    val locationRequest = LocationRequest.
    create().apply {
        interval = 2000
        fastestInterval = 1000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
 */

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val loc = locationResult.lastLocation

            Log.d("DashboardScreen", "onLocationResult ${loc.toString()}")
            locationResult.lastLocation?.let(onLocationUpdate)
        }
    }

    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
}

// Function to get a human-readable address from a location
fun getAddressFromLocation(context: Context, location: Location): String {
    val geocoder = Geocoder(context, Locale.getDefault())
    return try {
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        addresses?.firstOrNull()?.featureName ?: "Unknown location"
    } catch (e: Exception) {
        Log.d("DashboardScreen", "error: ${e.message}")
        return "Unknown location"
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

@Preview
@Composable
fun DashboardScreenPreview() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    DashboardScreen(drawerState)
}