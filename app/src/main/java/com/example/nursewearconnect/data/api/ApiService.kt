package com.example.nursewearconnect.data.api

import com.example.nursewearconnect.model.Product
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import retrofit2.http.Path

interface ApiService {
    // Products
    @GET("rest/v1/products")
    suspend fun getProducts(): List<Product>

    @GET("rest/v1/products/{id}")
    suspend fun getProductById(@Path("id") id: String): Product

    // Categories
    @GET("rest/v1/categories")
    suspend fun getCategories(): List<com.example.nursewearconnect.model.Category>
    
    @POST("rest/v1/categories")
    suspend fun addCategory(@Body category: Map<String, String>): com.example.nursewearconnect.model.Category

    @DELETE("rest/v1/categories")
    suspend fun deleteCategory(@Query("name") filter: String): Map<String, String>

    // Coupons
    @GET("rest/v1/coupons")
    suspend fun getCoupons(): List<Map<String, Any>>

    @POST("rest/v1/coupons")
    suspend fun addCoupon(@Body coupon: Map<String, Any>): Map<String, Any>

    @DELETE("rest/v1/coupons")
    suspend fun deleteCoupon(@Query("id") filter: String): Map<String, String>

    // Banners
    @GET("rest/v1/banners")
    suspend fun getBanners(): List<Map<String, Any>>

    @POST("rest/v1/banners")
    suspend fun addBanner(@Body banner: Map<String, Any>): Map<String, Any>

    @DELETE("rest/v1/banners")
    suspend fun deleteBanner(@Query("id") filter: String): Map<String, String>

    @GET("rest/v1/products?featured=eq.true")
    suspend fun getFeaturedProducts(): List<Product>

    // Orders
    @POST("rest/v1/orders")
    suspend fun createOrder(@Body orderData: Map<String, Any>): Map<String, Any>

    @POST("rest/v1/order_items")
    suspend fun createOrderItem(@Body itemData: Map<String, Any>): Map<String, Any>

    @GET("rest/v1/orders?select=*,profiles(full_name)")
    suspend fun getUserOrders(@Query("user_id") filter: String): List<Map<String, Any>>

    // Payment (These might need a separate Edge Function or external service)
    @POST("functions/v1/stk-push")
    suspend fun initiateStkPush(@Body data: Map<String, Any>): Map<String, Any>

    @GET("functions/v1/payment-status/{checkoutId}")
    suspend fun checkPaymentStatus(@Path("checkoutId") checkoutId: String): Map<String, Any>

    // User Profile
    @GET("rest/v1/profiles?select=*")
    suspend fun getProfileByUserId(@Query("id") filter: String): List<Map<String, Any>>

    @GET("rest/v1/profiles")
    suspend fun getAllProfiles(): List<Map<String, Any>>

    @PATCH("rest/v1/profiles")
    suspend fun updateProfile(@Query("id") userId: String, @Body data: Map<String, Any>)

    @GET("rest/v1/profiles?status=eq.pending&role=eq.vendor")
    suspend fun getPendingVendors(): List<Map<String, Any>>

    // Messages
    @GET("rest/v1/messages?select=*")
    suspend fun getMessages(@Query("or") filter: String): List<Map<String, Any>>

    @POST("rest/v1/messages")
    suspend fun sendMessage(@Body messageData: Map<String, Any>): Map<String, Any>

    // Notifications
    @GET("rest/v1/notifications?select=*")
    suspend fun getNotifications(@Query("user_id") filter: String): List<Map<String, Any>>

    @POST("rest/v1/notifications")
    suspend fun createNotification(@Body notification: Map<String, Any>): Map<String, Any>

    // Vendor Operations
    @GET("rest/v1/products")
    suspend fun getVendorProducts(@Query("vendor_id") filter: String): List<Product>

    @POST("rest/v1/products")
    suspend fun addProduct(@Body product: Product): Product

    @PUT("rest/v1/products")
    suspend fun updateProduct(@Query("id") id: String, @Body product: Product): Product

    @DELETE("rest/v1/products")
    suspend fun deleteProduct(@Query("id") id: String): Map<String, String>

    @GET("rest/v1/orders?select=*,profiles(full_name),order_items(*,products(name))")
    suspend fun getVendorOrders(@Query("vendor_id") filter: String): List<Map<String, Any>>

    @PATCH("rest/v1/orders")
    suspend fun updateOrderStatus(@Query("id") filter: String, @Body status: Map<String, String>): Map<String, Any>

    // Admin Operations
    @GET("rest/v1/orders?select=*,profiles(full_name),order_items(*,products(name))")
    suspend fun getAllOrders(): List<Map<String, Any>>

    @GET("rest/v1/system_logs?select=*,profiles(full_name)")
    suspend fun getSystemLogs(): List<Map<String, Any>>

    @DELETE("rest/v1/system_logs")
    suspend fun clearSystemLogs(@Query("id") filter: String = "neq.0"): Map<String, String>

    @POST("rest/v1/system_logs")
    suspend fun logAction(@Body logData: Map<String, Any>): Map<String, Any>

    // Session Management
    @GET("rest/v1/user_sessions?select=*")
    suspend fun getActiveSessions(@Query("user_id") filter: String): List<Map<String, Any>>

    @DELETE("rest/v1/user_sessions")
    suspend fun revokeSession(@Query("id") filter: String): Map<String, String>

    @GET("rest/v1/products")
    suspend fun searchProducts(@Query("name") query: String): List<Product>

    // Product Reviews
    @GET("rest/v1/reviews?select=*,profiles(full_name)")
    suspend fun getProductReviews(@Query("product_id") filter: String): List<Map<String, Any>>

    @POST("rest/v1/reviews")
    suspend fun addReview(@Body reviewData: Map<String, Any>): Map<String, Any>
}
