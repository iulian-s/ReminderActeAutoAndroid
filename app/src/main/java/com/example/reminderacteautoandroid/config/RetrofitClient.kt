package com.example.reminderacteautoandroid.config

import android.content.Context
import com.example.reminderacteautoandroid.AuthEvents
import com.example.reminderacteautoandroid.service.AuthApiService
import com.example.reminderacteautoandroid.service.DocumentApiService
import com.example.reminderacteautoandroid.service.VehicleApiService
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.getValue

const val BASE_URL = "https://iulian-s-reminderacteauto.hf.space"
//const val BASE_URL = "http://10.0.2.2:8080"
object RetrofitClient{
    private var tokenManager: TokenManager? = null

    fun init(context: Context){
        tokenManager = TokenManager(context)
    }

    private val authInterceptor = Interceptor{chain ->
        val requestBuilder = chain.request().newBuilder()

        tokenManager?.getToken()?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())

        if(response.code == 401){
            tokenManager?.deleteToken()
            runBlocking { AuthEvents.triggerLogout() }
        }
        response
    }

    private val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val authService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val vehicleService: VehicleApiService by lazy {
        retrofit.create(VehicleApiService::class.java)
    }

    val documentService: DocumentApiService by lazy {
        retrofit.create(DocumentApiService::class.java)
    }
}