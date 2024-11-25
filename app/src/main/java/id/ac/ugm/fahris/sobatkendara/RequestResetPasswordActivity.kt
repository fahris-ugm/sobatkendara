package id.ac.ugm.fahris.sobatkendara

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.ac.ugm.fahris.sobatkendara.service.ApiService
import id.ac.ugm.fahris.sobatkendara.ui.theme.SobatKendaraTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RequestResetPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Reset Password"
        setContent {
            RequestResetPasswordScreen(
                onOTPSuccess = {message, email ->
                    val intent = Intent(this, ResetPasswordActivity::class.java)
                    intent.putExtra("ACCOUNT_EMAIL", email )
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
fun RequestResetPasswordScreenPreview() {
    RequestResetPasswordScreen (
        onOTPSuccess = {message, email ->

        }
    )
}

@Composable
fun RequestResetPasswordScreen(
    onOTPSuccess: (message: String, email: String) -> Unit,

    ) {
    var email by rememberSaveable { mutableStateOf("phewhe@gmail.com") }

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
            text = "Request OTP for Reset Password",
            modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            readOnly = isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        // Show ProgressBar when loading
        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).alpha(alphaLoading))
        Text(
            text = errorMessage,
            color = Color.Red,
            modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (email.isEmpty()) {
                    Toast.makeText(context, "Please specify your user account (email)", Toast.LENGTH_SHORT).show()
                } else {
                    isLoading = true
                    CoroutineScope(Dispatchers.Main).launch {
                        val message = ApiService.requestOTP(context, email,
                            onRequestOTPError = { errorMessage = it }
                        )
                        Log.d("RequestResetPasswordScreen", "Message: $message")
                        isLoading = false
                        if (message != null) {
                            if (message.contains("sent")) {
                                Toast.makeText(context, "Check your email for the OTP", Toast.LENGTH_SHORT).show()
                                onOTPSuccess(message, email)
                            } else {
                                Toast.makeText(context, "OTP Request failed: $message", Toast.LENGTH_SHORT).show()
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
            Text("Send OTP to email")
        }

    }
}
