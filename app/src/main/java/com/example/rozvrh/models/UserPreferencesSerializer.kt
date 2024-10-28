package com.example.rozvrh.models

import androidx.datastore.core.Serializer
import com.example.rozvrh.CryptoManager
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

class UserPreferencesSerializer(private val cryptomanager: CryptoManager
): Serializer<UserPreferences> {
    override suspend fun readFrom(input: InputStream): UserPreferences {
        val decryptedBytes = this@UserPreferencesSerializer.cryptomanager.decrypt(input)
        return try {
            Json.decodeFromString(
                deserializer = UserPreferences.serializer(),
                string = decryptedBytes.decodeToString()

            )

        } catch (e: SerializationException) {
            defaultValue
        }
    }

    override suspend fun writeTo(
        t: UserPreferences,
        output: OutputStream
    ) {
        cryptomanager.encrypt(
            bytes = Json.encodeToString(
                serializer = UserPreferences.serializer(),
                value = t
            ).encodeToByteArray(),
            outputStream = output
        )
    }

    override val defaultValue: UserPreferences
        get() = UserPreferences()
}