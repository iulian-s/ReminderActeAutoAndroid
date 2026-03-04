package com.example.reminderacteautoandroid.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserApiService {
    data class ChangePasswordRequestDTO(
        val oldPassword: String,
        val newPassword: String
    )

    @POST("/api/users/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequestDTO): Response<Unit>

    @DELETE("/api/users")
    suspend fun deleteAccount(@Query("inputPassword") inputPassword: String): Response<Unit>

    @GET("/api/users/me")
    suspend fun getCurrentUser(): Response<VehicleApiService.UserResponseDTO>
}