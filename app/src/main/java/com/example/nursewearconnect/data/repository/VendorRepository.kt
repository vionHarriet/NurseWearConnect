package com.example.nursewearconnect.data.repository

import com.example.nursewearconnect.data.api.ApiService
import com.example.nursewearconnect.model.Product

class VendorRepository(private val apiService: ApiService) {

    suspend fun getVendorProducts(vendorId: String): Result<List<Product>> {
        return try {
            val products = apiService.getVendorProducts("eq.$vendorId")
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addProduct(product: Product): Result<Product> {
        return try {
            val added = apiService.addProduct(product)
            Result.success(added)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(product: Product): Result<Product> {
        return try {
            val updated = apiService.updateProduct("eq.${product.id}", product)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            apiService.deleteProduct("eq.$productId")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVendorOrders(vendorId: String): Result<List<Map<String, Any>>> {
        return try {
            val orders = apiService.getVendorOrders("eq.$vendorId")
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> {
        return try {
            apiService.updateOrderStatus("eq.$orderId", mapOf("status" to status))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
