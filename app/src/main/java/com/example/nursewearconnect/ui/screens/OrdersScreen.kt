package com.example.nursewearconnect.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.example.nursewearconnect.ui.viewmodel.HomeViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nursewearconnect.ui.components.OrderTimelineView
import com.example.nursewearconnect.ui.theme.*

data class UpdateNotification(
    val title: String,
    val description: String,
    val time: String,
    val isUnread: Boolean,
    val icon: ImageVector,
    val color: Color,
    val bgColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    innerPadding: PaddingValues,
    viewModel: HomeViewModel,
    onNavigateToNotifications: () -> Unit = {},
    onSupportClick: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("Active") }
    val filters = listOf("Active (1)", "Processing", "Delivered", "Returned")

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(bottom = innerPadding.calculateBottomPadding())
    ) {
        // Custom Responsive Header
        Surface(
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.statusBarsPadding()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Orders & Tracking",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ),
                        color = Slate900,
                        modifier = Modifier.weight(1f)
                    )
                    
                    IconButton(
                        onClick = onNavigateToNotifications,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box {
                            Icon(
                                Icons.Default.NotificationsNone,
                                contentDescription = "Notifications",
                                modifier = Modifier.size(24.dp),
                                tint = Slate900
                            )
                            if (uiState.unreadNotificationsCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFFF43F5E), CircleShape)
                                        .border(1.5.dp, Color.White, CircleShape)
                                        .align(Alignment.TopEnd)
                                )
                            }
                        }
                    }
                }

                // Filter Bar integrated into the sticky header
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { filter ->
                        FilterChip(
                            label = filter,
                            isSelected = filter.contains(selectedFilter),
                            onClick = { selectedFilter = filter.split(" ")[0] }
                        )
                    }
                }
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Brand600)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Active Order Section
                val activeOrder = uiState.allOrders.find { it["status"]?.toString()?.lowercase() in listOf("processing", "shipped", "in transit") }
                if (activeOrder != null) {
                    item {
                        ActiveOrderCard(order = activeOrder, onSupportClick = onSupportClick)
                    }
                }

                // Updates Section
                if (uiState.notifications.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Recent Updates", badge = if (uiState.unreadNotificationsCount > 0) "${uiState.unreadNotificationsCount} New" else null)
                        UpdatesList(notifications = uiState.notifications.take(3))
                    }
                }

                // Past Orders Section
                item {
                    SectionHeader(title = "Past Orders")
                    if (uiState.allOrders.isEmpty()) {
                        EmptyOrdersState()
                    } else {
                        // Display items from allOrders
                        uiState.allOrders.forEach { order ->
                            PastOrderCard(order)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyOrdersState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(48.dp), tint = Slate300)
        Spacer(Modifier.height(16.dp))
        Text("No past orders found", color = Slate500, fontSize = 14.sp)
    }
}

@Composable
fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Brand600 else Color.White,
        border = if (isSelected) null else BorderStroke(1.dp, Slate200),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Slate600
        )
    }
}

@Composable
fun ActiveOrderCard(order: Map<String, Any>, onSupportClick: () -> Unit = {}) {
    val orderId = order["id"]?.toString()?.takeLast(8) ?: "Unknown"
    val status = order["status"]?.toString() ?: "Processing"
    val totalAmount = order["total_amount"]?.toString() ?: "0"
    
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("Order #$orderId", fontSize = 12.sp, color = Slate500)
                    Text("Status: $status", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                }
                
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFDBEAFE))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFF3B82F6), CircleShape))
                        Text(status.replaceFirstChar { it.uppercase() }, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Items Preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate50)
                        .border(1.dp, Slate100, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Inventory2, null, modifier = Modifier.align(Alignment.Center).size(24.dp), tint = Slate300)
                }
                Column {
                    Text("Order Items", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Slate700)
                    Text("KSh $totalAmount Total", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Brand600)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = Slate100)

            OrderTimelineView(status = status) 

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Brand600)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Track Package", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(11.dp))
                    }
                }
                IconButton(
                    onClick = onSupportClick,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, Slate200, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.SupportAgent, null, modifier = Modifier.size(18.dp), tint = Slate600)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, badge: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
        if (badge != null) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Brand50
            ) {
                Text(
                    badge,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Brand600
                )
            }
        }
    }
}

@Composable
fun UpdatesList(notifications: List<Map<String, Any>>) {
    Surface(
        modifier = Modifier.padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            notifications.forEach { notif ->
                val type = notif["type"]?.toString() ?: "system"
                UpdateItem(
                    UpdateNotification(
                        title = notif["title"]?.toString() ?: "Update",
                        description = notif["message"]?.toString() ?: "",
                        time = notif["time"]?.toString() ?: "Recently",
                        isUnread = !(notif["isRead"] as? Boolean ?: true),
                        icon = when(type) {
                            "order" -> Icons.Default.MoveToInbox
                            "promo" -> Icons.Default.LocalOffer
                            else -> Icons.Default.Notifications
                        },
                        color = when(type) {
                            "order" -> Brand500
                            "promo" -> Color(0xFFF43F5E)
                            else -> Slate600
                        },
                        bgColor = when(type) {
                            "order" -> Brand50
                            "promo" -> Color(0xFFFFF1F2)
                            else -> Slate50
                        }
                    )
                )
            }
        }
    }
}

@Composable
fun UpdateItem(update: UpdateNotification) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (update.isUnread) Brand50.copy(alpha = 0.5f) else Color.Transparent)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(if (update.isUnread) Color.White else update.bgColor, CircleShape)
                .then(if (update.isUnread) Modifier.border(1.dp, Brand100, CircleShape) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Icon(update.icon, null, modifier = Modifier.size(18.dp), tint = update.color)
            if (update.isUnread) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Brand500, CircleShape)
                        .border(1.5.dp, Color.White, CircleShape)
                        .align(Alignment.TopEnd)
                )
            }
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(update.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Text(update.description, fontSize = 11.sp, color = if (update.isUnread) Slate600 else Slate500, lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(update.time, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Slate400)
        }
    }
}

@Composable
fun PastOrderCard(order: Map<String, Any> = emptyMap()) {
    val orderId = order["id"]?.toString()?.takeLast(8) ?: "NW-7210"
    val fullDate = order["created_at"]?.toString() ?: ""
    val date = if (fullDate.length >= 10) fullDate.substring(0, 10) else "Sep 28, 2023"
    val status = order["status"]?.toString()?.replaceFirstChar { it.uppercase() } ?: "Delivered"

    Surface(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Slate50),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(28.dp), tint = Slate300)
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Order #$orderId", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    Text(status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (status == "Delivered") Color(0xFF059669) else Slate400)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("$date • KSh ${order["total_amount"] ?: "0"}", fontSize = 12.sp, color = Slate500)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("View Details", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Brand600)
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(12.dp), tint = Brand600)
                }
            }

            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(32.dp)
                    .background(Slate50, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(20.dp), tint = Slate600)
            }
        }
    }
}
