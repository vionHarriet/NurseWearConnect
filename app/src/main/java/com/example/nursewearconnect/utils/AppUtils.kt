package com.example.nursewearconnect.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import com.example.nursewearconnect.ui.theme.*
import java.io.ByteArrayOutputStream
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
     * Checks if a Supabase timestamp string is within a given date range.
     */
    fun isDateInRange(dateStr: String?, start: Long?, end: Long?): Boolean {
        if (dateStr == null) return false
        if (start == null && end == null) return true
        return try {
            // Supabase format: 2023-10-27T10:00:00+00:00
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val date = sdf.parse(dateStr.split("T")[0])?.time ?: return true
            
            // Normalize start and end to start of day if they aren't already
            // but usually DateRangePicker gives start of day UTC or local.
            val startMatch = start == null || date >= start
            val endMatch = end == null || date <= end
            startMatch && endMatch
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Error Mapping: Converts technical exceptions to user-friendly messages.
     * This method is designed to be idempotent; if it receives an already mapped
     * friendly message, it will return it as is.
     */
    fun mapThrowable(t: Throwable): String {
        val originalMessage = t.message ?: ""
        val message = originalMessage.lowercase()
        
        return when {
            message.contains("invalid_credentials") -> 
                "Invalid email or password. Please try again."
            message.contains("email_not_confirmed") -> 
                "Please confirm your email address before logging in."
            message.contains("user_already_exists") -> 
                "An account with this email already exists."
            message.contains("network") || message.contains("timeout") || message.contains("connect") -> 
                "Network error. Please check your internet connection."
            message.contains("rate_limit") || message.contains("429") ->
                "Too many attempts. Please try again later."
            message.contains("insufficient_funds") ->
                "Transaction failed: Insufficient funds."
            message.contains("expired") ->
                "Your session has expired. Please log in again."
            message.contains("forbidden") || message.contains("403") ->
                "Access denied. You don't have permission for this action."
            message.contains("not_found") || message.contains("404") ->
                "Resource not found. Please try again."
            // If the message already looks like a friendly sentence (no curly braces, no technical keywords)
            // or if it's already one of our mapped results, return it as is.
            originalMessage.isNotEmpty() && !message.contains("{") && !message.contains("exception") && 
            !message.contains("http") && !message.contains("supabase") && !message.contains("ktor") ->
                originalMessage
            else -> "An error occurred. Please try again or contact support."
        }
    }

    /**
     * Image Optimization: Compress and resize image before upload
     */
    fun optimizeImage(bytes: ByteArray, maxWidth: Int = 1024, maxHeight: Int = 1024, quality: Int = 80): ByteArray {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

        // Calculate sample size
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
        options.inJustDecodeBounds = false

        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: return bytes
        
        // Final resize if needed to match exact maxWidth/maxHeight constraints
        val resizedBitmap = if (bitmap.width > maxWidth || bitmap.height > maxHeight) {
            val scale = Math.min(maxWidth.toFloat() / bitmap.width, maxHeight.toFloat() / bitmap.height)
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
        } else {
            bitmap
        }

        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return outputStream.toByteArray()
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
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
