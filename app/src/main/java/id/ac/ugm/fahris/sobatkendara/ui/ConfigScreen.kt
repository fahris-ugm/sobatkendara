package id.ac.ugm.fahris.sobatkendara.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ac.ugm.fahris.sobatkendara.R
import id.ac.ugm.fahris.sobatkendara.service.ApiService
import id.ac.ugm.fahris.sobatkendara.ui.components.AppBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ConfigScreen(
    drawerState: DrawerState,
) {
    val scope = rememberCoroutineScope()
    var notificationEmail by rememberSaveable { mutableStateOf( "") }
    var isEmailDialogOpen by rememberSaveable { mutableStateOf(false) }

    var headlightThreshold by rememberSaveable { mutableStateOf(50f) }
    var drowsinessThreshold by rememberSaveable { mutableStateOf(50f) }
    var accidentShakinessThreshold by rememberSaveable { mutableStateOf(50f) }
    var accidentSoundThreshold by rememberSaveable { mutableStateOf(50f) }
    var isLoading by rememberSaveable { mutableStateOf(true) }

    val context = LocalContext.current

    // Fetch profile data when the screen is shown
    LaunchedEffect(Unit) {
        isLoading = true
        val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        val profile = ApiService.getProfile(context, token?:"", onGetProfileError = {message ->
            Toast.makeText(context, "Failed to get profile: $message", Toast.LENGTH_SHORT).show()
        })
        if (profile != null) {
            notificationEmail = profile.additionalEmail?:""
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            AppBar(
                drawerState = drawerState,
                title = R.string.drawer_config
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(it).padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            if (isLoading) {
                // Show a loading spinner while fetching data
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = notificationEmail,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                        label = { Text("Notification Email") }
                    )
                    IconButton(onClick = { isEmailDialogOpen = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Email")
                    }
                }

                // Modal Dialog to Edit Email
                if (isEmailDialogOpen) {
                    EditEmailDialog(
                        currentEmail = notificationEmail,
                        onEmailUpdated = { newEmail ->
                            notificationEmail = newEmail
                            isEmailDialogOpen = false
                            isLoading = true
                            CoroutineScope(Dispatchers.Main).launch {
                                val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                                val token = sharedPreferences.getString("auth_token", null)
                                val profile = ApiService.editProfile(context, token?:"", newEmail,
                                    onEditProfileError = {message ->
                                        Toast.makeText(context, "Failed to edit profile: $message", Toast.LENGTH_SHORT).show()
                                    })
                                if (profile != null) {
                                    notificationEmail = profile.additionalEmail?:""
                                }
                                isLoading = false
                            }
                        },
                        onDismiss = { isEmailDialogOpen = false }
                    )
                }
            }
            // Sliders for thresholds
            ThresholdSlider(
                label = "Headlight Threshold",
                value = headlightThreshold,
                onValueChange = { headlightThreshold = it }
            )

            ThresholdSlider(
                label = "Drowsiness Threshold",
                value = drowsinessThreshold,
                onValueChange = { drowsinessThreshold = it }
            )

            ThresholdSlider(
                label = "Accident Shakiness Threshold",
                value = accidentShakinessThreshold,
                onValueChange = { accidentShakinessThreshold = it }
            )

            ThresholdSlider(
                label = "Accident Sound Threshold",
                value = accidentSoundThreshold,
                onValueChange = { accidentSoundThreshold = it }
            )
        }
    }
}
@Composable
fun ThresholdSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Text("$label: ${value.toInt()}%", fontSize = 18.sp)
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = 0f..100f,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun EditEmailDialog(
    currentEmail: String,
    onEmailUpdated: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newEmail by rememberSaveable { mutableStateOf(currentEmail) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Edit Notification Email") },
        text = {
            Column {
                OutlinedTextField(
                    value = newEmail,
                    onValueChange = { newEmail = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (!newEmail.trim().isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                    onEmailUpdated(newEmail)
                } else {
                    Toast.makeText(context, "Invalid email format", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}

@Preview
@Composable
fun ConfigScreenPreview() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    ConfigScreen(drawerState)
}