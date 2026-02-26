package com.example.reminderacteautoandroid.service

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import java.time.LocalDate
import java.time.LocalDateTime

interface VehicleApiService {
    data class DocumentResponseDTO(
        val id: Long,
        val vehicleId: Long,
        val type: String,
        val expiryDate: String,
    )
    data class VehicleResponseDTO(
        val id: Long,
        val userId: Long,
        val brand: String,
        val model: String,
        val documents: MutableSet<DocumentResponseDTO> = mutableSetOf()
    )

    data class UserResponseDTO(
        val id: Long,
        val email: String,
        val isVerified: Boolean,
        val createdAt: String,
        val vehicles: MutableSet<VehicleResponseDTO> = mutableSetOf()
    )

    @GET("/api/vehicles")
    suspend fun getVehiclesAndDocuments(): UserResponseDTO

    @DELETE("/api/vehicles/{id}")
    suspend fun deleteVehicle(@Path("id") id: Long): Response<Unit>
}