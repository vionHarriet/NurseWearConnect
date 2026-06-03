package com.example.nursewearconnect.data.repository

import com.example.nursewearconnect.data.api.ApiService

class AdminRepository(private val apiService: ApiService) {
    
    suspend fun getPendingVendors(): Result<List<Map<String, Any>>> {
        return try {
            Result.success(apiService.getPendingVendors())
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }

    suspend fun approveVendor(vendorId: String, adminId: String): Result<Unit> {
        return try {
            apiService.updateProfile("eq.$vendorId", mapOf("status" to "active"))
            logAction(adminId, "APPROVE_VENDOR", "Approved vendor profile: $vendorId")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }

    suspend fun rejectVendor(vendorId: String, adminId: String, notes: String? = null): Result<Unit> {
        return try {
            val updateData = mutableMapOf<String, Any>("status" to "rejected")
            notes?.let { updateData["status_notes"] = it }
            apiService.updateProfile("eq.$vendorId", updateData)
            logAction(adminId, "REJECT_VENDOR", "Rejected vendor profile: $vendorId. Notes: ${notes ?: "None"}")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }

    suspend fun getAllOrders(
        status: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        searchQuery: String? = null,
        limit: Int? = null,
        offset: Int? = null
    ): Result<List<Map<String, Any>>> {
        return try {
            val statusParam = status?.let { "eq.$it" }
            val gteDate = startDate?.let { "gte.$it" }
            val lteDate = endDate?.let { "lte.$it" }
            val idSearch = searchQuery?.let { "ilike.*$it*" }

            val orders = apiService.getAllOrders(
                status = statusParam,
                gteDate = gteDate,
                lteDate = lteDate,
                idSearch = idSearch,
                limit = limit,
                offset = offset
            )
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }

    suspend fun getSystemLogs(
        startDate: String? = null,
        endDate: String? = null,
        limit: Int? = null,
        offset: Int? = null
    ): Result<List<Map<String, Any>>> {
        return try {
            val gteDate = startDate?.let { "gte.$it" }
            val lteDate = endDate?.let { "lte.$it" }

            Result.success(apiService.getSystemLogs(
                gteDate = gteDate,
                lteDate = lteDate,
                limit = limit,
                offset = offset
            ))
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }

    suspend fun clearSystemLogs(): Result<Unit> {
        return try {
            apiService.clearSystemLogs()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }

    suspend fun logAction(userId: String, action: String, details: String, severity: String = "info"): Result<Unit> {
        return try {
            val logData = mapOf(
                "user_id" to userId,
                "action" to action,
                "details" to details,
                "severity" to severity
            )
            apiService.logAction(logData)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }

    suspend fun getAllUsers(): Result<List<Map<String, Any>>> {
        return try {
            Result.success(apiService.getAllProfiles())
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }

    // Payouts Management
    suspend fun getPayouts(): Result<List<Map<String, Any>>> {
        return try {
            Result.success(apiService.getPayouts())
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }

    suspend fun createPayout(vendorId: String, amount: Int, adminId: String): Result<Unit> {
        return try {
            val payoutData = mapOf(
                "vendor_id" to vendorId,
                "amount" to amount,
                "status" to "pending"
            )
            apiService.createPayout(payoutData)
            logAction(adminId, "CREATE_PAYOUT", "Created payout for vendor $vendorId: KSh $amount")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }

    suspend fun updatePayoutStatus(payoutId: String, status: String, reference: String?, adminId: String): Result<Unit> {
        return try {
            val updateData = mutableMapOf("status" to status)
            reference?.let { updateData["reference_number"] = it }
            if (status == "paid") {
                updateData["processed_at"] = java.time.OffsetDateTime.now().toString()
            }
            apiService.updatePayoutStatus("eq.$payoutId", updateData)
            logAction(adminId, "UPDATE_PAYOUT", "Updated payout $payoutId to $status. Ref: $reference")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }
}
