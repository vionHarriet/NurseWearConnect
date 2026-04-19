package com.example.nursewearconnect.data.repository

import com.example.nursewearconnect.data.api.ApiService
import com.example.nursewearconnect.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProductRepository(private val apiService: ApiService) {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    suspend fun refreshProducts(): Result<List<Product>> {
        return try {
            val fetchedProducts = apiService.getProducts()
            _products.value = fetchedProducts
            Result.success(fetchedProducts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFeaturedProducts(): List<Product> {
        return try {
            apiService.getFeaturedProducts()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCategories(): Result<List<String>> = runCatching {
        apiService.getCategories()
    }

    suspend fun addCategory(name: String): Result<Unit> = runCatching {
        apiService.addCategory(mapOf("name" to name))
    }

    suspend fun deleteCategory(name: String): Result<Unit> = runCatching {
        apiService.deleteCategory(name)
    }

    suspend fun getCoupons(): Result<List<Map<String, Any>>> = runCatching {
        apiService.getCoupons()
    }

    suspend fun addCoupon(coupon: Map<String, Any>): Result<Unit> = runCatching {
        apiService.addCoupon(coupon)
    }

    suspend fun deleteCoupon(id: String): Result<Unit> = runCatching {
        apiService.deleteCoupon(id)
    }

    suspend fun getBanners(): Result<List<Map<String, Any>>> = runCatching {
        apiService.getBanners()
    }

    suspend fun addBanner(banner: Map<String, Any>): Result<Unit> = runCatching {
        apiService.addBanner(banner)
    }

    suspend fun deleteBanner(id: String): Result<Unit> = runCatching {
        apiService.deleteBanner(id)
    }
}
