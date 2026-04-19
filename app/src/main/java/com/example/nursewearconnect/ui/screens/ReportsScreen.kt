package com.example.nursewearconnect.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AssignmentReturn
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nursewearconnect.ui.theme.*
import com.example.nursewearconnect.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    innerPadding: PaddingValues,
    viewModel: HomeViewModel,
    onNavigateToInventory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Dynamic calculations
    val totalRevenue = uiState.allOrders.sumOf { (it["totalAmount"] as? Number)?.toDouble() ?: 0.0 }
    val totalOrders = uiState.allOrders.size
    val activeUsers = 3150 // Still static unless we have a UserRepository.getAllUsers()
    val returnRate = "2%" // Placeholder for return logic

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(top = innerPadding.calculateTopPadding()),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Business Insights",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = Slate900
            )
            Text("Overview of platform performance", fontSize = 14.sp, color = Slate500)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Revenue",
                    value = "KSh ${String.format("%.1fk", totalRevenue / 1000)}",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = Brand600,
                    trend = "+12%"
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Orders",
                    value = totalOrders.toString(),
                    icon = Icons.Default.ShoppingCart,
                    color = Color(0xFF8B5CF6),
                    trend = "+8%"
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Returns",
                    value = "14", // Placeholder
                    icon = Icons.AutoMirrored.Filled.AssignmentReturn,
                    color = Color(0xFFF43F5E),
                    trend = "-2%"
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Active Users",
                    value = activeUsers.toString(),
                    icon = Icons.Default.People,
                    color = Color(0xFF10B981),
                    trend = "+15%"
                )
            }
        }

        item {
            PerformanceChartCard()
        }

        item {
            Button(
                onClick = onNavigateToInventory,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Brand600),
                border = BorderStroke(1.dp, Brand100)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Manage Global Inventory", fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    trend: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = color.copy(alpha = 0.1f)
                ) {
                    Icon(
                        icon,
                        null,
                        modifier = Modifier.padding(8.dp).size(20.dp),
                        tint = color
                    )
                }
                Text(
                    trend,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (trend.startsWith("+")) Color(0xFF10B981) else Color(0xFFF43F5E)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontSize = 12.sp, color = Slate500)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
        }
    }
}

@Composable
fun PerformanceChartCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Revenue Trend", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
            Spacer(modifier = Modifier.height(24.dp))
            
            // Dummy Chart Visualization
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Slate50, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.BarChart, null, modifier = Modifier.size(48.dp), tint = Slate300)
                    Text("Chart Data Loading...", fontSize = 12.sp, color = Slate400)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                    Text(day, fontSize = 11.sp, color = Slate400)
                }
            }
        }
    }
}
