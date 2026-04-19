package com.example.nursewearconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Redeem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nursewearconnect.ui.viewmodel.HomeViewModel
import com.example.nursewearconnect.ui.theme.*

data class NotificationItem(
    val id: Int,
    val title: String,
    val message: String,
    val time: String,
    val type: NotificationType,
    val isRead: Boolean = false
)

enum class NotificationType {
    ORDER, PROMO, SYSTEM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(onBackClick: () -> Unit, viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedNotification by remember { mutableStateOf<NotificationItem?>(null) }
    
    val notifications = uiState.notifications.map { notifMap ->
        NotificationItem(
            id = (notifMap["id"] as? Double)?.toInt() ?: 0,
            title = notifMap["title"] as? String ?: "Notification",
            message = notifMap["message"] as? String ?: "",
            time = notifMap["time"] as? String ?: "",
            type = when(notifMap["type"] as? String) {
                "order" -> NotificationType.ORDER
                "promo" -> NotificationType.PROMO
                else -> NotificationType.SYSTEM
            },
            isRead = notifMap["isRead"] as? Boolean ?: false
        )
    }.ifEmpty {
        listOf(
            NotificationItem(1, "Order Shipped", "Your order #NW1234 has been shipped and is on its way. Estimated delivery: Tomorrow by 5 PM.", "2m ago", NotificationType.ORDER),
            NotificationItem(2, "Flash Sale!", "Get 20% off on all scrub sets this weekend only. Use code NURSE20 at checkout.", "1h ago", NotificationType.PROMO),
            NotificationItem(3, "Account Verified", "Your vendor profile has been successfully verified. You can now start listing your products in the catalog.", "3h ago", NotificationType.SYSTEM, true),
            NotificationItem(4, "New Message", "You have a new message from Elite Scrubs Support regarding your custom order.", "5h ago", NotificationType.SYSTEM, true)
        )
    }

    if (selectedNotification != null) {
        NotificationDetailDialog(
            notification = selectedNotification!!,
            onDismiss = { 
                if (!selectedNotification!!.isRead) {
                    viewModel.markNotificationAsRead(selectedNotification!!.id)
                }
                selectedNotification = null 
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Slate50
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(notifications) { notification ->
                NotificationRow(notification) { selectedNotification = notification }
            }
        }
    }
}

@Composable
fun NotificationDetailDialog(notification: NotificationItem, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = when (notification.type) {
                                NotificationType.ORDER -> Color(0xFFE0F2FE)
                                NotificationType.PROMO -> Color(0xFFFEF3C7)
                                NotificationType.SYSTEM -> Slate100
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (notification.type) {
                            NotificationType.ORDER -> Icons.Outlined.LocalShipping
                            NotificationType.PROMO -> Icons.Outlined.Redeem
                            NotificationType.SYSTEM -> Icons.Outlined.Notifications
                        },
                        contentDescription = null,
                        tint = when (notification.type) {
                            NotificationType.ORDER -> Color(0xFF0284C7)
                            NotificationType.PROMO -> Color(0xFFD97706)
                            NotificationType.SYSTEM -> Slate600
                        },
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(notification.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(notification.time, fontSize = 12.sp, color = Slate400)
                Spacer(Modifier.height(12.dp))
                Text(notification.message, fontSize = 15.sp, color = Slate700, lineHeight = 22.sp)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun NotificationRow(notification: NotificationItem, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (notification.isRead) Color.White else Brand50.copy(alpha = 0.5f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = when (notification.type) {
                            NotificationType.ORDER -> Color(0xFFE0F2FE)
                            NotificationType.PROMO -> Color(0xFFFEF3C7)
                            NotificationType.SYSTEM -> Slate100
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notification.type) {
                        NotificationType.ORDER -> Icons.Outlined.LocalShipping
                        NotificationType.PROMO -> Icons.Outlined.Redeem
                        NotificationType.SYSTEM -> Icons.Outlined.Notifications
                    },
                    contentDescription = null,
                    tint = when (notification.type) {
                        NotificationType.ORDER -> Color(0xFF0284C7)
                        NotificationType.PROMO -> Color(0xFFD97706)
                        NotificationType.SYSTEM -> Slate600
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = notification.time,
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = Slate600,
                    lineHeight = 18.sp
                )
            }
            
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp, top = 6.dp)
                        .size(8.dp)
                        .background(Brand500, CircleShape)
                )
            }
        }
    }
}
