package com.example.nursewearconnect.model

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val priceKes: Int,
    val rating: Double,
    val reviewsCount: Int,
    val tag: String?,
    val images: List<String>,
    val emoji: String
)

val MOCK_PRODUCTS = listOf(
    Product("1", "Women's V-Neck Core Scrub Top", "Top", 3500, 4.8, 124, "NEW", emptyList(), "👕"),
    Product("2", "Women's Jogger Scrub Pants", "Pants", 4200, 4.9, 89, null, emptyList(), "👖"),
    Product("3", "Premium Flex Scrub Set", "Set", 10500, 4.8, 201, "BESTSELLER", emptyList(), "👗"),
    Product("4", "ComfortPro Lab Coat", "Jacket", 8000, 4.6, 67, "SALE", emptyList(), "🥼"),
    Product("5", "CoolBreeze Anti-Microbial Top", "Top", 3200, 4.7, 156, null, emptyList(), "👕"),
    Product("6", "Flex-Fit Cargo Scrub Pants", "Pants", 4500, 4.5, 43, null, emptyList(), "👖")
)
