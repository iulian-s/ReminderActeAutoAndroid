package com.example.reminderacteautoandroid.service
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.time.LocalDate

interface DocumentApiService {
    data class DocumentInput(
        val id: Long? = null,
        var type: String = "",
        var expiryDate: LocalDate? = null
    )

    @POST("/api/documents/vehicle/{vehicleId}")
    suspend fun addDocument(
        @Path("vehicleId") vehicleId: Long,
        @Body request: VehicleApiService.DocumentRequestDTO?
    ): Response<VehicleApiService.DocumentResponseDTO>

    @DELETE("/api/documents/{id}")
    suspend fun deleteDocument(
        @Path("id") id: Long
    ): Response<Unit>

    @PUT("/api/documents/{id}")
    suspend fun updateDocument(
        @Path("id") id: Long,
        @Body request: VehicleApiService.DocumentRequestDTO
    ): Response<VehicleApiService.DocumentResponseDTO>

    @GET("/api/documents/{vehicleId}")
    suspend fun getDocumentsByVehicleId(
        @Path("vehicleId") vehicleId: Long
    ): Response<List<VehicleApiService.DocumentResponseDTO>>
}