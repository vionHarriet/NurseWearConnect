package com.example.nursewearconnect.data.repository

import com.example.nursewearconnect.data.api.ApiService
import com.example.nursewearconnect.ui.viewmodel.CartItem
import com.example.nursewearconnect.utils.AppUtils
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

    suspend fun placeOrder(userId: String, cartItems: List<CartItem>, totalAmount: Double, shippingAddress: String, couponCode: String? = null): OrderResult {
        _orderState.value = OrderResult.Loading
        return try {
            // Optional Coupon Validation
            var finalAmount = totalAmount
            if (!couponCode.isNullOrBlank()) {
                try {
                    val coupons = apiService.getCoupons()
                    val validCoupon = coupons.find { 
                        it["code"]?.toString().equals(couponCode, ignoreCase = true) && 
                        (it["active"] as? Boolean ?: false) 
                    }
                    if (validCoupon != null) {
                        val discount = (validCoupon["discount_percent"] as? Number)?.toDouble() ?: 0.0
                        finalAmount -= (totalAmount * (discount / 100.0))
                    }
                } catch (e: Exception) {
                    // Log coupon failure but proceed with original amount
                }
            }

            val orderData = mutableMapOf(
                "user_id" to userId,
                "total_amount" to finalAmount,
                "shipping_address" to shippingAddress,
                "currency" to "KES",
                "status" to "pending"
            )
            
            if (!couponCode.isNullOrBlank()) {
                orderData["coupon_code"] = couponCode
            }

            val response = apiService.createOrder(orderData)
            val orderId = response["id"]?.toString() ?: "unknown_order_id"
            
            // Insert order items
            cartItems.forEach { item ->
                val itemData = mapOf(
                    "order_id" to orderId,
                    "product_id" to item.product.id,
                    "quantity" to item.quantity,
                    "unit_price" to item.product.priceKes,
                    "size" to item.size,
                    "color" to (item.color?.name ?: "Default"),
                    "vendor_id" to (item.product.vendor_id ?: "admin")
                )
                apiService.createOrderItem(itemData)
            }

            // Create notification for customer
            try {
                apiService.createNotification(mapOf(
                    "user_id" to userId,
                    "title" to "Order Placed",
                    "content" to "Your order #$orderId has been placed successfully. Please complete payment.",
                    "type" to "ORDER"
                ))
            } catch (e: Exception) {
                // Non-critical, ignore
            }

            val result = OrderResult.Success(orderId)
            _orderState.value = result
            result
        } catch (e: Exception) {
            val result = OrderResult.Error(AppUtils.mapThrowable(e))
            _orderState.value = result
            result
        }
    }

    suspend fun getUserOrders(filter: String): Result<List<Map<String, Any>>> {
        return try {
            Result.success(apiService.getUserOrders(filter))
        } catch (e: Exception) {
            Result.failure(Exception(AppUtils.mapThrowable(e)))
        }
    }
}
