package id.ac.ugm.fahris.sobatkendara

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import id.ac.ugm.fahris.sobatkendara.service.ApiService
import id.ac.ugm.fahris.sobatkendara.ui.components.OtpInputField
import id.ac.ugm.fahris.sobatkendara.ui.components.pxToDp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResetPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Enable the action bar back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Reset Password"
        setContent {
            val email = intent.getStringExtra("ACCOUNT_EMAIL")
            ResetPasswordScreen (
                accountEmail = email ?: "",
                onResetPasswordSuccess = {
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    this.startActivity(intent)
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
fun ResetPasswordScreenPreview() {
    ResetPasswordScreen(
        "test@mail.com",
        onResetPasswordSuccess = {
        }
    )
}

@Composable
fun ResetPasswordScreen(
    accountEmail: String,
    onResetPasswordSuccess: (String) -> Unit,

    ) {
    var email by rememberSaveable { mutableStateOf(accountEmail) }
    var password by rememberSaveable { mutableStateOf("password123") }
    var confirmPassword by rememberSaveable { mutableStateOf("password123") }
    //var otp by rememberSaveable { mutableStateOf("") }
    val otpValue = rememberSaveable { mutableStateOf("") }
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
            text = "Reset Password",
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        val pattern = remember { Regex("^\\d+\$") }
        val maxchar = 6
        /*
        OutlinedTextField(
            value = otp,
            onValueChange = {
                if (it.isEmpty() || it.matches(pattern) || it.length <= maxchar) {
                    otp = it
                } },
            label = { Text("OTP (check your email)") },
            readOnly = isLoading,
            modifier = Modifier.fillMaxWidth()
        )*/
        Text(
            text = "OTP (check your email)",
            modifier = Modifier.padding(bottom = 16.dp)
        )
        OtpInputField(
            otp = otpValue,
            count = maxchar,
            otpBoxModifier = Modifier
                .border(3.pxToDp(), Color.Black)
                .background(Color.White),
            otpTextType = KeyboardType.Number
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
                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || otpValue.value.isEmpty()) {
                    Toast.makeText(context, "Please enter all fields ${otpValue.value}", Toast.LENGTH_SHORT).show()
                } else if (password != confirmPassword) {
                    Toast.makeText(context, "Password and confirm password are not equal", Toast.LENGTH_SHORT).show()
                } else {
                    isLoading = true
                    CoroutineScope(Dispatchers.Main).launch {
                        val message = ApiService.resetPassword(context, email, password, otpValue.value,
                            onResetPasswordError = { errorMessage = it }
                        )
                        Log.d("ResetPasswordScreen", "Message: $message")
                        isLoading = false
                        if (message != null) {
                            if (message.contains("success")) {
                                Toast.makeText(context, "Password reset successful", Toast.LENGTH_SHORT).show()
                                onResetPasswordSuccess(email)
                            } else {
                                Toast.makeText(context, "Password reset failed: $message", Toast.LENGTH_SHORT).show()
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
            Text("Reset Password")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}