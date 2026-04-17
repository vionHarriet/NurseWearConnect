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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nursewearconnect.ui.theme.*

data class TrackingStep(
    val title: String,
    val subtitle: String,
    val isCompleted: Boolean,
    val isActive: Boolean,
    val icon: ImageVector
)

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
fun OrdersScreen(innerPadding: PaddingValues) {
    var selectedFilter by remember { mutableStateOf("Active") }
    val filters = listOf("Active (1)", "Processing", "Delivered", "Returned")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Orders & Tracking",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .background(Slate50, CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp),
                            tint = Slate600
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .background(Slate50, CircleShape)
                    ) {
                        Box {
                            Icon(
                                Icons.Default.NotificationsNone,
                                contentDescription = "Notifications",
                                modifier = Modifier.size(20.dp),
                                tint = Slate600
                            )
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(0xFFF43F5E), CircleShape)
                                    .border(2.dp, Color.White, CircleShape)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                )
            )
        },
        containerColor = Slate50
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Filter Bar
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp),
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

            // Active Order Section
            item {
                ActiveOrderCard()
            }

            // Updates Section
            item {
                SectionHeader(title = "Updates", badge = "2 New")
                UpdatesList()
            }

            // Past Orders Section
            item {
                SectionHeader(title = "Past Orders")
                PastOrderCard()
            }
        }
    }
}

@Composable
fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(100.dp),
        color = if (isSelected) Slate900 else Color.White,
        border = if (isSelected) null else BorderStroke(1.dp, Slate200),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Slate600
        )
    }
}

@Composable
fun ActiveOrderCard() {
    Surface(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Brand100),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("Order #NW-8492", fontSize = 12.sp, color = Slate500)
                    Text("Arriving Oct 17", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
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
                        Text("In Transit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Items Preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(2) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Slate50)
                                .border(1.dp, Slate100, RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.Inventory2, null, modifier = Modifier.align(Alignment.Center).size(24.dp), tint = Slate300)
                        }
                    }
                }
                Column {
                    Text("2 Items", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Slate700)
                    Text("KSh 11,460 Total", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Brand600)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = Slate100)

            TrackingTimeline()

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Carrier Link", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(11.dp))
                    }
                }
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, Slate200, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.ReportProblem, null, modifier = Modifier.size(18.dp), tint = Slate600)
                }
            }
        }
    }
}

@Composable
fun TrackingTimeline() {
    val steps = listOf(
        TrackingStep("Order Placed", "Oct 12, 09:41 AM", true, false, Icons.Default.Check),
        TrackingStep("Shipped", "Oct 14, 02:15 PM • Nairobi Hub", true, true, Icons.Default.LocalShipping),
        TrackingStep("Delivered", "Estimated Oct 17", false, false, Icons.Default.Home)
    )

    Column(modifier = Modifier.padding(start = 11.dp)) {
        steps.forEachIndexed { index, step ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (step.isCompleted) Brand500 else Color.White)
                            .border(2.dp, if (step.isCompleted) Color.White else Slate200, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            step.icon,
                            contentDescription = null,
                            modifier = Modifier.size(10.dp),
                            tint = if (step.isCompleted) Color.White else Slate300
                        )
                    }
                    if (index < steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(30.dp)
                                .background(if (step.isActive) Brand500 else Slate100)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.padding(bottom = if (index < steps.size - 1) 20.dp else 0.dp)) {
                    Text(
                        text = step.title,
                        fontSize = 13.sp,
                        fontWeight = if (step.isCompleted) FontWeight.Bold else FontWeight.Medium,
                        color = if (step.isCompleted) Slate900 else Slate400
                    )
                    Text(
                        text = step.subtitle,
                        fontSize = 11.sp,
                        color = if (step.isCompleted) Slate500 else Slate400
                    )
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
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp),
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
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Brand600
                )
            }
        }
    }
}

@Composable
fun UpdatesList() {
    Surface(
        modifier = Modifier.padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            UpdateItem(
                UpdateNotification(
                    "Package out for delivery",
                    "Your scrubs will arrive today between 2 PM and 5 PM.",
                    "Just now",
                    true,
                    Icons.Default.MoveToInbox,
                    Brand500,
                    Brand50
                )
            )
            UpdateItem(
                UpdateNotification(
                    "15% Off Your Next Reorder",
                    "Use code NURSE15 at checkout valid for 7 days.",
                    "Yesterday",
                    false,
                    Icons.Default.LocalOffer,
                    Color(0xFFF43F5E),
                    Color(0xFFFFF1F2)
                )
            )
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
fun PastOrderCard() {
    Surface(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
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
                    Text("Order #NW-7210", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    Text("Delivered", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate400)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Sep 28, 2023 • 3 Items", fontSize = 12.sp, color = Slate500)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("View Receipt", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Brand600)
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
