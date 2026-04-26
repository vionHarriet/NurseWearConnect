package com.example.nursewearconnect.data.repository

import com.example.nursewearconnect.data.api.ApiService
import com.example.nursewearconnect.data.local.ProductDao
import com.example.nursewearconnect.data.local.ProductEntity
import com.example.nursewearconnect.model.Product
import com.example.nursewearconnect.model.ProductColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class ProductRepository(
    private val apiService: ApiService,
    private val productDao: ProductDao,
    private val categoryDao: com.example.nursewearconnect.data.local.CategoryDao
) {
    val products: Flow<List<Product>> = productDao.getAllProducts().map { entities ->
        entities.map { it.toDomain() }
    }

    val categories: Flow<List<com.example.nursewearconnect.model.Category>> = categoryDao.getAllCategories().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun refreshProducts(): Result<List<Product>> {
        return try {
            val fetchedProducts = apiService.getProducts()
            productDao.refreshProducts(fetchedProducts.map { it.toEntity() })
            Result.success(fetchedProducts)
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }

    suspend fun getFeaturedProducts(): List<Product> {
        return try {
            apiService.getFeaturedProducts()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCategories(): Result<List<com.example.nursewearconnect.model.Category>> = try {
        val fetched = apiService.getCategories()
        categoryDao.insertCategories(fetched.map { it.toEntity() })
        Result.success(fetched)
    } catch (e: Exception) {
        Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
    }

    suspend fun addCategory(name: String, adminId: String? = null): Result<Unit> = try {
        apiService.addCategory(mapOf("name" to name))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
    }

    suspend fun deleteCategory(name: String, adminId: String? = null): Result<Unit> = try {
        apiService.deleteCategory("eq.$name")
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
    }

    suspend fun getCoupons(): Result<List<Map<String, Any>>> = try {
        Result.success(apiService.getCoupons())
    } catch (e: Exception) {
        Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
    }

    suspend fun addCoupon(coupon: Map<String, Any>, adminId: String? = null): Result<Unit> = try {
        apiService.addCoupon(coupon)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
    }

    suspend fun deleteCoupon(id: String, adminId: String? = null): Result<Unit> = try {
        apiService.deleteCoupon("eq.$id")
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
    }

    suspend fun getBanners(): Result<List<Map<String, Any>>> = try {
        Result.success(apiService.getBanners())
    } catch (e: Exception) {
        Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
    }

    suspend fun addBanner(banner: Map<String, Any>, adminId: String? = null): Result<Unit> = try {
        apiService.addBanner(banner)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
    }

    suspend fun deleteBanner(id: String, adminId: String? = null): Result<Unit> = try {
        apiService.deleteBanner("eq.$id")
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
    }

    suspend fun searchProducts(query: String): Result<List<Product>> {
        return try {
            // Server-side full-text search using Supabase Postgrest
            // The 'fts' filter uses the search index if created in SQL
            val results = apiService.searchProducts("fts.$query")
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }
    suspend fun getProductReviews(productId: String): Result<List<Map<String, Any>>> {
        return try {
            Result.success(apiService.getProductReviews("eq.$productId"))
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }

    suspend fun addReview(productId: String, userId: String, rating: Int, comment: String): Result<Unit> {
        return try {
            val reviewData = mapOf(
                "product_id" to productId,
                "user_id" to userId,
                "rating" to rating,
                "comment" to comment
            )
            apiService.addReview(reviewData)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }
}

fun ProductEntity.toDomain() = Product(
    id = id,
    name = name,
    category = category,
    gender = gender,
    priceKes = priceKes,
    rating = rating,
    reviewsCount = reviewsCount,
    stockCount = stockCount,
    tag = tag,
    images = images,
    description = description,
    material = material,
    features = features,
    inStock = inStock,
    availableSizes = availableSizes,
    availableColors = availableColors,
    subCategory = subCategory,
    vendor_id = vendor_id,
    vendorName = vendorName,
    vendorRating = vendorRating
)

fun Product.toEntity() = ProductEntity(
    id = id,
    name = name,
    category = category,
    gender = gender,
    priceKes = priceKes,
    rating = rating,
    reviewsCount = reviewsCount,
    stockCount = stockCount,
    tag = tag,
    images = images,
    description = description,
    material = material,
    features = features,
    inStock = inStock,
    availableSizes = availableSizes,
    availableColors = availableColors,
    subCategory = subCategory,
    vendor_id = vendor_id,
    vendorName = vendorName,
    vendorRating = vendorRating
)

fun com.example.nursewearconnect.data.local.CategoryEntity.toDomain() = com.example.nursewearconnect.model.Category(
    id = id,
    name = name
)

fun com.example.nursewearconnect.model.Category.toEntity() = com.example.nursewearconnect.data.local.CategoryEntity(
    id = id,
    name = name,
    icon = null
)
