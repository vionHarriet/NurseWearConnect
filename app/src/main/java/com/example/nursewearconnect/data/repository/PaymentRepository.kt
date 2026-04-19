package com.example.nursewearconnect.data.repository

import com.example.nursewearconnect.data.api.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class PaymentStatus {
    object Idle : PaymentStatus()
    object Loading : PaymentStatus()
    data class Success(val checkoutId: String) : PaymentStatus()
    data class Error(val message: String) : PaymentStatus()
}

class PaymentRepository(private val apiService: ApiService) {
    private val _paymentState = MutableStateFlow<PaymentStatus>(PaymentStatus.Idle)
    val paymentState: StateFlow<PaymentStatus> = _paymentState.asStateFlow()

    suspend fun initiateMpesaPayment(orderId: String, phoneNumber: String, amount: Double): PaymentStatus {
        _paymentState.value = PaymentStatus.Loading
        return try {
            val paymentData = mapOf(
                "orderId" to orderId,
                "phoneNumber" to phoneNumber,
                "amount" to amount,
                "type" to "MPESA_STK_PUSH"
            )
            val response = apiService.initiateStkPush(paymentData)
            val checkoutId = response["CheckoutRequestID"] as? String ?: "unknown_checkout_id"
            val result = PaymentStatus.Success(checkoutId)
            _paymentState.value = result
            result
        } catch (e: Exception) {
            val result = PaymentStatus.Error(e.message ?: "Payment initiation failed")
            _paymentState.value = result
            result
        }
    }

    suspend fun checkStatus(checkoutId: String): Map<String, Any> {
        return try {
            apiService.checkPaymentStatus(checkoutId)
        } catch (e: Exception) {
            mapOf("status" to "ERROR", "message" to (e.message ?: "Failed to check status"))
        }
    }
}
