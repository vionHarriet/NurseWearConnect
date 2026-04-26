package com.example.nursewearconnect.model

import com.google.gson.annotations.SerializedName

data class Category(
    val id: String,
    val name: String,
    @SerializedName("created_at")
    val createdAt: String? = null
)
