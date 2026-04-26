package com.example.nursewearconnect.di

import android.content.Context
import androidx.room.Room
import com.example.nursewearconnect.data.api.ApiService
import com.example.nursewearconnect.data.local.AppDatabase
import com.example.nursewearconnect.data.repository.*
import com.example.nursewearconnect.data.security.SecurityManager
import com.example.nursewearconnect.utils.Constants
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(private val context: Context) {

    private val supabaseClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = Constants.SUPABASE_URL,
            supabaseKey = Constants.SUPABASE_ANON_KEY
        ) {
            install(Postgrest)
            install(Auth)
            install(Realtime)
            install(Storage)
        }
    }

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "nursewear_db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    private val apiService: ApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val token = securityManager.getToken() ?: Constants.SUPABASE_ANON_KEY
                val request = chain.request().newBuilder()
                    .addHeader("apikey", Constants.SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .baseUrl(Constants.SUPABASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private val securityManager: SecurityManager by lazy { SecurityManager(context) }

    val userRepository: UserRepository by lazy { UserRepository(apiService, securityManager, supabaseClient) }
    val productRepository: ProductRepository by lazy { 
        ProductRepository(apiService, database.productDao(), database.categoryDao()) 
    }
    val cartRepository: CartRepository by lazy { CartRepository(securityManager) }
    val orderRepository: OrderRepository by lazy { OrderRepository(apiService) }
    val paymentRepository: PaymentRepository by lazy { PaymentRepository(apiService) }
    val adminRepository: AdminRepository by lazy { AdminRepository(apiService) }
    val vendorRepository: VendorRepository by lazy { VendorRepository(apiService, adminRepository) }
    val authRepository: AuthRepository by lazy { AuthRepository(supabaseClient, securityManager, apiService) }
}
