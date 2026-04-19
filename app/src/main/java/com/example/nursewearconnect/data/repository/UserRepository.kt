package com.example.nursewearconnect.data.repository

import com.example.nursewearconnect.data.api.ApiService
import com.example.nursewearconnect.data.security.SecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserRepository(
    private val apiService: ApiService,
    private val securityManager: SecurityManager
) {
    private val _userProfile = MutableStateFlow<Map<String, Any>?>(null)
    val userProfile: StateFlow<Map<String, Any>?> = _userProfile

    fun initFromCache() {
        val cachedName = securityManager.getUserName()
        val cachedRole = securityManager.getUserRole()
        if (cachedName != null || cachedRole != null) {
            _userProfile.value = mapOf(
                "full_name" to (cachedName ?: ""),
                "role" to (cachedRole ?: "student")
            )
        }
    }

    init {
        initFromCache()
    }

    suspend fun fetchProfile(userId: String): Result<Map<String, Any>?> {
        return try {
            val profiles = apiService.getProfileByUserId("eq.$userId")
            var profile = profiles.firstOrNull()
            
            // Normalize role in the profile map
            if (profile != null) {
                val normalizedRole = (profile["role"]?.toString() ?: "student").lowercase()
                profile = profile.toMutableMap().apply {
                    this["role"] = normalizedRole
                }
            }

            _userProfile.value = profile
            
            // Sync with SecurityManager cache
            profile?.let {
                val role = it["role"]?.toString() ?: "student"
                val name = it["full_name"]?.toString() ?: ""
                securityManager.saveUserRole(role)
                securityManager.saveUserName(name)
            }
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(userId: String, data: Map<String, Any>): Result<Unit> {
        return try {
            apiService.updateProfile("eq.$userId", data)
            fetchProfile(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllUsers(): List<Map<String, Any>> {
        return try {
            apiService.getAllProfiles()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getNotifications(userId: String): List<Map<String, Any>> {
        return try {
            apiService.getNotifications("eq.$userId")
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMessages(userId: String): List<Map<String, Any>> {
        return try {
            val filter = "(sender_id.eq.$userId,receiver_id.eq.$userId)"
            apiService.getMessages(filter)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun sendMessage(messageData: Map<String, Any>): Result<Map<String, Any>> {
        return try {
            val response = apiService.sendMessage(messageData)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserRole(): String? {
        return securityManager.getUserRole()
    }

    fun getUserName(): String? {
        return securityManager.getUserName()
    }

    fun getUserId(): String? {
        return securityManager.getUserId()
    }

    fun logout() {
        securityManager.clearToken()
        _userProfile.value = null
    }
}
