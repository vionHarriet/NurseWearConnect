package com.example.nursewearconnect.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>): List<Long>

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts(): Int

    @Transaction
    suspend fun refreshProducts(products: List<ProductEntity>): Int {
        deleteAllProducts()
        insertProducts(products)
        return products.size
    }
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>): List<Long>

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories(): Int
}
