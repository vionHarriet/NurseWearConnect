package com.example.nursewearconnect.data.repository

import com.example.nursewearconnect.data.api.ApiService
import com.example.nursewearconnect.ui.viewmodel.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class OrderResult {
    data class Success(val orderId: String) : OrderResult()
    data class Error(val message: String) : OrderResult()
    object Loading : OrderResult()
}

class OrderRepository(private val apiService: ApiService) {
    private val _orderState = MutableStateFlow<OrderResult?>(null)
    val orderState: StateFlow<OrderResult?> = _orderState.asStateFlow()

    suspend fun placeOrder(userId: String, cartItems: List<CartItem>, totalAmount: Double, shippingAddress: String): OrderResult {
        _orderState.value = OrderResult.Loading
        return try {
            val orderData = mapOf(
                "userId" to userId,
                "items" to cartItems.map { 
                    mapOf(
                        "productId" to it.product.id,
                        "quantity" to it.quantity,
                        "size" to it.size,
                        "color" to it.color?.name
                    )
                },
                "totalAmount" to totalAmount,
                "shippingAddress" to shippingAddress,
                "currency" to "KES"
            )
            val response = apiService.createOrder(orderData)
            val orderId = response["id"] as? String ?: "unknown_order_id"
            val result = OrderResult.Success(orderId)
            _orderState.value = result
            result
        } catch (e: Exception) {
            val result = OrderResult.Error(e.message ?: "Failed to place order")
            _orderState.value = result
            result
        }
    }

    suspend fun getUserOrders(filter: String): Result<List<Map<String, Any>>> {
        return try {
            Result.success(apiService.getUserOrders(filter))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
