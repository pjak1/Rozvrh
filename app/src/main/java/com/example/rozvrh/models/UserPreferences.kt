package com.example.rozvrh.models

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val cookies: Map<String, List<String>> = mapOf()
)
