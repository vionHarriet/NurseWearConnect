package com.example.nursewearconnect.utils

import androidx.compose.ui.graphics.Color
import com.example.nursewearconnect.ui.theme.*
import java.text.NumberFormat
import java.util.*
import java.util.regex.Pattern

object AppUtils {

    /**
     * Currency: formatKES(amount) → "KES 3,500.00"
     */
    fun formatKES(amount: Double?): String {
        if (amount == null) return "KES 0.00"
        val format = NumberFormat.getCurrencyInstance(Locale("en", "KE"))
        // NumberFormat for KE usually uses KES or Sh, but we want exactly "KES 3,500.00"
        val formatted = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }.format(amount)
        return "KES $formatted"
    }

    /**
     * toKES(usd) → Convert USD to KES (multiply by ~130)
     */
    fun toKES(usd: Double): Double {
        return usd * 130.0
    }

    /**
     * calcCart(items) → Calculate {subtotal, shipping, vat(16%), discount, total}
     * Assuming price is in KES already.
     */
    data class CartTotals(
        val subtotal: Double,
        val shipping: Double,
        val vat: Double,
        val discount: Double,
        val total: Double
    )

    fun calcCart(items: List<Pair<Double, Int>>, discountAmount: Double = 0.0): CartTotals {
        val subtotal = items.sumOf { it.first * it.second }
        // Free shipping over 5000 KES as per guide logic
        val shipping = if (subtotal > 5000 || subtotal == 0.0) 0.0 else 400.0
        val vat = subtotal * 0.16
        val total = subtotal + shipping + vat - discountAmount
        return CartTotals(subtotal, shipping, vat, discountAmount, total)
    }

    /**
     * Validation: isEmail(str)
     */
    fun isEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        val emailPattern = "^[A-Za-z0-9+_.-]+@(.+)$"
        return Pattern.compile(emailPattern).matcher(email).matches()
    }

    /**
     * isKEPhone(str) → validates +254 or 0 prefix with 9 digits
     */
    fun isKEPhone(phone: String?): Boolean {
        if (phone.isNullOrBlank()) return false
        // Matches +254 followed by 9 digits starting with 7 or 1, or 0 followed by 9 digits starting with 7 or 1
        val kePattern = "^(?:\\+254|0)[17]\\d{8}$"
        return Pattern.compile(kePattern).matcher(phone).matches()
    }

    /**
     * passwordStrength(pw) → 0-4 score
     */
    fun passwordStrength(password: String?): Int {
        if (password == null || password.length < 8) return 0
        var score = 0
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        return score
    }

    /**
     * Sanitization: strip HTML tags
     */
    fun sanitize(str: String?): String {
        if (str == null) return ""
        return str.replace(Regex("<[^>]*>"), "")
    }

    /**
     * truncate(str, maxLength) → add ellipsis
     */
    fun truncate(str: String?, maxLength: Int): String {
        if (str == null) return ""
        if (str.length <= maxLength) return str
        return str.substring(0, maxLength).trim() + "..."
    }

    /**
     * Date formatting: timeAgo(date)
     */
    fun timeAgo(timeInMillis: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timeInMillis
        
        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            else -> "${diff / 86400000}d ago"
        }
    }

    /**
     * Order status: colors and progress
     */
    val ORDER_STATUS_COLORS = mapOf(
        "Pending" to Color(0xFFF59E0B), // Amber
        "Processing" to Color(0xFF3B82F6), // Blue
        "In Transit" to Color(0xFF8B5CF6), // Purple
        "Delivered" to Color(0xFF10B981), // Emerald
        "Cancelled" to Color(0xFFEF4444)  // Red
    )

    val ORDER_PROGRESS = mapOf(
        "Pending" to 0.1f,
        "Processing" to 0.4f,
        "In Transit" to 0.7f,
        "Delivered" to 1.0f,
        "Cancelled" to 0.0f
    )
}
