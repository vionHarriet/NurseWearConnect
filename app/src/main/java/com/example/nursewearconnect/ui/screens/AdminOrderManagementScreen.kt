package com.example.nursewearconnect.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
fun AdminOrderManagementScreen(
    onBackClick: () -> Unit,
    viewModel: HomeViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAdminData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Global Orders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search by Order ID or Vendor...") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Slate400) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brand600,
                        unfocusedBorderColor = Slate200
                    ),
                    singleLine = true
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val filteredOrders = uiState.allOrders.filter {
                    val id = it["id"]?.toString() ?: ""
                    val vendor = it["vendor_name"]?.toString() ?: ""
                    id.contains(searchQuery, ignoreCase = true) || vendor.contains(searchQuery, ignoreCase = true)
                }

                items(filteredOrders) { orderMap ->
                    val profiles = orderMap["profiles"] as? Map<*, *>
                    val customerName = profiles?.get("full_name")?.toString() ?: "Unknown"

                    val order = AdminOrderItem(
                        id = orderMap["id"]?.toString()?.take(8) ?: "",
                        vendorName = "System", // Global view
                        customerName = customerName,
                        amount = (orderMap["total_amount"] as? Number)?.toInt() ?: 0,
                        status = orderMap["status"]?.toString() ?: "Pending",
                        date = orderMap["created_at"]?.toString()?.split("T")?.get(0) ?: "",
                        itemsCount = 1 // Simplified for overview
                    )
                    AdminOrderCard(order)
                }
            }
        }
    }
}

@Composable
fun AdminOrderCard(order: AdminOrderItem) {
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
                    Text("Order #${order.id}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                    Text(order.date, fontSize = 12.sp, color = Slate500)
                }
                Surface(
                    color = when(order.status) {
                        "Pending" -> Color(0xFFFEF3C7)
                        "Shipped" -> Brand50
                        "Delivered" -> Color(0xFFECFDF5)
                        else -> Color(0xFFFEE2E2)
                    },
                    shape = RoundedCornerShape(99.dp)
                ) {
                    Text(
                        order.status,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when(order.status) {
                            "Pending" -> Color(0xFFD97706)
                            "Shipped" -> Brand600
                            "Delivered" -> Color(0xFF059669)
                            else -> Color(0xFFDC2626)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Store, null, modifier = Modifier.size(14.dp), tint = Slate400)
                Spacer(Modifier.width(6.dp))
                Text("Vendor: ${order.vendorName}", fontSize = 13.sp, color = Slate700)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = Slate400)
                Spacer(Modifier.width(6.dp))
                Text("Customer: ${order.customerName}", fontSize = 13.sp, color = Slate700)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate50)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${order.itemsCount} Items", fontSize = 13.sp, color = Slate500)
                Text("KSh ${order.amount}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Brand600)
            }
        }
    }
}

data class AdminOrderItem(
    val id: String,
    val vendorName: String,
    val customerName: String,
    val amount: Int,
    val status: String,
    val date: String,
    val itemsCount: Int
)
