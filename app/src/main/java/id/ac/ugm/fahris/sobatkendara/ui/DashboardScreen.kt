package id.ac.ugm.fahris.sobatkendara.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ac.ugm.fahris.sobatkendara.ui.components.AppBar


@Composable
fun DashboardScreen(
    drawerState: DrawerState,
) {
    var currentLocation by remember { mutableStateOf("Fetching location...") }
    var speed by remember { mutableStateOf("0 km/h") }
    var distance by remember { mutableStateOf("0.0 km") }
    var timeElapsed by remember { mutableStateOf("00:00:00") }
    var compassDirection by remember { mutableStateOf("N") }

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