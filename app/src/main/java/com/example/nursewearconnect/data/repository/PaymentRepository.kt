package com.example.nursewearconnect.data.repository

import com.example.nursewearconnect.data.api.ApiService
import com.example.nursewearconnect.utils.AppUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class PaymentStatus {
    object Idle : PaymentStatus()
    object Loading : PaymentStatus()
    data class Success(val checkoutId: String) : PaymentStatus()
    data class Completed(val transactionId: String) : PaymentStatus()
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
            
            // Audit Log: Payment Initiated
            try {
                apiService.logAction(mapOf(
                    "user_id" to "system", // The caller should ideally pass userId
                    "action" to "PAYMENT_INITIATED",
                    "details" to "STK Push initiated for order #$orderId (KES $amount)",
                    "severity" to "info"
                ))
            } catch (e: Exception) {}

            val result = PaymentStatus.Success(checkoutId)
            _paymentState.value = result
            result
        } catch (e: Exception) {
            val result = PaymentStatus.Error(AppUtils.mapThrowable(e))
            _paymentState.value = result
            result
        }
    }

    suspend fun checkStatus(checkoutId: String): Map<String, Any> {
        return try {
            val status = apiService.checkPaymentStatus(checkoutId)
            val resultCode = status["ResultCode"]?.toString()
            if (resultCode == "0") {
                _paymentState.value = PaymentStatus.Completed(status["MpesaReceiptNumber"]?.toString() ?: "TRANS_OK")
            } else if (resultCode != null && resultCode != "PENDING") {
                _paymentState.value = PaymentStatus.Error(status["ResultDesc"]?.toString() ?: "Payment Failed")
            }
            status
        } catch (e: Exception) {
            mapOf("status" to "ERROR", "message" to (e.message ?: "Failed to check status"))
        }
    }
}
