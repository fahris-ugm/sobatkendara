package id.ac.ugm.fahris.sobatkendara.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ac.ugm.fahris.sobatkendara.R
import id.ac.ugm.fahris.sobatkendara.ui.components.AppBar

@Composable
fun ConfigScreen(
    drawerState: DrawerState,
) {

    var notificationEmail by rememberSaveable { mutableStateOf( "phewhe@gmail.com") }
    var isEmailDialogOpen by rememberSaveable { mutableStateOf(false) }

    var headlightThreshold by rememberSaveable { mutableStateOf(50f) }
    var drowsinessThreshold by rememberSaveable { mutableStateOf(50f) }
    var accidentShakinessThreshold by rememberSaveable { mutableStateOf(50f) }
    var accidentSoundThreshold by rememberSaveable { mutableStateOf(50f) }

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
                    },
                    onDismiss = { isEmailDialogOpen = false }
                )
            }

            // Slider: Headlight Threshold
            Text("Headlight Threshold: ${headlightThreshold.toInt()}%", fontSize = 18.sp)
            Slider(
                value = headlightThreshold,
                onValueChange = { headlightThreshold = it },
                valueRange = 0f..100f,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Slider: Drowsiness Threshold
            Text("Drowsiness Threshold: ${drowsinessThreshold.toInt()}%", fontSize = 18.sp)
            Slider(
                value = drowsinessThreshold,
                onValueChange = { drowsinessThreshold = it },
                valueRange = 0f..100f,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Slider: Accident Shakiness Threshold
            Text("Accident Shakiness Threshold: ${accidentShakinessThreshold.toInt()}%", fontSize = 18.sp)
            Slider(
                value = accidentShakinessThreshold,
                onValueChange = { accidentShakinessThreshold = it },
                valueRange = 0f..100f,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Slider: Accident Sound Threshold
            Text("Accident Sound Threshold: ${accidentSoundThreshold.toInt()}%", fontSize = 18.sp)
            Slider(
                value = accidentSoundThreshold,
                onValueChange = { accidentSoundThreshold = it },
                valueRange = 0f..100f,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
fun EditEmailDialog(
    currentEmail: String,
    onEmailUpdated: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newEmail by rememberSaveable { mutableStateOf(currentEmail) }

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
            Button(onClick = { onEmailUpdated(newEmail) }) {
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