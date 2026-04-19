package com.example.nursewearconnect.data.repository

import com.example.nursewearconnect.data.api.ApiService
import com.example.nursewearconnect.data.security.SecurityManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(
    private val supabaseClient: SupabaseClient,
    private val securityManager: SecurityManager,
    private val apiService: ApiService
) {
    private val _isLoggedIn = MutableStateFlow(securityManager.getToken() != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            val session = supabaseClient.auth.currentSessionOrNull()
            val token = session?.accessToken
            val user = session?.user
            val userId = user?.id
            
            // Try to get role from profile table first, fallback to metadata
            var role = user?.userMetadata?.get("role")?.toString()?.replace("\"", "")?.lowercase() ?: "student"
            
            if (token != null && userId != null) {
                // Use commit() for synchronous write to avoid race conditions during login navigation
                securityManager.saveToken(token)
                securityManager.saveUserId(userId)
                
                // Fetch profile roles immediately after login for fast UI transitions
                try {
                    val profiles = apiService.getProfileByUserId("eq.$userId")
                    if (profiles.isNotEmpty()) {
                        val profile = profiles.first()
                        role = (profile["role"]?.toString() ?: role).lowercase()
                        val name = profile["full_name"]?.toString() ?: ""
                        securityManager.saveUserName(name)
                    }
                } catch (e: Exception) {
                    // Fallback to metadata role already set
                }
                
                securityManager.saveUserRole(role)
                _isLoggedIn.value = true
                Result.success(Unit)
            } else {
                Result.failure(Exception("Login failed: No session received"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        email: String,
        password: String,
        fullName: String,
        phoneNumber: String,
        role: String,
        businessName: String? = null,
        location: String? = null,
        businessDescription: String? = null
    ): Result<Unit> {
        val normalizedRole = role.lowercase()
        return try {
            val metadata = mutableMapOf<String, String>(
                "full_name" to fullName,
                "phone_number" to phoneNumber,
                "role" to normalizedRole
            )
            
            if (normalizedRole == "vendor") {
                businessName?.let { metadata["business_name"] = it }
                location?.let { metadata["location"] = it }
                businessDescription?.let { metadata["business_description"] = it }
            }

            supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = kotlinx.serialization.json.buildJsonObject {
                    metadata.forEach { (k, v) ->
                        put(k, kotlinx.serialization.json.JsonPrimitive(v))
                    }
                }
            }
            
            val session = supabaseClient.auth.currentSessionOrNull()
            val token = session?.accessToken
            val user = session?.user
            val userId = user?.id
            
            if (token != null) {
                securityManager.saveToken(token)
                securityManager.saveUserRole(normalizedRole)
                userId?.let { securityManager.saveUserId(it) }
                _isLoggedIn.value = true
                Result.success(Unit)
            } else {
                // For vendors, might not get a token immediately if confirmation is required
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        securityManager.clearToken()
        _isLoggedIn.value = false
    }

    fun getUserRole(): String {
        return securityManager.getUserRole() ?: "student"
    }

    suspend fun refreshUserRole(): String {
        val userId = securityManager.getUserId() ?: return "student"
        return try {
            val profiles = apiService.getProfileByUserId("eq.$userId")
            val role = profiles.firstOrNull()?.get("role")?.toString() ?: "student"
            securityManager.saveUserRole(role)
            role
        } catch (e: Exception) {
            getUserRole()
        }
    }

    suspend fun requestPasswordReset(email: String): Result<Unit> {
        return try {
            supabaseClient.auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String, otp: String, newPassword: String): Result<Unit> {
        return try {
            // In Supabase Kotlin SDK, you usually verify OTP then update password
            supabaseClient.auth.verifyEmailOtp(
                type = io.github.jan.supabase.auth.OtpType.Email.RECOVERY,
                email = email,
                token = otp
            )
            supabaseClient.auth.updateUser {
                password = newPassword
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
