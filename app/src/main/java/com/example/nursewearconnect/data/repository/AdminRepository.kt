package com.example.nursewearconnect.data.repository

import com.example.nursewearconnect.data.api.ApiService

class AdminRepository(private val apiService: ApiService) {
    
    suspend fun getPendingVendors(): Result<List<Map<String, Any>>> {
        return try {
            val profiles = apiService.getAllProfiles()
            val pendingVendors = profiles.filter { 
                it["role"] == "vendor" && it["status"] == "pending"
            }
            Result.success(pendingVendors) 
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun approveVendor(vendorId: String): Result<Unit> {
        return try {
            apiService.updateProfile(vendorId, mapOf("status" to "active"))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectVendor(vendorId: String): Result<Unit> {
        return try {
            apiService.updateProfile(vendorId, mapOf("status" to "rejected"))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllOrders(): Result<List<Map<String, Any>>> {
        return try {
            val orders = apiService.getAllOrders()
            // In a real app, we would join with profiles to get names.
            // For now, we'll return the raw data and let the UI handle mapping if needed.
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSystemLogs(): Result<List<Map<String, Any>>> {
        return try {
            Result.success(apiService.getSystemLogs())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
