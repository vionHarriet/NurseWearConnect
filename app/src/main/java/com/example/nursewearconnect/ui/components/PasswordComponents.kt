package com.example.nursewearconnect.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nursewearconnect.ui.theme.*

@Composable
fun PasswordStrengthSection(
    hasMinLength: Boolean,
    hasUppercase: Boolean,
    hasNumber: Boolean,
    hasSpecialChar: Boolean
) {
    val strength = when {
        hasMinLength && hasUppercase && hasNumber && hasSpecialChar -> 1f
        (hasMinLength && hasUppercase && hasNumber) || (hasMinLength && hasUppercase && hasSpecialChar) -> 0.66f
        hasMinLength -> 0.33f
        else -> 0f
    }

    val strengthColor = when {
        strength > 0.9f -> Color(0xFF10B981) // Green
        strength > 0.5f -> Color(0xFFF59E0B) // Orange
        strength > 0f -> Color(0xFFEF4444) // Red
        else -> Slate200
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Password Strength",
                fontSize = 12.sp,
                color = Slate500,
                fontWeight = FontWeight.Medium
            )
            Text(
                when {
                    strength > 0.9f -> "Strong"
                    strength > 0.5f -> "Medium"
                    strength > 0f -> "Weak"
                    else -> ""
                },
                fontSize = 12.sp,
                color = strengthColor,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { strength },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = strengthColor,
            trackColor = Slate100,
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Requirements List
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                RequirementItem("At least 8 characters", hasMinLength)
                RequirementItem("Contains uppercase", hasUppercase)
            }
            Column(modifier = Modifier.weight(1f)) {
                RequirementItem("Contains number", hasNumber)
                RequirementItem("Special character", hasSpecialChar)
            }
        }
    }
}

@Composable
fun RequirementItem(text: String, isMet: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            if (isMet) Icons.Default.CheckCircle else Icons.Default.Circle,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = if (isMet) Color(0xFF10B981) else Slate300
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (isMet) Slate900 else Slate500
        )
    }
}
