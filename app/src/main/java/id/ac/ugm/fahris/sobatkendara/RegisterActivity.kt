package id.ac.ugm.fahris.sobatkendara

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ac.ugm.fahris.sobatkendara.service.ApiService
import id.ac.ugm.fahris.sobatkendara.ui.theme.SobatKendaraTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Enable the action bar back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Register New Account"
        setContent {
            RegisterScreen(
                onRegisterSuccess = {
                    finish()
                }
            )
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        // Handle back button click
        finish()
        return true
    }
}


@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
        RegisterScreen(
            onRegisterSuccess = {
            }
        )
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: (String) -> Unit,

) {
    var email by rememberSaveable { mutableStateOf("phewhe@gmail.com") }
    var password by rememberSaveable { mutableStateOf("password123") }
    var confirmPassword by rememberSaveable { mutableStateOf("password123") }
    var notifEmail by rememberSaveable { mutableStateOf("phewhe@gmail.com") }
    var showPassword by rememberSaveable { mutableStateOf(value = false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    var isLoading by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val alphaLoading = if (isLoading) 1f else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Register",
            modifier = Modifier.padding(bottom = 16.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            readOnly = isLoading,
            modifier = Modifier.fillMaxWidth()
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
                Text(text = "Password")
            },
            readOnly = isLoading,
            placeholder = { Text(text = "Type password here") },
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
                Text(text = "Confirm Password")
            },
            readOnly = isLoading,
            placeholder = { Text(text = "Confirm password here") },
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
            value = notifEmail,
            onValueChange = { notifEmail = it },
            label = { Text("Notification Email Address") },
            readOnly = isLoading,
            modifier = Modifier.fillMaxWidth()
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
                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(context, "Please enter all fields", Toast.LENGTH_SHORT).show()
                } else if (password != confirmPassword) {
                    Toast.makeText(context, "Password and confirm password not equal", Toast.LENGTH_SHORT).show()
                } else {
                    isLoading = true
                    CoroutineScope(Dispatchers.Main).launch {
                        val message = ApiService.signup(context, email, password, notifEmail,
                            onSignupError = { errorMessage = it }
                        )
                        Log.d("RegisterScreen", "Message: $message")
                        isLoading = false
                        if (message != null) {
                            if (message.contains("success")) {
                                Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()
                                onRegisterSuccess(email)
                            } else {
                                Toast.makeText(context, "Registration failed: $message", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            //Toast.makeText(context, "Registration failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
