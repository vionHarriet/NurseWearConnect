package com.example.nursewearconnect.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nursewearconnect.ui.theme.*

@Composable
fun VendorPendingScreen(
    status: String,
    statusNotes: String?,
    onLogout: () -> Unit
) {
    val isRejected = status == "rejected"
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(if (isRejected) Color(0xFFFEF2F2) else Brand50),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRejected) Icons.Default.ErrorOutline else Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp).let { 
                        if (!isRejected) it.size(50.dp * scale) else it 
                    },
                    tint = if (isRejected) Color(0xFFEF4444) else Brand600
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = if (isRejected) "Action Required" else "Application Pending",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isRejected) 
                    "Your application needs corrections before you can start selling." 
                    else "We're currently reviewing your business details. This usually takes 24-48 hours.",
                fontSize = 16.sp,
                color = Slate600,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            if (!statusNotes.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isRejected) Color(0xFFFFF7ED) else Brand50,
                    tonalElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Feedback from Admin:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isRejected) Color(0xFFC2410C) else Brand700
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            statusNotes,
                            fontSize = 14.sp,
                            color = Slate700,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { /* Contact Support */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Brand600)
            ) {
                Icon(Icons.Default.Email, null)
                Spacer(Modifier.width(8.dp))
                Text("Contact Support")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
}
