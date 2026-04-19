package com.example.nursewearconnect.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.nursewearconnect.ui.viewmodel.CartItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SecurityManager(context: Context) {
    private val gson = Gson()
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        sharedPreferences.edit().putString("auth_token", token).commit()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    fun saveUserId(userId: String) {
        sharedPreferences.edit().putString("user_id", userId).commit()
    }

    fun getUserId(): String? {
        return sharedPreferences.getString("user_id", null)
    }

    fun clearToken() {
        sharedPreferences.edit()
            .remove("auth_token")
            .remove("user_id")
            .remove("user_role")
            .remove("user_name")
            .commit()
    }

    fun saveUserRole(role: String) {
        sharedPreferences.edit().putString("user_role", role).commit()
    }

    fun getUserRole(): String? {
        return sharedPreferences.getString("user_role", null)
    }

    fun saveUserName(name: String) {
        sharedPreferences.edit().putString("user_name", name).commit()
    }

    fun getUserName(): String? {
        return sharedPreferences.getString("user_name", null)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("biometric_enabled", enabled).apply()
    }

    fun isBiometricEnabled(): Boolean {
        return sharedPreferences.getBoolean("biometric_enabled", false)
    }

    fun saveCart(cartItems: List<CartItem>) {
        val json = gson.toJson(cartItems)
        sharedPreferences.edit().putString("cart_items", json).apply()
    }

    fun getCart(): List<CartItem> {
        val json = sharedPreferences.getString("cart_items", null) ?: return emptyList()
        val type = object : TypeToken<List<CartItem>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearCart() {
        sharedPreferences.edit().remove("cart_items").apply()
    }
}
