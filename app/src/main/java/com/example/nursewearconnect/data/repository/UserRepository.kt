package com.example.nursewearconnect.data.repository

import com.example.nursewearconnect.data.api.ApiService
import com.example.nursewearconnect.data.security.SecurityManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserRepository(
    private val apiService: ApiService,
    private val securityManager: SecurityManager,
    private val supabaseClient: SupabaseClient
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
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
                val cachedRole = securityManager.getUserRole()
                val normalizedRole = (profile["role"]?.toString() ?: cachedRole ?: "student").lowercase()
                profile = profile.toMutableMap().apply {
                    this["role"] = normalizedRole
                }
            }

            _userProfile.value = profile
            
            // Sync with SecurityManager cache
            profile?.let {
                val role = it["role"]?.toString() ?: securityManager.getUserRole() ?: "student"
                val name = it["full_name"]?.toString() ?: ""
                securityManager.saveUserRole(role)
                securityManager.saveUserName(name)
            }
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }

    suspend fun updateProfile(userId: String, data: Map<String, Any>): Result<Unit> {
        return try {
            apiService.updateProfile("eq.$userId", data)
            fetchProfile(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }

    suspend fun uploadImage(userId: String, bytes: ByteArray, bucketName: String): Result<String> {
        return try {
            val bucket = supabaseClient.storage.from(bucketName)
            val extension = "jpg"
            val fileName = "${bucketName}_${java.util.UUID.randomUUID()}.$extension"
            val path = "$userId/$fileName"
            
            // Upload the file to Supabase Storage
            bucket.upload(path, bytes) {
                upsert = true
            }
            
            // Get the public URL
            val publicUrl = bucket.publicUrl(path)
            
            // Only update profile if it's an avatar upload
            if (bucketName == "avatars") {
                updateProfile(userId, mapOf("avatar_url" to publicUrl))
            }
            
            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
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
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }

    fun getMessagesRealtime(userId: String): Flow<PostgresAction> {
        val channel = supabaseClient.channel("messages_realtime")
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "messages"
        }
    }

    fun getProfileRealtime(userId: String): Flow<PostgresAction> {
        val channel = supabaseClient.channel("profile_realtime")
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "profiles"
        }
    }

    fun getNotificationsRealtime(userId: String): Flow<PostgresAction> {
        val channel = supabaseClient.channel("notifications_realtime")
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "notifications"
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

    suspend fun getActiveSessions(userId: String): List<Map<String, Any>> {
        return try {
            apiService.getActiveSessions("eq.$userId")
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun revokeSession(sessionId: String): Result<Unit> {
        return try {
            apiService.revokeSession("eq.$sessionId")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(com.example.nursewearconnect.utils.AppUtils.mapThrowable(e)))
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        securityManager.setBiometricEnabled(enabled)
    }

    fun isBiometricEnabled(): Boolean {
        return securityManager.isBiometricEnabled()
    }
}
