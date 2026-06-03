package com.example.nursewearconnect.data.repository

import com.example.nursewearconnect.data.api.ApiService
import com.example.nursewearconnect.model.Product
import com.example.nursewearconnect.data.repository.AdminRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow

class VendorRepository(
    private val apiService: ApiService,
    private val adminRepository: AdminRepository,
    private val supabaseClient: SupabaseClient
) {

    fun getVendorProductsRealtime(vendorId: String): Flow<PostgresAction> {
        val channel = supabaseClient.channel("vendor_products_$vendorId")
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "products"
        }
    }

    fun getVendorOrdersRealtime(vendorId: String): Flow<PostgresAction> {
        val channel = supabaseClient.channel("vendor_orders_$vendorId")
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "orders"
        }
    }

    suspend fun getVendorProducts(vendorId: String): Result<List<Product>> {
        return try {
            val products = apiService.getVendorProducts("eq.$vendorId")
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }

    suspend fun addProduct(product: Product): Result<Product> {
        return try {
            val added = apiService.addProduct(product)
            product.vendor_id?.let {
                adminRepository.logAction(it, "ADD_PRODUCT", "Vendor added product: ${product.name}")
            }
            Result.success(added)
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }

    suspend fun updateProduct(product: Product): Result<Product> {
        return try {
            val updated = apiService.updateProduct("eq.${product.id}", product)
            product.vendor_id?.let {
                adminRepository.logAction(it, "UPDATE_PRODUCT", "Vendor updated product: ${product.name}")
            }
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }

    suspend fun deleteProduct(productId: String, vendorId: String? = null): Result<Unit> {
        return try {
            apiService.deleteProduct("eq.$productId")
            vendorId?.let {
                adminRepository.logAction(it, "DELETE_PRODUCT", "Vendor deleted product ID: $productId")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }

    suspend fun getVendorOrders(vendorId: String): Result<List<Map<String, Any>>> {
        return try {
            val orders = apiService.getVendorOrders("eq.$vendorId")
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: String, vendorId: String? = null): Result<Unit> {
        return try {
            apiService.updateOrderStatus("eq.$orderId", mapOf("status" to status))
            
            vendorId?.let {
                adminRepository.logAction(it, "UPDATE_ORDER_STATUS", "Vendor updated order $orderId to $status")
            }

            // Social Proof Trigger: Prompt for review if delivered
            if (status.lowercase() == "delivered") {
                try {
                    // Fetch order to get user_id
                    val orderResult = apiService.getAllOrders().find { it["id"] == orderId }
                    val userId = orderResult?.get("user_id")?.toString()
                    if (userId != null) {
                        apiService.createNotification(mapOf(
                            "user_id" to userId,
                            "title" to "Rate your purchase!",
                            "content" to "Your order #$orderId has been delivered. Tell us what you think and help other nurses!",
                            "type" to "REVIEW_PROMPT"
                        ))
                    }
                } catch (e: Exception) {
                    // Non-critical
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }
}
