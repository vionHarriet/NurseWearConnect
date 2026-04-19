package com.example.nursewearconnect.model

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val gender: String, // "Male", "Female", or "Unisex"
    val priceKes: Int,
    val rating: Double,
    val reviewsCount: Int,
    val tag: String?,
    val images: List<String>,
    val description: String = "",
    val material: String = "High-quality, breathable fabric designed for all-day comfort.",
    val features: List<String> = emptyList(),
    val inStock: Boolean = true,
    val availableSizes: List<String> = listOf("XS", "S", "M", "L", "XL", "XXL"),
    val availableColors: List<ProductColor> = listOf(
        ProductColor("Navy", 0xFF1E3A8A),
        ProductColor("Black", 0xFF000000),
        ProductColor("Teal", 0xFF0D9488)
    ),
    val subCategory: String? = null,
    val measurementGuide: Map<String, String>? = null,
    val vendor_id: String? = null
)

data class ProductColor(
    val name: String,
    val hex: Long
)
