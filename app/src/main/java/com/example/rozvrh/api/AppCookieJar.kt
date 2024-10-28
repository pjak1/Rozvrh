package com.example.rozvrh.api

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import com.example.rozvrh.models.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import kotlin.collections.mapNotNull

class AppCookieJar(
    private val context: Context,
    private val dataStore: DataStore<UserPreferences>
) : CookieJar {

    private val tag = "AppCookieJar" // Tag for logging

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val domain = url.host

        runBlocking {
            // Load the current preferences
            val currentPreferences = dataStore.data.first()

            // Get the current cookies for the given domain
            val updatedCookies = currentPreferences.cookies?.toMutableMap() ?: mutableMapOf()
            val existingCookies = updatedCookies[domain]?.toMutableList() ?: mutableListOf()

            // Add new cookies and avoid duplicates
            cookies.forEach { cookie ->
                if (!existingCookies.contains(cookie.toString())) {
                    existingCookies.add(cookie.toString())
                }
            }

            // Save the updated list of cookies
            updatedCookies[domain] = existingCookies

            // Update the DataStore with the new preferences
            dataStore.updateData { currentPreferences.copy(cookies = updatedCookies) }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val domain = url.host

        // Use runBlocking to load cookies from the DataStore
        val cookies: List<String>? = runBlocking {
            dataStore.data.first().cookies?.get(domain).also { loadedCookies ->
                // Debugging: Log cookies for the domain
                Log.d(tag, "Loaded cookies for domain $domain: $loadedCookies")
            }
        }

        // Map cookies to Cookie objects, ensuring null handling
        val parsedCookies = cookies?.mapNotNull { cookieString ->
            val cookie = Cookie.parse(url, cookieString)
            // Debugging: Log each cookie being parsed
            Log.d(tag, "Parsed cookie: $cookie")
            cookie
        }

        return parsedCookies ?: emptyList()
    }
}
