package id.ac.ugm.fahris.sobatkendara

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainContent(
                onMenuClick = {

                }
            )
        }
    }
}

@Composable
fun MainContent(onMenuClick: () -> Unit) {
    var currentLocation by remember { mutableStateOf("Fetching location...") }
    var speed by remember { mutableStateOf("0 km/h") }
    var distance by remember { mutableStateOf("0.0 km") }
    var timeElapsed by remember { mutableStateOf("00:00:00") }
    var compassDirection by remember { mutableStateOf("N") }

    // Main UI Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Menu Icon
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Default.Menu, contentDescription = "Menu")
        }

        Spacer(modifier = Modifier.height(16.dp))

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

@Composable
fun DrawerContent(
    onConfigClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Configs",
            fontSize = 18.sp,
            modifier = Modifier
                .clickable { onConfigClick() }
                .padding(8.dp)
        )
        Text(
            text = "Change Password",
            fontSize = 18.sp,
            modifier = Modifier
                .clickable { onChangePasswordClick() }
                .padding(8.dp)
        )
        Text(
            text = "Sign Out",
            fontSize = 18.sp,
            modifier = Modifier
                .clickable { onSignOutClick() }
                .padding(8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainContent(
        onMenuClick = {

        }
    )
}