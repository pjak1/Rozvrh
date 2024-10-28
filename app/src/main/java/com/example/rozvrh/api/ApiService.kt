package com.example.rozvrh.api

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.example.rozvrh.CryptoManager
import com.example.rozvrh.models.UserPreferences
import com.example.rozvrh.models.UserPreferencesSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException

private val Context.userPreferencesDataStore: DataStore<UserPreferences> by dataStore(
    fileName = "user_prefs.json",
    serializer = UserPreferencesSerializer(CryptoManager())
)

class ApiService(context: Context) {

    private val client = OkHttpClient.Builder()
        .cookieJar(AppCookieJar(context, context.userPreferencesDataStore))
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    // Function to initiate the login and fetch timetable
    fun initiateLogin(
        username: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (Int) -> Unit,
        onFailure: (IOException) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            sendRequest("https://is.muni.cz/auth/", object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onFailure(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        onError(response.code)
                    } else {
                        handleSuccessfulAuthResponse(response, username, password, onSuccess, onError, onFailure)
                    }
                }
            })
        }
    }

    private fun handleSuccessfulAuthResponse(
        response: Response,
        username: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (Int) -> Unit,
        onFailure: (IOException) -> Unit
    ) {
        val redirectUrl = response.request.url.toString()
        println("Redirected to: $redirectUrl")
        sendLoginRequest(redirectUrl, username, password, onSuccess, onError, onFailure)
    }

    private fun sendLoginRequest(
        url: String,
        username: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (Int) -> Unit,
        onFailure: (IOException) -> Unit
    ) {
        val formBody = FormBody.Builder()
            .add("akce", "login")
            .add("credential_0", username)
            .add("credential_1", password)
            .add("uloz", "uloz")
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFailure(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onError(response.code)
                } else {
                    onSuccess()
                }
            }
        })
    }

    fun fetchTimeTableData(
        onSuccess: (response: Response) -> Unit
    ) {
        val timetableUrl = "https://is.muni.cz/auth/rozvrh/zobraz/muj?pref=w&fakulta=1421&obdobi=9483&format=xml&bv=s&tyden=0&od=&do=&vybrat=1"
        sendRequest(timetableUrl, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("table_fetch", "Request failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.d("table_fetch", "Response error: ${response.code}")
                } else {
                    onSuccess(response)
                }
            }
        })
    }

    // Generic GET request without a body
    fun sendRequest(url: String, callback: Callback) {
        val request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request).enqueue(callback)
    }
}
