package id.ac.ugm.fahris.sobatkendara.service

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Date
import javax.net.ssl.HttpsURLConnection

object ApiService {
    // Toggle between different environments
    private const val ENVIRONMENT = "development" // or "production"

    private val connectionTimeout = 3000
    private val BASE_URL: String
        get() = when (ENVIRONMENT) {
            "development" -> "https://192.168.26.128:8443"
            "production" -> "https://sk-service-414032994764.us-central1.run.app"
            else -> "https://192.168.26.128:8443"
        }

    suspend fun login(context: Context, email: String, password: String, onLoginError: (String) -> Unit): String? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("${BASE_URL}/login")
                val httpsURLConnection = url.openConnection() as HttpsURLConnection
                httpsURLConnection.connectTimeout = connectionTimeout

                if (ENVIRONMENT == "development") {
                    // Set up SSL to ignore certificate validation
                    httpsURLConnection.sslSocketFactory =
                        UnsafeSSLHelper.getUnsafeSSLSocketFactory()
                    httpsURLConnection.hostnameVerifier =
                        UnsafeSSLHelper.getUnsafeHostnameVerifier()
                }

                httpsURLConnection.requestMethod = "POST"
                httpsURLConnection.setRequestProperty("Content-Type", "application/json")
                httpsURLConnection.doOutput = true

                // Create the JSON request body
                val jsonInputString = JSONObject()
                jsonInputString.put("email", email)
                jsonInputString.put("password", password)

                // Write the request body
                val outputStream: OutputStream = httpsURLConnection.outputStream
                outputStream.write(jsonInputString.toString().toByteArray())
                outputStream.flush()
                outputStream.close()

                // Check the response code
                val responseCode = httpsURLConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response
                    val reader = BufferedReader(InputStreamReader(httpsURLConnection.inputStream))
                    val response = reader.readText()
                    reader.close()

                    // Parse the JSON response to extract the token
                    val jsonResponse = JSONObject(response)
                    return@withContext jsonResponse.getString("token")
                } else {
                    val reader = BufferedReader(InputStreamReader(httpsURLConnection.errorStream))
                    val response = reader.readText()
                    reader.close()

                    // Parse the JSON response to extract the token
                    val jsonResponse = JSONObject(response)
                    val message = jsonResponse.getString("message")
                    // Handle error
                    withContext(Dispatchers.Main) {
                        //Toast.makeText(context, "Login failed: $responseCode - $message", Toast.LENGTH_SHORT).show()
                        onLoginError(message)
                    }
                    return@withContext null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    //Toast.makeText(context, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                    onLoginError("An error occurred: ${e.message}")
                }
                return@withContext null
            }
        }
    }

    suspend fun signup(context: Context, email: String, password: String, notifEmail: String, onSignupError: (String) -> Unit): String? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("${BASE_URL}/signup")
                val httpsURLConnection = url.openConnection() as HttpsURLConnection
                httpsURLConnection.connectTimeout = connectionTimeout

                if (ENVIRONMENT == "development") {
                    // Set up SSL to ignore certificate validation
                    httpsURLConnection.sslSocketFactory =
                        UnsafeSSLHelper.getUnsafeSSLSocketFactory()
                    httpsURLConnection.hostnameVerifier =
                        UnsafeSSLHelper.getUnsafeHostnameVerifier()
                }

                httpsURLConnection.requestMethod = "POST"
                httpsURLConnection.setRequestProperty("Content-Type", "application/json")
                httpsURLConnection.doOutput = true

                // Create the JSON request body
                val jsonInputString = JSONObject()
                jsonInputString.put("email", email)
                jsonInputString.put("password", password)
                jsonInputString.put("notif_email", notifEmail)

                // Write the request body
                val outputStream: OutputStream = httpsURLConnection.outputStream
                outputStream.write(jsonInputString.toString().toByteArray())
                outputStream.flush()
                outputStream.close()

                // Check the response code
                val responseCode = httpsURLConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    // Read the response
                    val reader = BufferedReader(InputStreamReader(httpsURLConnection.inputStream))
                    val response = reader.readText()
                    reader.close()

                    // Parse the JSON response to extract the token
                    val jsonResponse = JSONObject(response)
                    return@withContext jsonResponse.getString("message")
                } else {
                    val reader = BufferedReader(InputStreamReader(httpsURLConnection.errorStream))
                    val response = reader.readText()
                    reader.close()

                    // Parse the JSON response to extract the token
                    val jsonResponse = JSONObject(response)
                    val message = jsonResponse.getString("message")
                    // Handle error
                    withContext(Dispatchers.Main) {
                        //Toast.makeText(context, "Registration failed: $responseCode - $message", Toast.LENGTH_SHORT).show()
                        onSignupError(message)
                    }
                    return@withContext null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    //Toast.makeText(context, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                    onSignupError("An error occurred: ${e.message}")
                }
                return@withContext null
            }
        }
    }

    suspend fun requestOTP(context: Context, email: String, onRequestOTPError: (String) -> Unit): String? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("${BASE_URL}/request-otp")
                val httpsURLConnection = url.openConnection() as HttpsURLConnection
                httpsURLConnection.connectTimeout = connectionTimeout

                if (ENVIRONMENT == "development") {
                    // Set up SSL to ignore certificate validation
                    httpsURLConnection.sslSocketFactory =
                        UnsafeSSLHelper.getUnsafeSSLSocketFactory()
                    httpsURLConnection.hostnameVerifier =
                        UnsafeSSLHelper.getUnsafeHostnameVerifier()
                }

                httpsURLConnection.requestMethod = "POST"
                httpsURLConnection.setRequestProperty("Content-Type", "application/json")
                httpsURLConnection.doOutput = true

                // Create the JSON request body
                val jsonInputString = JSONObject()
                jsonInputString.put("email", email)

                // Write the request body
                val outputStream: OutputStream = httpsURLConnection.outputStream
                outputStream.write(jsonInputString.toString().toByteArray())
                outputStream.flush()
                outputStream.close()

                // Check the response code
                val responseCode = httpsURLConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    // Read the response
                    val reader = BufferedReader(InputStreamReader(httpsURLConnection.inputStream))
                    val response = reader.readText()
                    reader.close()

                    // Parse the JSON response to extract the token
                    val jsonResponse = JSONObject(response)
                    return@withContext jsonResponse.getString("message")
                } else {
                    val reader = BufferedReader(InputStreamReader(httpsURLConnection.errorStream))
                    val response = reader.readText()
                    reader.close()

                    // Parse the JSON response to extract the token
                    val jsonResponse = JSONObject(response)
                    val message = jsonResponse.getString("message")
                    // Handle error
                    withContext(Dispatchers.Main) {
                        //Toast.makeText(context, "Registration failed: $responseCode - $message", Toast.LENGTH_SHORT).show()
                        onRequestOTPError(message)
                    }
                    return@withContext null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    //Toast.makeText(context, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                    onRequestOTPError("An error occurred: ${e.message}")
                }
                return@withContext null
            }
        }
    }

    suspend fun resetPassword(context: Context, email: String, password: String, otp: String, onResetPasswordError: (String) -> Unit): String? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("${BASE_URL}/reset-password")
                val httpsURLConnection = url.openConnection() as HttpsURLConnection
                httpsURLConnection.connectTimeout = connectionTimeout

                if (ENVIRONMENT == "development") {
                    // Set up SSL to ignore certificate validation
                    httpsURLConnection.sslSocketFactory =
                        UnsafeSSLHelper.getUnsafeSSLSocketFactory()
                    httpsURLConnection.hostnameVerifier =
                        UnsafeSSLHelper.getUnsafeHostnameVerifier()
                }

                httpsURLConnection.requestMethod = "POST"
                httpsURLConnection.setRequestProperty("Content-Type", "application/json")
                httpsURLConnection.doOutput = true

                // Create the JSON request body
                val jsonInputString = JSONObject()
                jsonInputString.put("email", email)
                jsonInputString.put("otp", otp)
                jsonInputString.put("new_password", password)

                // Write the request body
                val outputStream: OutputStream = httpsURLConnection.outputStream
                outputStream.write(jsonInputString.toString().toByteArray())
                outputStream.flush()
                outputStream.close()

                // Check the response code
                val responseCode = httpsURLConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    // Read the response
                    val reader = BufferedReader(InputStreamReader(httpsURLConnection.inputStream))
                    val response = reader.readText()
                    reader.close()

                    // Parse the JSON response to extract the token
                    val jsonResponse = JSONObject(response)
                    return@withContext jsonResponse.getString("message")
                } else {
                    val reader = BufferedReader(InputStreamReader(httpsURLConnection.errorStream))
                    val response = reader.readText()
                    reader.close()

                    // Parse the JSON response to extract the token
                    val jsonResponse = JSONObject(response)
                    val message = jsonResponse.getString("message")
                    // Handle error
                    withContext(Dispatchers.Main) {
                        //Toast.makeText(context, "Registration failed: $responseCode - $message", Toast.LENGTH_SHORT).show()
                        onResetPasswordError(message)
                    }
                    return@withContext null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    //Toast.makeText(context, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                    onResetPasswordError("An error occurred: ${e.message}")
                }
                return@withContext null
            }
        }
    }
    suspend fun changePassword(context: Context, token: String, oldPassword: String, newPassword: String, onChangePasswordError: (String) -> Unit): String? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("${BASE_URL}/change-password")
                val httpsURLConnection = url.openConnection() as HttpsURLConnection
                httpsURLConnection.connectTimeout = connectionTimeout

                if (ENVIRONMENT == "development") {
                    // Set up SSL to ignore certificate validation
                    httpsURLConnection.sslSocketFactory =
                        UnsafeSSLHelper.getUnsafeSSLSocketFactory()
                    httpsURLConnection.hostnameVerifier =
                        UnsafeSSLHelper.getUnsafeHostnameVerifier()
                }

                httpsURLConnection.requestMethod = "POST"
                httpsURLConnection.setRequestProperty("Content-Type", "application/json")
                httpsURLConnection.doOutput = true

                httpsURLConnection.setRequestProperty("Authorization", "Bearer $token")

                // Create the JSON request body
                val jsonInputString = JSONObject()
                jsonInputString.put("old_password", oldPassword)
                jsonInputString.put("new_password", newPassword)

                // Write the request body
                val outputStream: OutputStream = httpsURLConnection.outputStream
                outputStream.write(jsonInputString.toString().toByteArray())
                outputStream.flush()
                outputStream.close()

                // Check the response code
                val responseCode = httpsURLConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response
                    val reader = BufferedReader(InputStreamReader(httpsURLConnection.inputStream))
                    val response = reader.readText()
                    reader.close()

                    // Parse the JSON response to extract the token
                    val jsonResponse = JSONObject(response)
                    return@withContext jsonResponse.getString("message")
                } else {
                    val reader = BufferedReader(InputStreamReader(httpsURLConnection.errorStream))
                    val response = reader.readText()
                    reader.close()

                    // Parse the JSON response to extract the token
                    val jsonResponse = JSONObject(response)
                    val message = jsonResponse.getString("message")
                    // Handle error
                    withContext(Dispatchers.Main) {
                        onChangePasswordError(message)
                    }
                    return@withContext null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onChangePasswordError("An error occurred: ${e.message}")
                }
                return@withContext null
            }
        }
    }
    suspend fun logout(context: Context, token: String, onLogoutError: (String) -> Unit): String? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("${BASE_URL}/logout")
                val httpsURLConnection = url.openConnection() as HttpsURLConnection
                httpsURLConnection.connectTimeout = connectionTimeout

                if (ENVIRONMENT == "development") {
                    // Set up SSL to ignore certificate validation
                    httpsURLConnection.sslSocketFactory =
                        UnsafeSSLHelper.getUnsafeSSLSocketFactory()
                    httpsURLConnection.hostnameVerifier =
                        UnsafeSSLHelper.getUnsafeHostnameVerifier()
                }

                httpsURLConnection.requestMethod = "POST"
                httpsURLConnection.setRequestProperty("Content-Type", "application/json")
                httpsURLConnection.doOutput = true

                httpsURLConnection.setRequestProperty("Authorization", "Bearer $token")

                // Check the response code
                val responseCode = httpsURLConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response
                    val reader = BufferedReader(InputStreamReader(httpsURLConnection.inputStream))
                    val response = reader.readText()
                    reader.close()

                    // Parse the JSON response to extract the token
                    val jsonResponse = JSONObject(response)
                    return@withContext jsonResponse.getString("message")
                } else {
                    val reader = BufferedReader(InputStreamReader(httpsURLConnection.errorStream))
                    val response = reader.readText()
                    reader.close()

                    // Parse the JSON response to extract the token
                    val jsonResponse = JSONObject(response)
                    val message = jsonResponse.getString("message")
                    // Handle error
                    withContext(Dispatchers.Main) {
                        onLogoutError(message)
                    }
                    return@withContext null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onLogoutError("An error occurred: ${e.message}")
                }
                return@withContext null
            }
        }
    }
    suspend fun getProfile(context: Context, token: String, onGetProfileError: (String) -> Unit): UserProfile? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("${BASE_URL}/profile")
                val httpsURLConnection = url.openConnection() as HttpsURLConnection
                httpsURLConnection.connectTimeout = connectionTimeout

                if (ENVIRONMENT == "development") {
                    // Set up SSL to ignore certificate validation
                    httpsURLConnection.sslSocketFactory =
                        UnsafeSSLHelper.getUnsafeSSLSocketFactory()
                    httpsURLConnection.hostnameVerifier =
                        UnsafeSSLHelper.getUnsafeHostnameVerifier()
                }

                httpsURLConnection.requestMethod = "GET"
                httpsURLConnection.setRequestProperty("Content-Type", "application/json")
                httpsURLConnection.doOutput = true

                httpsURLConnection.setRequestProperty("Authorization", "Bearer $token")

                // Check the response code
                val responseCode = httpsURLConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response
                    val reader = BufferedReader(InputStreamReader(httpsURLConnection.inputStream))
                    val response = reader.readText()
                    reader.close()

                    // Parse the JSON response to extract the token
                    val jsonResponse = JSONObject(response)
                    return@withContext UserProfile(
                        id = jsonResponse.getInt("id"),
                        email = jsonResponse.getString("email"),
                        additionalEmail = jsonResponse.getString("additional_email"),
                        timestampString = jsonResponse.getString("created_at")
                    )
                } else {
                    val reader = BufferedReader(InputStreamReader(httpsURLConnection.errorStream))
                    val response = reader.readText()
                    reader.close()

                    // Parse the JSON response to extract the token
                    val jsonResponse = JSONObject(response)
                    val message = jsonResponse.getString("message")
                    // Handle error
                    withContext(Dispatchers.Main) {
                        onGetProfileError(message)
                    }
                    return@withContext null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onGetProfileError("An error occurred: ${e.message}")
                }
                return@withContext null
            }
        }
    }
    suspend fun editProfile(context: Context, token: String, email: String, onEditProfileError: (String) -> Unit): UserProfile? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("${BASE_URL}/edit-profile")
                val httpsURLConnection = url.openConnection() as HttpsURLConnection
                httpsURLConnection.connectTimeout = connectionTimeout

                if (ENVIRONMENT == "development") {
                    // Set up SSL to ignore certificate validation
                    httpsURLConnection.sslSocketFactory =
                        UnsafeSSLHelper.getUnsafeSSLSocketFactory()
                    httpsURLConnection.hostnameVerifier =
                        UnsafeSSLHelper.getUnsafeHostnameVerifier()
                }

                httpsURLConnection.requestMethod = "PUT"
                httpsURLConnection.setRequestProperty("Content-Type", "application/json")
                httpsURLConnection.doOutput = true

                httpsURLConnection.setRequestProperty("Authorization", "Bearer $token")

                // Create the JSON request body
                val jsonInputString = JSONObject()
                jsonInputString.put("additional_email", email)

                // Write the request body
                val outputStream: OutputStream = httpsURLConnection.outputStream
                outputStream.write(jsonInputString.toString().toByteArray())
                outputStream.flush()
                outputStream.close()

                // Check the response code
                val responseCode = httpsURLConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response
                    val reader = BufferedReader(InputStreamReader(httpsURLConnection.inputStream))
                    val response = reader.readText()
                    reader.close()

                    // Parse the JSON response to extract the token
                    val jsonResponse = JSONObject(response)
                    return@withContext UserProfile(
                        id = jsonResponse.getInt("id"),
                        email = jsonResponse.getString("email"),
                        additionalEmail = jsonResponse.getString("additional_email"),
                        timestampString = jsonResponse.getString("created_at")
                    )
                } else {
                    val reader = BufferedReader(InputStreamReader(httpsURLConnection.errorStream))
                    val response = reader.readText()
                    reader.close()

                    // Parse the JSON response to extract the token
                    val jsonResponse = JSONObject(response)
                    val message = jsonResponse.getString("message")
                    // Handle error
                    withContext(Dispatchers.Main) {
                        onEditProfileError(message)
                    }
                    return@withContext null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onEditProfileError("An error occurred: ${e.message}")
                }
                return@withContext null
            }
        }
    }
    suspend fun sendAlert(context: Context, token: String, alert: AlertRequest, onSendAlertError: (String) -> Unit): String? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("${BASE_URL}/send-alert")
                val httpsURLConnection = url.openConnection() as HttpsURLConnection
                httpsURLConnection.connectTimeout = connectionTimeout

                if (ENVIRONMENT == "development") {
                    // Set up SSL to ignore certificate validation
                    httpsURLConnection.sslSocketFactory =
                        UnsafeSSLHelper.getUnsafeSSLSocketFactory()
                    httpsURLConnection.hostnameVerifier =
                        UnsafeSSLHelper.getUnsafeHostnameVerifier()
                }

                httpsURLConnection.requestMethod = "POST"
                httpsURLConnection.setRequestProperty("Content-Type", "application/json")
                httpsURLConnection.doOutput = true

                httpsURLConnection.setRequestProperty("Authorization", "Bearer $token")

                // Prepare the JSON request body
                val jsonBody = JSONObject().apply {
                    put("event_timestamp", alert.eventTimestamp.toString("yyyy-MM-dd HH:mm:ss"))
                    put("sound_decibel_value", alert.soundDecibelValue)
                    put("sound_decibel_threshold", alert.soundDecibelThreshold)
                    put("shake_value", alert.shakeValue)
                    put("shake_threshold", alert.shakeThreshold)
                    put("gps_location", alert.gpsLocation)
                }

                // Write the request body
                val outputStream: OutputStream = httpsURLConnection.outputStream
                outputStream.write(jsonBody.toString().toByteArray())
                outputStream.flush()
                outputStream.close()

                // Check the response code
                val responseCode = httpsURLConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    // Read the response
                    val reader = BufferedReader(InputStreamReader(httpsURLConnection.inputStream))
                    val response = reader.readText()
                    reader.close()

                    // Parse the JSON response to extract the token
                    val jsonResponse = JSONObject(response)
                    val message = jsonResponse.getString("message")
                    return@withContext message
                } else {
                    val reader = BufferedReader(InputStreamReader(httpsURLConnection.errorStream))
                    val response = reader.readText()
                    reader.close()

                    // Parse the JSON response to extract the token
                    val jsonResponse = JSONObject(response)
                    val message = jsonResponse.getString("message")
                    // Handle error
                    withContext(Dispatchers.Main) {
                        onSendAlertError(message)
                    }
                    return@withContext null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onSendAlertError("An error occurred: ${e.message}")
                }
                return@withContext null
            }
        }
    }
}

// Extension function to format Date to String
fun Date.toString(format: String): String {
    val dateFormat = java.text.SimpleDateFormat(format, java.util.Locale.getDefault())
    dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
    return dateFormat.format(this)
}