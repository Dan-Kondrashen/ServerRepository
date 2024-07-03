package ru.kondrashen.diplomappv20.repository.responces

data class AuthResponse(
    val status: String,
    val id: Int,
    val accessToken: String,
    val refreshToken: String
)
