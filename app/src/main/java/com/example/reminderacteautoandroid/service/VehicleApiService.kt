package com.example.reminderacteautoandroid.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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

    data class VehicleRequestDTO(
        val userId: Long,
        val brand: String,
        val model: String
    )

    data class DocumentRequestDTO(
        val vehicleId: Long,
        val type: String,
        val expiryDate: String
    )

    @GET("/api/vehicles")
    suspend fun getVehiclesAndDocuments(): UserResponseDTO

    @GET("/api/vehicles/{id}")
    suspend fun getVehicleById(@Path("id") id: Long): Response<VehicleResponseDTO>

    @DELETE("/api/vehicles/{id}")
    suspend fun deleteVehicle(@Path("id") id: Long): Response<Unit>

    @POST("/api/vehicles")
    suspend fun addVehicle(@Body request: VehicleRequestDTO): Response<VehicleResponseDTO>

    @PUT("/api/vehicles/{id}")
    suspend fun updateVehicle(@Path("id") id: Long, @Body request: VehicleRequestDTO): Response<VehicleResponseDTO>
}