package com.example.nursewearconnect.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nursewearconnect.ui.theme.*
import com.example.nursewearconnect.ui.viewmodel.HomeViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorAnalyticsScreen(
    onBackClick: () -> Unit,
    viewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales Analytics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Slate50
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SalesSummaryCards(uiState.vendorOrders)
            }
            
            item {
                Text(
                    "Revenue Trend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(Modifier.height(12.dp))
                RevenueChart()
            }
            
            item {
                Text(
                    "Top Selling Products",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            }
            
            items(uiState.vendorProducts.take(3)) { product ->
                TopProductItem(product.name, (10..50).random(), (5000..25000).random())
            }
            
            item {
                Text(
                    "Recent Orders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            }
            
            items(uiState.vendorOrders.take(5)) { order ->
                RecentOrderCard(order)
            }
        }
    }
}

@Composable
fun SalesSummaryCards(orders: List<Map<String, Any>>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val totalRevenue = orders.sumOf { (it["total_amount"] as? Number)?.toDouble() ?: 0.0 }
        val totalOrders = orders.size
        
        AnalyticsCard(
            label = "Total Revenue",
            value = "KSh ${String.format(Locale.US, "%,.0f", totalRevenue)}",
            trend = "+12.5%",
            modifier = Modifier.weight(1f)
        )
        AnalyticsCard(
            label = "Total Orders",
            value = totalOrders.toString(),
            trend = "+5.2%",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AnalyticsCard(label: String, value: String, trend: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, fontSize = 12.sp, color = Slate500)
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(trend, fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun RevenueChart() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Canvas(modifier = Modifier.padding(24.dp)) {
            val path = Path()
            val points = listOf(0.2f, 0.5f, 0.4f, 0.8f, 0.7f, 0.9f, 1.0f)
            val width = size.width
            val height = size.height
            
            path.moveTo(0f, height * (1 - points[0]))
            points.forEachIndexed { index, point ->
                if (index > 0) {
                    val x = (width / (points.size - 1)) * index
                    val y = height * (1 - point)
                    path.lineTo(x, y)
                }
            }
            
            drawPath(
                path = path,
                color = Brand600,
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}

@Composable
fun TopProductItem(name: String, sold: Int, revenue: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(name, fontWeight = FontWeight.Medium, color = Slate900)
                Text("$sold units sold", fontSize = 12.sp, color = Slate500)
            }
            Text("KSh $revenue", fontWeight = FontWeight.Bold, color = Brand600)
        }
    }
}

@Composable
fun RecentOrderCard(order: Map<String, Any>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Brand50, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("#${order["id"].toString().takeLast(3)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Brand600)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Order ${order["id"].toString().take(8)}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(order["status"].toString(), fontSize = 12.sp, color = Slate400)
            }
            Text("KSh ${order["total_amount"]}", fontWeight = FontWeight.Bold)
        }
    }
}
