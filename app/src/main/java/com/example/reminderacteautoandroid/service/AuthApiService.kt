package com.example.reminderacteautoandroid.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    data class UserRequestDTO(
        val email: String,
        val password: String
    )
    data class AuthResponse(val token: String)

    @POST("/api/auth/login")
    suspend fun login(@Body request: UserRequestDTO): Response<AuthResponse>

    @POST("/api/auth/register")
    suspend fun register(@Body request: UserRequestDTO): Response<Void>
}