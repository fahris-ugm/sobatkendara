package id.ac.ugm.fahris.sobatkendara

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Check if the token exists in SharedPreferences
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val token = null//sharedPreferences.getString("auth_token", null)

        if (token != null) {
            // Token exists, start MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            setContent {
                //SobatKendaraTheme {
                LoginScreen(
                    onLoginSuccess = { token ->

                        with(sharedPreferences.edit()) {
                            putString("auth_token", token)
                            apply()
                        }
                        // Navigate to MainActivity on successful login
                        //startActivity(Intent(this, MainActivity::class.java))
                        //finish()
                    },
                    onSignUp = {
                        // Navigate to Sign Up screen
                        //Toast.makeText(this, "Sign Up clicked", Toast.LENGTH_SHORT).show()
                        this.startActivity(Intent(this, RegisterActivity::class.java))
                    },
                    onForgotPassword = {
                        // Handle Forgot Password action
                        Toast.makeText(this, "Forgot Password clicked", Toast.LENGTH_SHORT).show()
                    }
                )
                //}
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SobatKendaraTheme {
        LoginScreen(
            onLoginSuccess = {
            },
            onSignUp = {
            },
            onForgotPassword = {
            }
        )
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onSignUp: () -> Unit,
    onForgotPassword: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("phewhe@gmail.com") }
    var password by rememberSaveable { mutableStateOf("password123") }
    var showPassword by rememberSaveable { mutableStateOf(value = false) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current

    val alphaLoading = if (isLoading) 1f else 0f
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {

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
            placeholder = { Text(text = "Type password here") },
            readOnly = isLoading,
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

        Button(
            onClick = {
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(context, "Please enter all fields", Toast.LENGTH_SHORT).show()
                } else {
                    isLoading = true
                    CoroutineScope(Dispatchers.Main).launch {
                        val token = ApiService.login(context, email, password,
                            onLoginError = { message ->
                                //Toast.makeText(context, "Login failed: $mssage", Toast.LENGTH_SHORT).show()
                                errorMessage = message
                            }
                        )
                        Log.d("LoginScreen", "Token: $token")
                        isLoading = false
                        if (token != null) {
                            Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                            onLoginSuccess(token)
                        } else {
                            //Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = errorMessage,
            color = Color.Red,
            modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Sign Up and Forgot Password options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Sign Up",
                fontSize = 16.sp,
                modifier = Modifier.clickable { if (!isLoading) onSignUp() }
            )

            Text(
                text = "Forgot Password?",
                fontSize = 16.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.clickable { if (!isLoading) onForgotPassword() }
            )
        }
    }
}