package com.example.nursewearconnect

import android.app.Application
import com.example.nursewearconnect.data.api.ApiService
import com.example.nursewearconnect.data.api.RetrofitClient
import com.example.nursewearconnect.data.repository.*
import com.example.nursewearconnect.data.security.SecurityManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

class NurseWearApplication : Application() {

    val supabaseClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = "https://trpsejzasbfqlshrbbae.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRycHNlanphc2JmcWxzaHJiYmFlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzU4NDg4NTksImV4cCI6MjA5MTQyNDg1OX0.oD0zM5VDLXxt1onGsqCYo0HGh51bskWZjCFH5boXxSw"
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }
    }
    
    val securityManager: SecurityManager by lazy {
        SecurityManager(applicationContext)
    }
    
    val apiService: ApiService by lazy {
        RetrofitClient.getApiService(securityManager)
    }
    
    val authRepository: AuthRepository by lazy {
        AuthRepository(supabaseClient, securityManager, apiService)
    }
    
    val productRepository: ProductRepository by lazy {
        ProductRepository(apiService)
    }

    val cartRepository: CartRepository by lazy {
        CartRepository(securityManager)
    }

    val orderRepository: OrderRepository by lazy {
        OrderRepository(apiService)
    }

    val paymentRepository: PaymentRepository by lazy {
        PaymentRepository(apiService)
    }

    val userRepository: UserRepository by lazy {
        UserRepository(apiService, securityManager)
    }

    val vendorRepository: VendorRepository by lazy {
        VendorRepository(apiService)
    }

    val adminRepository: AdminRepository by lazy {
        AdminRepository(apiService)
    }
}
