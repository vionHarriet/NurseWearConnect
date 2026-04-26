package com.example.nursewearconnect.model

import com.google.gson.annotations.SerializedName

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val gender: String, // "Male", "Female", or "Unisex"
    @SerializedName("price_kes")
    val priceKes: Int,
    val rating: Double,
    @SerializedName("reviews_count")
    val reviewsCount: Int,
    @SerializedName("stock_count")
    val stockCount: Int = 0,
    val tag: String?,
    val images: List<String>,
    val description: String = "",
    val material: String = "High-quality, breathable fabric designed for all-day comfort.",
    val features: List<String> = emptyList(),
    @SerializedName("in_stock")
    val inStock: Boolean = true,
    @SerializedName("available_sizes")
    val availableSizes: List<String> = listOf("XS", "S", "M", "L", "XL", "XXL"),
    @SerializedName("available_colors")
    val availableColors: List<ProductColor> = listOf(
        ProductColor("Navy", 0xFF1E3A8A),
        ProductColor("Black", 0xFF000000),
        ProductColor("Teal", 0xFF0D9488)
    ),
    @SerializedName("sub_category")
    val subCategory: String? = null,
    val measurementGuide: Map<String, String>? = null,
    val vendor_id: String? = null,
    @SerializedName("vendor_name")
    val vendorName: String? = null,
    @SerializedName("vendor_rating")
    val vendorRating: Double? = null
)

data class ProductColor(
    val name: String,
    val hex: Long
)
