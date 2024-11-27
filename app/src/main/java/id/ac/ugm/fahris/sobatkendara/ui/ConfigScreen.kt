package id.ac.ugm.fahris.sobatkendara.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalInspectionMode
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

    var flagHeadlightAlert by rememberSaveable { mutableStateOf(false) }
    var flagDrowsinessAlert by rememberSaveable { mutableStateOf(false) }
    var flagAccidentMovementAlert by rememberSaveable { mutableStateOf(false) }
    var flagAccidentAudioAlert by rememberSaveable { mutableStateOf(false) }

    var headlightThreshold by rememberSaveable { mutableStateOf(10f) }
    var drowsinessThreshold by rememberSaveable { mutableStateOf(300f) }
    var accidentShakinessThreshold by rememberSaveable { mutableStateOf(25f) }
    var accidentSoundThreshold by rememberSaveable { mutableStateOf(20000f) }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

    // Fetch profile data when the screen is shown
    LaunchedEffect(Unit) {
        isLoading = true
        val token = sharedPreferences.getString("auth_token", null)
        flagHeadlightAlert = sharedPreferences.getBoolean("flag_headlight_alert", true)
        flagDrowsinessAlert = sharedPreferences.getBoolean("flag_drowsiness_alert", true)
        flagAccidentMovementAlert = sharedPreferences.getBoolean("flag_accident_movement_alert", true)
        flagAccidentAudioAlert = sharedPreferences.getBoolean("flag_accident_audio_alert", true)

        headlightThreshold = sharedPreferences.getFloat("headlight_threshold", 10f)
        drowsinessThreshold = sharedPreferences.getFloat("drowsiness_threshold", 300f)
        accidentShakinessThreshold = sharedPreferences.getFloat("accident_shakiness_threshold", 25f)
        accidentSoundThreshold = sharedPreferences.getFloat("accident_sound_threshold", 20000f)

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
            modifier = Modifier.fillMaxSize().padding(it).padding(16.dp).verticalScroll(
                rememberScrollState()
            ),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {

            if (isLoading && !LocalInspectionMode.current) {
                // Show a loading spinner while fetching data
                Box(
                    modifier = Modifier.fillMaxWidth(),
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
            Spacer(modifier = Modifier.height(24.dp))
            FeatureSwitch("Headlight Alert", flagHeadlightAlert) {
                flagHeadlightAlert = it
                sharedPreferences.edit().putBoolean("flag_headlight_alert", it).apply()
            }
            Spacer(modifier = Modifier.height(16.dp))
            FeatureSwitch("Drowsiness Alert", flagDrowsinessAlert) {
                flagDrowsinessAlert = it
                sharedPreferences.edit().putBoolean("flag_drowsiness_alert", it).apply()
            }
            Spacer(modifier = Modifier.height(16.dp))
            FeatureSwitch("Accident Movement Alert", flagAccidentMovementAlert) {
                flagAccidentMovementAlert = it
                sharedPreferences.edit().putBoolean("flag_accident_movement_alert", it).apply()
            }
            Spacer(modifier = Modifier.height(16.dp))
            FeatureSwitch("Accident Audio Alert", flagAccidentAudioAlert) {
                flagAccidentAudioAlert = it
                sharedPreferences.edit().putBoolean("flag_accident_audio_alert", it).apply()
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Sliders for thresholds
            // 0 - 50 lux
            ThresholdSlider(
                label = "Headlight Threshold",
                value = headlightThreshold,
                unit = "lux",
                valueRange = 0f..50f,
                onValueChange = {
                    headlightThreshold = it
                    sharedPreferences.edit().putFloat("headlight_threshold", it).apply()
                }
            )
            // 0 - 1500
            ThresholdSlider(
                label = "Drowsiness Threshold",
                value = drowsinessThreshold,
                unit = "dB",
                valueRange = 0f..1500f,
                onValueChange = {
                    drowsinessThreshold = it
                    sharedPreferences.edit().putFloat("drowsiness_threshold", it).apply()
                }
            )
            // 10 - 100 m/s2
            // 10 - 100 rad/s
            ThresholdSlider(
                label = "Accident Shakiness Threshold",
                value = accidentShakinessThreshold,
                unit = "m/s²",
                valueRange = 10f..100f,
                onValueChange = {
                    accidentShakinessThreshold = it
                    sharedPreferences.edit().putFloat("accident_shakiness_threshold", it).apply()
                }
            )
            // 1000 - 40000
            ThresholdSlider(
                label = "Accident Sound Threshold",
                value = accidentSoundThreshold,
                unit = "dB",
                valueRange = 1000f..40000f,
                onValueChange = {
                    accidentSoundThreshold = it
                    sharedPreferences.edit().putFloat("accident_sound_threshold", it).apply()
                }
            )
        }
    }
}
@Composable
fun ThresholdSlider(label: String, value: Float, unit: String, valueRange: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    Text("$label: ${value.toInt()} $unit", fontSize = 18.sp)
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun FeatureSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.padding(start = 8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
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