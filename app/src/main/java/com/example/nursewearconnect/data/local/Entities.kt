package com.example.nursewearconnect.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nursewearconnect.model.ProductColor

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val gender: String,
    val priceKes: Int,
    val rating: Double,
    val reviewsCount: Int,
    val stockCount: Int,
    val tag: String?,
    val images: List<String>,
    val description: String,
    val material: String,
    val features: List<String>,
    val inStock: Boolean,
    val availableSizes: List<String>,
    val availableColors: List<ProductColor>,
    val subCategory: String?,
    val vendor_id: String?,
    val vendorName: String?,
    val vendorRating: Double?
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String?
)
