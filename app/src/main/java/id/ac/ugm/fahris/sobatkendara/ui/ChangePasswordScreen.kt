package id.ac.ugm.fahris.sobatkendara.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.ac.ugm.fahris.sobatkendara.R
import id.ac.ugm.fahris.sobatkendara.service.ApiService
import id.ac.ugm.fahris.sobatkendara.ui.components.AppBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun ChangePasswordScreen(
    drawerState: DrawerState,
) {
    var oldPassword by rememberSaveable { mutableStateOf("password123") }
    var password by rememberSaveable { mutableStateOf("password123") }
    var confirmPassword by rememberSaveable { mutableStateOf("password123") }
    var showPassword by rememberSaveable { mutableStateOf(value = false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    var isLoading by rememberSaveable { mutableStateOf(false) }
    val alphaLoading = if (isLoading) 1f else 0f

    val context = LocalContext.current

    Scaffold(
        topBar = {
            AppBar(drawerState = drawerState, title = R.string.drawer_change_password)
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(it).padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = oldPassword,
                onValueChange = { newText ->
                    oldPassword = newText
                },
                label = {
                    Text(text = "Current Password")
                },
                readOnly = isLoading,
                placeholder = { Text(text = "Type current password here") },
                shape = RoundedCornerShape(percent = 20),
                visualTransformation = if (showPassword) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    if (showPassword) {
                        IconButton(onClick = { showPassword = false }) {
                            Icon(
                                imageVector = Icons.Filled.Visibility,
                                contentDescription = "hide_password"
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { showPassword = true }) {
                            Icon(
                                imageVector = Icons.Filled.VisibilityOff,
                                contentDescription = "hide_password"
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = password,
                onValueChange = { newText ->
                    password = newText
                },
                label = {
                    Text(text = "New Password")
                },
                readOnly = isLoading,
                placeholder = { Text(text = "Type new password here") },
                shape = RoundedCornerShape(percent = 20),
                visualTransformation = if (showPassword) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    if (showPassword) {
                        IconButton(onClick = { showPassword = false }) {
                            Icon(
                                imageVector = Icons.Filled.Visibility,
                                contentDescription = "hide_password"
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { showPassword = true }) {
                            Icon(
                                imageVector = Icons.Filled.VisibilityOff,
                                contentDescription = "hide_password"
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = confirmPassword,
                onValueChange = { newText ->
                    confirmPassword = newText
                },
                label = {
                    Text(text = "Confirm New Password")
                },
                readOnly = isLoading,
                placeholder = { Text(text = "Confirm new password here") },
                shape = RoundedCornerShape(percent = 20),
                visualTransformation = if (showPassword) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    if (showPassword) {
                        IconButton(onClick = { showPassword = false }) {
                            Icon(
                                imageVector = Icons.Filled.Visibility,
                                contentDescription = "hide_password"
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { showPassword = true }) {
                            Icon(
                                imageVector = Icons.Filled.VisibilityOff,
                                contentDescription = "hide_password"
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Show ProgressBar when loading
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).alpha(alphaLoading))

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {

                    if (oldPassword.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                        Toast.makeText(context, "Please enter all fields", Toast.LENGTH_SHORT).show()
                    } else if (password != confirmPassword) {
                        Toast.makeText(context, "New password and confirm new password are not equal", Toast.LENGTH_SHORT).show()
                    } else {
                        isLoading = true
                        CoroutineScope(Dispatchers.Main).launch {
                            val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                            val token = sharedPreferences.getString("auth_token", null)
                            val message = ApiService.changePassword(context, token?: "", oldPassword, password,
                                onChangePasswordError = { errorMessage = it }
                            )
                            Log.d("ChangePasswordScreen", "Message: $message")
                            isLoading = false
                            if (message != null) {
                                if (message.contains("success")) {
                                    Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                                    errorMessage = ""
                                } else {
                                    Toast.makeText(context, "Password change failed: $message", Toast.LENGTH_SHORT).show()
                                    errorMessage = message
                                }
                            } else {

                            }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Change Password")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview
@Composable
fun ChangePasswordScreenPreview() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    ChangePasswordScreen(drawerState)
}