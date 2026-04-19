package com.example.nursewearconnect.data.api

import com.example.nursewearconnect.data.api.interceptors.AuthInterceptor
import com.example.nursewearconnect.data.security.SecurityManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://trpsejzasbfqlshrbbae.supabase.co/"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRycHNlanphc2JmcWxzaHJiYmFlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzU4NDg4NTksImV4cCI6MjA5MTQyNDg1OX0.oD0zM5VDLXxt1onGsqCYo0HGh51bskWZjCFH5boXxSw"

    private var apiService: ApiService? = null

    fun getApiService(securityManager: SecurityManager): ApiService {
        return apiService ?: synchronized(this) {
            val interceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor(AuthInterceptor(securityManager, SUPABASE_KEY))
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            retrofit.create(ApiService::class.java).also { apiService = it }
        }
    }
}
