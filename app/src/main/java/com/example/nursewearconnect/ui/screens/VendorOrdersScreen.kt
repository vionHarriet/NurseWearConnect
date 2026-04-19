package com.example.nursewearconnect.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nursewearconnect.ui.theme.*
import com.example.nursewearconnect.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorOrdersScreen(
    onBackClick: () -> Unit,
    viewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("New", "Processing", "Completed")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Orders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Slate900
                )
            )
        },
        containerColor = Slate50
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Brand600,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Brand600
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 14.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val status = tabs[selectedTab].lowercase()
                
                // Map the List<Map<String, Any>> from uiState to VendorOrder objects
                val orders = uiState.vendorOrders.map { map ->
                    val profiles = map["profiles"] as? Map<*, *>
                    val customerName = profiles?.get("full_name")?.toString() ?: "Unknown Customer"
                    
                    VendorOrder(
                        id = map["id"]?.toString()?.take(8) ?: "",
                        customerName = customerName,
                        itemName = map["itemName"]?.toString() ?: "Product Bundle",
                        quantity = (map["quantity"] as? Number)?.toInt() ?: 1,
                        size = map["size"]?.toString() ?: "N/A",
                        total = (map["total_amount"] as? Number)?.toInt() ?: 0,
                        date = map["created_at"]?.toString()?.split("T")?.get(0) ?: "Recently",
                        status = map["status"]?.toString() ?: "Pending"
                    )
                }

                items(orders.filter { 
                    it.status.lowercase() == status || (status == "new" && it.status == "Pending") 
                }) { order ->
                    VendorOrderCard(
                        order = order,
                        onAccept = { viewModel.updateVendorOrderStatus(order.id, "Processing") },
                        onComplete = { viewModel.updateVendorOrderStatus(order.id, "Completed") }
                    )
                }
            }
        }
    }
}

@Composable
fun VendorOrderCard(
    order: VendorOrder,
    onAccept: () -> Unit,
    onComplete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Order #${order.id}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                    Text(order.date, fontSize = 12.sp, color = Slate500)
                }
                Surface(
                    color = when(order.status) {
                        "Pending" -> Color(0xFFFEF3C7)
                        "Processing" -> Brand50
                        else -> Color(0xFFECFDF5)
                    },
                    shape = RoundedCornerShape(99.dp)
                ) {
                    Text(
                        order.status,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when(order.status) {
                            "Pending" -> Color(0xFFD97706)
                            "Processing" -> Brand600
                            else -> Color(0xFF059669)
                        }
                    )
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate100)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(Slate50, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Inventory2, null, tint = Brand600)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(order.itemName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                    Text("Qty: ${order.quantity} • Size: ${order.size}", fontSize = 12.sp, color = Slate500)
                }
                Text("KSh ${order.total}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = Slate400, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(order.customerName, fontSize = 13.sp, color = Slate700)
            }
            
            if (order.status != "Completed") {
                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (order.status == "Pending") {
                        Button(
                            onClick = onAccept,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Brand600)
                        ) {
                            Text("Accept Order")
                        }
                    } else if (order.status == "Processing") {
                        Button(
                            onClick = onComplete,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("Mark as Completed")
                        }
                    }
                }
            }
        }
    }
}

data class VendorOrder(
    val id: String,
    val customerName: String,
    val itemName: String,
    val quantity: Int,
    val size: String,
    val total: Int,
    val date: String,
    val status: String
)
