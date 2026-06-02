package com.example.nursewearconnect.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.Canvas
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import com.example.nursewearconnect.ui.components.OrderTimelineView
import com.example.nursewearconnect.ui.theme.*
import com.example.nursewearconnect.ui.viewmodel.HomeUiState
import com.example.nursewearconnect.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderManagementScreen(
    onBackClick: (() -> Unit)? = null,
    viewModel: HomeViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    var statusFilter by remember { mutableStateOf("All") }
    
    // Date Filtering State
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDateRangePickerState()
    val dateFilterLabel = remember(datePickerState.selectedStartDateMillis, datePickerState.selectedEndDateMillis) {
        if (datePickerState.selectedStartDateMillis != null && datePickerState.selectedEndDateMillis != null) {
            val sdf = java.text.SimpleDateFormat("MMM dd", java.util.Locale.US)
            val start = sdf.format(java.util.Date(datePickerState.selectedStartDateMillis!!))
            val end = sdf.format(java.util.Date(datePickerState.selectedEndDateMillis!!))
            "$start - $end"
        } else {
            "All Time"
        }
    }

    val tabs = listOf("Orders", "Payouts", "Financials", "Logs")
    
    val uiState by viewModel.uiState.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    
    var selectedOrderForStatus by remember { mutableStateOf<AdminOrderItem?>(null) }
    var selectedOrderForDetail by remember { mutableStateOf<Map<String, Any>?>(null) }
    var selectedPayoutForUpdate by remember { mutableStateOf<Map<String, Any>?>(null) }
    var showCreatePayout by remember { mutableStateOf(false) }

    // Update data when filters change (Server-side)
    LaunchedEffect(statusFilter, datePickerState.selectedStartDateMillis, datePickerState.selectedEndDateMillis, searchQuery) {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val startDate = datePickerState.selectedStartDateMillis?.let { sdf.format(java.util.Date(it)) }
        val endDate = datePickerState.selectedEndDateMillis?.let { sdf.format(java.util.Date(it)) }
        
        viewModel.fetchAdminOrders(
            status = if (statusFilter == "All") null else statusFilter,
            startDate = startDate,
            endDate = endDate,
            searchQuery = if (searchQuery.length >= 3) searchQuery else null,
            page = 0,
            append = false
        )
    }

    // Dialogs
    if (showCreatePayout) {
        val vendors = allUsers.filter { (it["role"] as? String)?.lowercase() == "vendor" }
            .map { it["id"].toString() to (it["full_name"]?.toString() ?: "Unknown Vendor") }
        
        CreatePayoutDialog(
            vendors = vendors,
            onDismiss = { showCreatePayout = false },
            onConfirm = { vendorId, amount ->
                viewModel.createPayout(vendorId, amount)
                showCreatePayout = false
            }
        )
    }

    if (selectedPayoutForUpdate != null) {
        UpdatePayoutDialog(
            payout = selectedPayoutForUpdate!!,
            onDismiss = { selectedPayoutForUpdate = null },
            onUpdate = { id, status, ref ->
                viewModel.updatePayoutStatus(id, status, ref)
            }
        )
    }

    if (selectedOrderForDetail != null) {
        AdminOrderDetailDialog(
            orderMap = selectedOrderForDetail!!,
            allUsers = allUsers,
            onDismiss = { selectedOrderForDetail = null }
        )
    }

    if (selectedOrderForStatus != null) {
        UpdateOrderStatusDialog(
            order = selectedOrderForStatus!!,
            onDismiss = { selectedOrderForStatus = null },
            onUpdate = { id, status ->
                viewModel.updateVendorOrderStatus(id, status)
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    datePickerState.setSelection(null, null)
                    showDatePicker = false
                }) {
                    Text("Clear")
                }
            }
        ) {
            DateRangePicker(
                state = datePickerState,
                modifier = Modifier.height(450.dp),
                title = { Text("Filter by Date", modifier = Modifier.padding(16.dp)) },
                showModeToggle = false
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Admin Hub", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    onBackClick?.let { back ->
                        IconButton(onClick = back) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    when (selectedTab) {
                        0, 2, 3 -> {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    Icons.Default.DateRange, 
                                    contentDescription = "Date Filter", 
                                    tint = if (datePickerState.selectedStartDateMillis != null) Brand600 else Slate600
                                )
                            }
                        }
                        1 -> {
                            IconButton(onClick = { viewModel.scheduleAutomatedPayouts() }) {
                                Icon(Icons.Default.Autorenew, contentDescription = "Schedule Payouts", tint = Brand600)
                            }
                            IconButton(onClick = { showCreatePayout = true }) {
                                Icon(Icons.Default.AddCircle, contentDescription = "New Payout", tint = Brand600)
                            }
                        }
                    }
                    if (selectedTab == 3) {
                        IconButton(onClick = {
                            val csvData = viewModel.exportLogsToCSV()
                            if (csvData.isNotEmpty()) {
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "System Logs Export")
                                    putExtra(android.content.Intent.EXTRA_TEXT, csvData)
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "Share Logs"))
                            }
                        }) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Export Logs", tint = Slate600)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Slate50
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Tab Header
            Surface(color = Color.White, shadowElevation = 1.dp) {
                Column {
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
                                text = { Text(title, fontSize = 12.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium) }
                            )
                        }
                    }
                    
                    if (selectedTab == 0 || selectedTab == 1 || selectedTab == 3) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { 
                                    Text(when(selectedTab) {
                                        0 -> "Search Orders (ID, Customer)..."
                                        1 -> "Search Payouts (Vendor)..."
                                        else -> "Search Logs..."
                                    })
                                },
                                leadingIcon = { Icon(Icons.Default.Search, null, tint = Slate400) },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            
                            if (selectedTab == 0) {
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    LazyRow(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val filters = listOf("All", "Pending", "Processing", "Shipped", "Delivered", "Cancelled")
                                        items(filters) { filter ->
                                            FilterChip(
                                                selected = statusFilter == filter,
                                                onClick = { statusFilter = filter },
                                                label = { Text(filter) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Brand100,
                                                    selectedLabelColor = Brand700
                                                )
                                            )
                                        }
                                    }
                                    
                                    if (datePickerState.selectedStartDateMillis != null) {
                                        Surface(
                                            color = Brand50,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.padding(start = 8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(dateFilterLabel, fontSize = 10.sp, color = Brand700, fontWeight = FontWeight.Bold)
                                                Icon(
                                                    Icons.Default.Close, 
                                                    null, 
                                                    modifier = Modifier.size(12.dp).clickable { datePickerState.setSelection(null, null) },
                                                    tint = Brand700
                                                )
                                            }
                                        }
                                    }
                                }
                            } else if (datePickerState.selectedStartDateMillis != null) {
                                Surface(
                                    color = Brand50,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.DateRange, null, modifier = Modifier.size(12.dp), tint = Brand700)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Date Range: $dateFilterLabel", fontSize = 12.sp, color = Brand700, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.width(8.dp))
                                        Icon(
                                            Icons.Default.Close, 
                                            null, 
                                            modifier = Modifier.size(14.dp).clickable { datePickerState.setSelection(null, null) },
                                            tint = Brand700
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            when (selectedTab) {
                0 -> OrderList(
                    viewModel,
                    uiState, 
                    allUsers, 
                    searchQuery, 
                    statusFilter, 
                    dateRange = datePickerState.selectedStartDateMillis to datePickerState.selectedEndDateMillis,
                    onStatusClick = { selectedOrderForStatus = it }, 
                    onClick = { selectedOrderForDetail = it }
                )
                1 -> PayoutList(uiState, allUsers, searchQuery, onUpdateClick = { selectedPayoutForUpdate = it })
                2 -> FinancialOverview(uiState, allUsers, dateRange = datePickerState.selectedStartDateMillis to datePickerState.selectedEndDateMillis)
                3 -> SystemLogList(
                    viewModel,
                    uiState, 
                    searchQuery, 
                    dateRange = datePickerState.selectedStartDateMillis to datePickerState.selectedEndDateMillis,
                    onClearLogs = { viewModel.clearSystemLogs() }
                )
            }
        }
    }
}

@Composable
fun OrderList(
    viewModel: HomeViewModel,
    uiState: HomeUiState,
    allUsers: List<Map<String, Any>>,
    searchQuery: String,
    statusFilter: String,
    dateRange: Pair<Long?, Long?>,
    onStatusClick: (AdminOrderItem) -> Unit,
    onClick: (Map<String, Any>) -> Unit
) {
    val filteredOrders = uiState.adminFilteredOrders

    if (filteredOrders.isEmpty()) {
        if (uiState.isAdminOrdersLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Brand600)
            }
        } else {
            EmptyState(
                icon = Icons.Default.Inventory,
                title = "No Orders Found",
                subtitle = if (searchQuery.isNotEmpty() || statusFilter != "All") "Try adjusting your filters" else "No orders have been placed yet."
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredOrders) { orderMap ->
                val profiles = orderMap["profiles"] as? Map<*, *>
                val customerName = profiles?.get("full_name")?.toString() ?: "Unknown"
                val orderItems = orderMap["order_items"] as? List<Map<String, Any>>
                val itemsCount = orderItems?.size ?: 0
                
                // Group by vendor and resolve names
                val vendorIds = orderItems?.mapNotNull { (it["products"] as? Map<*, *>)?.get("vendor_id")?.toString() }?.distinct() ?: emptyList()
                val vendorDisplay = if (vendorIds.size > 1) {
                    "Multiple Vendors (${vendorIds.size})"
                } else if (vendorIds.isNotEmpty()) {
                    val vId = vendorIds.first()
                    allUsers.find { it["id"] == vId }?.get("full_name")?.toString() ?: "Vendor ($vId)"
                } else {
                    "System"
                }

                val order = AdminOrderItem(
                    id = orderMap["id"]?.toString()?.take(8) ?: "",
                    fullId = orderMap["id"]?.toString() ?: "",
                    vendorName = vendorDisplay,
                    customerName = customerName,
                    amount = (orderMap["total_amount"] as? Number)?.toInt() ?: 0,
                    status = orderMap["status"]?.toString() ?: "Pending",
                    date = orderMap["created_at"]?.toString()?.split("T")?.get(0) ?: "",
                    itemsCount = itemsCount
                )
                AdminOrderCard(order = order, onStatusClick = { onStatusClick(order) }, onClick = { onClick(orderMap) })
            }
            
            if (uiState.adminOrdersHasMore) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        if (uiState.isAdminOrdersLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Brand600)
                        } else {
                            TextButton(onClick = {
                                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                                val startDate = dateRange.first?.let { sdf.format(java.util.Date(it)) }
                                val endDate = dateRange.second?.let { sdf.format(java.util.Date(it)) }
                                viewModel.fetchAdminOrders(
                                    status = if (statusFilter == "All") null else statusFilter,
                                    startDate = startDate,
                                    endDate = endDate,
                                    searchQuery = if (searchQuery.length >= 3) searchQuery else null,
                                    page = uiState.adminOrdersPage + 1,
                                    append = true
                                )
                            }) {
                                Text("Load More", color = Brand600)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PayoutList(uiState: HomeUiState, allUsers: List<Map<String, Any>>, searchQuery: String, onUpdateClick: (Map<String, Any>) -> Unit) {
    val filteredPayouts = uiState.payouts.filter { payout ->
        val profiles = payout["profiles"] as? Map<*, *>
        val vendorId = payout["vendor_id"]?.toString()
        val vendorName = profiles?.get("full_name")?.toString() ?: 
                        allUsers.find { it["id"] == vendorId }?.get("full_name")?.toString() ?: ""
        
        vendorName.contains(searchQuery, ignoreCase = true)
    }

    if (filteredPayouts.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Payments,
            title = "No Payouts Found",
            subtitle = if (searchQuery.isNotEmpty()) "No payouts matching '$searchQuery'" else "No payout history available."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredPayouts) { payout ->
                PayoutCard(payout = payout, allUsers = allUsers, onUpdateClick = { onUpdateClick(payout) })
            }
        }
    }
}

@Composable
fun FinancialOverview(uiState: HomeUiState, allUsers: List<Map<String, Any>>, dateRange: Pair<Long?, Long?>) {
    // Dynamic Commission Calculation based on vendor profile rates
    var platformRevenue = 0.0
    var totalGrossVolume = 0.0
    var readyForPayout = 0.0
    
    val filteredOrders = uiState.allOrders.filter { 
        com.example.nursewearconnect.utils.AppUtils.isDateInRange(it["created_at"]?.toString(), dateRange.first, dateRange.second)
    }

    // Prepare data for the chart
    val dailyRevenue = mutableMapOf<String, Double>()
    val dailyCommission = mutableMapOf<String, Double>()

    filteredOrders.forEach { order ->
        val date = order["created_at"]?.toString()?.split("T")?.firstOrNull() ?: "Other"
        val amount = (order["total_amount"] as? Number)?.toDouble() ?: 0.0
        val status = order["status"]?.toString() ?: "Pending"
        totalGrossVolume += amount
        
        dailyRevenue[date] = (dailyRevenue[date] ?: 0.0) + amount
        
        val orderItems = order["order_items"] as? List<Map<String, Any>> ?: emptyList()
        orderItems.forEach { item ->
            val itemPrice = (item["price_at_purchase"] as? Number)?.toDouble() ?: 0.0
            val qty = (item["quantity"] as? Number)?.toInt() ?: 1
            val itemTotal = itemPrice * qty
            
            val vendorId = (item["products"] as? Map<*, *>)?.get("vendor_id")?.toString()
            val vendorProfile = allUsers.find { it["id"] == vendorId }
            val commissionRate = (vendorProfile?.get("commission_rate") as? Number)?.toDouble() ?: 10.0
            
            val itemCommission = itemTotal * (commissionRate / 100.0)
            platformRevenue += itemCommission
            dailyCommission[date] = (dailyCommission[date] ?: 0.0) + itemCommission
            
            if (status.lowercase() == "delivered") {
                readyForPayout += (itemTotal - itemCommission)
            }
        }
    }

    val paidPayouts = uiState.payouts.filter { (it["status"] as? String) == "paid" }.sumOf { (it["amount"] as? Number)?.toDouble() ?: 0.0 }
    val pendingPayouts = uiState.payouts.filter { (it["status"] as? String) == "pending" }.sumOf { (it["amount"] as? Number)?.toDouble() ?: 0.0 }
    val netPlatformProfit = platformRevenue

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Revenue & Commission Trends", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Slate900)
            Spacer(Modifier.height(12.dp))
            RevenueTrendsChart(dailyRevenue, dailyCommission)
        }

        item {
            Text("Financial Ledger", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Slate900)
            Spacer(Modifier.height(8.dp))
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Slate100)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FinancialRow("Gross Sales", "KSh ${String.format("%,.0f", totalGrossVolume)}", Slate900)
                    FinancialRow("Platform Commission", "- KSh ${String.format("%,.0f", platformRevenue)}", Color(0xFFF43F5E))
                    HorizontalDivider(color = Slate50)
                    FinancialRow("Vendor Net Earnings", "KSh ${String.format("%,.0f", totalGrossVolume - platformRevenue)}", Brand700)
                    FinancialRow("Total Disbursed", "- KSh ${String.format("%,.0f", paidPayouts)}", Slate500)
                    FinancialRow("Pending Disbursal", "- KSh ${String.format("%,.0f", pendingPayouts)}", Color(0xFFD97706))
                    HorizontalDivider(color = Slate100, thickness = 2.dp)
                    FinancialRow("Net Platform Profit", "KSh ${String.format("%,.0f", netPlatformProfit)}", Color(0xFF10B981))
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Ready for Payout",
                    value = "KSh ${String.format("%,.0f", (readyForPayout - paidPayouts - pendingPayouts).coerceAtLeast(0.0))}",
                    icon = Icons.Default.AccountBalanceWallet,
                    color = Brand600,
                    trend = "Unsettled"
                )
                AdminStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Avg. Order",
                    value = "KSh ${if (filteredOrders.isNotEmpty()) String.format("%,.0f", totalGrossVolume / filteredOrders.size) else "0"}",
                    icon = Icons.Default.Analytics,
                    color = Color(0xFF8B5CF6),
                    trend = "In Selected Period"
                )
            }
        }
    }
}

@Composable
fun RevenueTrendsChart(revenueData: Map<String, Double>, commissionData: Map<String, Double>) {
    val sortedDates = revenueData.keys.sorted()
    val revenuePoints = sortedDates.map { revenueData[it] ?: 0.0 }
    val commissionPoints = sortedDates.map { commissionData[it] ?: 0.0 }
    
    val maxVal = (revenuePoints.maxOrNull() ?: 1.0).coerceAtLeast(1.0)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100)
    ) {
        if (sortedDates.isEmpty()) {
            Box(contentAlignment = Alignment.Center) {
                Text("Not enough data for trend analysis", color = Slate400, fontSize = 12.sp)
            }
        } else {
            Canvas(modifier = Modifier.padding(24.dp).fillMaxSize()) {
                val width = size.width
                val height = size.height
                val spacing = width / (if (sortedDates.size > 1) sortedDates.size - 1 else 1)

                // Draw Revenue Path
                val revenuePath = Path().apply {
                    revenuePoints.forEachIndexed { index, value ->
                        val x = index * spacing
                        val y = height - (value.toFloat() / maxVal.toFloat() * height)
                        if (index == 0) moveTo(x, y) else lineTo(x, y)
                    }
                }
                drawPath(revenuePath, color = Brand600, style = Stroke(width = 3.dp.toPx()))

                // Draw Commission Path
                val commissionPath = Path().apply {
                    commissionPoints.forEachIndexed { index, value ->
                        val x = index * spacing
                        val y = height - (value.toFloat() / maxVal.toFloat() * height)
                        if (index == 0) moveTo(x, y) else lineTo(x, y)
                    }
                }
                drawPath(commissionPath, color = Color(0xFFF43F5E), style = Stroke(width = 2.dp.toPx()))
            }
        }
    }
}

@Composable
fun SystemLogList(
    viewModel: HomeViewModel,
    uiState: HomeUiState,
    searchQuery: String,
    dateRange: Pair<Long?, Long?>,
    onClearLogs: () -> Unit
) {
    // We'll use the server-side fetched logs from uiState.systemLogs
    // For now, let's keep the existing filtering but also use the pagination if available
    val filteredLogs = uiState.systemLogs.filter { log ->
        val action = log["action"]?.toString() ?: ""
        val details = log["details"]?.toString() ?: ""
        val createdAt = log["created_at"]?.toString()
        
        val matchesSearch = action.contains(searchQuery, ignoreCase = true) || details.contains(searchQuery, ignoreCase = true)
        val matchesDate = com.example.nursewearconnect.utils.AppUtils.isDateInRange(createdAt, dateRange.first, dateRange.second)
        
        matchesSearch && matchesDate
    }.reversed()

    // Trigger server-side fetch for logs when filters change
    LaunchedEffect(searchQuery, dateRange) {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val startDate = dateRange.first?.let { sdf.format(java.util.Date(it)) }
        val endDate = dateRange.second?.let { sdf.format(java.util.Date(it)) }
        viewModel.fetchSystemLogs(startDate, endDate, page = 0, append = false)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${filteredLogs.size} Audit Entries", fontWeight = FontWeight.Bold, color = Slate700)
            TextButton(onClick = onClearLogs, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFF43F5E))) {
                Icon(Icons.Default.DeleteSweep, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Clear All")
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredLogs) { log ->
                LogItemCard(log)
            }
            
            // Add Load More for Logs if implemented in ViewModel/State
            // For now, let's assume it's just one page or handled similarly to orders
        }
    }
}

@Composable
fun LogItemCard(log: Map<String, Any>) {
    val action = log["action"]?.toString() ?: "UNKNOWN"
    val details = log["details"]?.toString() ?: ""
    val date = log["created_at"]?.toString()?.split("T")?.firstOrNull() ?: ""
    val time = log["created_at"]?.toString()?.split("T")?.get(1)?.take(5) ?: ""
    val severity = log["severity"]?.toString() ?: "info"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(8.dp).background(
                    when(severity) {
                        "error" -> Color(0xFFF43F5E)
                        "warning" -> Color(0xFFF59E0B)
                        else -> Color(0xFF10B981)
                    }, CircleShape
                )
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(action, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                Text(details, fontSize = 12.sp, color = Slate500)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(date, fontSize = 10.sp, color = Slate400)
                Text(time, fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminStatCard(
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
                    Icon(icon, null, modifier = Modifier.padding(8.dp).size(20.dp), tint = color)
                }
                if (trend.isNotEmpty()) {
                    Text(trend, color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(title, color = Slate500, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(value, color = Slate900, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FinancialRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Slate500, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
    }
}

@Composable
fun AdminOrderDetailDialog(orderMap: Map<String, Any>, allUsers: List<Map<String, Any>>, onDismiss: () -> Unit) {
    val profiles = orderMap["profiles"] as? Map<*, *>
    val customerName = profiles?.get("full_name")?.toString() ?: "Unknown"
    val orderItems = orderMap["order_items"] as? List<Map<String, Any>> ?: emptyList()

    val context = androidx.compose.ui.platform.LocalContext.current
    val customerPhone = profiles?.get("phone_number")?.toString() ?: ""
    val customerId = orderMap["user_id"]?.toString() ?: ""

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Order #${orderMap["id"]?.toString()?.take(8)}")
                Row {
                    if (customerPhone.isNotEmpty()) {
                        IconButton(onClick = { 
                            val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:$customerPhone") }
                            context.startActivity(intent)
                        }) { Icon(Icons.Default.Phone, null, tint = Brand600) }
                    }
                    IconButton(onClick = { 
                        // Note: Internal messaging logic would go here
                        // For now, open SMS
                        if (customerPhone.isNotEmpty()) {
                            val intent = Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("smsto:$customerPhone") }
                            context.startActivity(intent)
                        }
                    }) { Icon(Icons.Default.Message, null, tint = Brand600) }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Text("Customer: $customerName", fontWeight = FontWeight.Bold, color = Slate900)
                Text("Status: ${orderMap["status"]}", color = Brand600, fontWeight = FontWeight.SemiBold)
                
                OrderTimelineView(status = orderMap["status"]?.toString() ?: "Pending")

                HorizontalDivider(color = Slate100)
                
                Text("Order Breakdown (by Vendor)", fontWeight = FontWeight.SemiBold, color = Slate700)
                
                val groupedItems = orderItems.groupBy { (it["products"] as? Map<*, *>)?.get("vendor_id")?.toString() ?: "System" }
                
                groupedItems.forEach { (vendorId, items) ->
                    val vendorName = allUsers.find { it["id"] == vendorId }?.get("full_name")?.toString() ?: "Vendor $vendorId"
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        color = Slate50,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(vendorName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate600)
                            items.forEach { item ->
                                val products = item["products"] as? Map<String, Any>
                                val name = products?.get("name")?.toString() ?: "Unknown Product"
                                val qty = (item["quantity"] as? Number)?.toInt() ?: 1
                                val price = (item["price_at_purchase"] as? Number)?.toDouble() ?: 0.0
                                
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(name, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                    Text("x$qty KSh ${String.format("%,.0f", price * qty)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun UpdateOrderStatusDialog(order: AdminOrderItem, onDismiss: () -> Unit, onUpdate: (String, String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Order Status") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select new status for Order #${order.id}")
                val statuses = listOf("Pending", "Processing", "Shipped", "Delivered", "Cancelled")
                statuses.forEach { status ->
                    val isSelected = order.status.equals(status, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onUpdate(order.fullId, status)
                                onDismiss()
                            },
                        color = if (isSelected) Brand50 else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSelected, onClick = null)
                            Spacer(Modifier.width(8.dp))
                            Text(status, color = if (isSelected) Brand600 else Slate700)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun UpdatePayoutDialog(payout: Map<String, Any>, onDismiss: () -> Unit, onUpdate: (String, String, String) -> Unit) {
    var refNumber by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Payout Status") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Payout to: ${(payout["profiles"] as? Map<*, *>)?.get("full_name")}")
                Text("Amount: KSh ${payout["amount"]}", fontWeight = FontWeight.Bold)
                
                OutlinedTextField(
                    value = refNumber,
                    onValueChange = { refNumber = it },
                    label = { Text("M-Pesa Reference (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            onUpdate(payout["id"].toString(), "paid", refNumber)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Mark Paid")
                    }
                    OutlinedButton(
                        onClick = {
                            onUpdate(payout["id"].toString(), "failed", refNumber)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF43F5E))
                    ) {
                        Text("Mark Failed")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CreatePayoutDialog(vendors: List<Pair<String, String>>, onDismiss: () -> Unit, onConfirm: (String, Int) -> Unit) {
    var selectedVendorId by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Initiate Vendor Payout") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Select vendor and specify amount to disburse.")
                
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(vendors) { (id, name) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { selectedVendorId = id }.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedVendorId == id, onClick = null)
                            Text(name, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (KSh)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (selectedVendorId.isNotEmpty() && amount.isNotEmpty()) onConfirm(selectedVendorId, amount.toInt()) },
                enabled = selectedVendorId.isNotEmpty() && amount.isNotEmpty()
            ) {
                Text("Confirm Payout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminOrderCard(order: AdminOrderItem, onStatusClick: () -> Unit, onClick: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Order #${order.id}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                        IconButton(
                            onClick = { clipboardManager.setText(AnnotatedString(order.fullId)) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, "Copy ID", modifier = Modifier.size(14.dp), tint = Slate400)
                        }
                    }
                    Text(order.date, fontSize = 12.sp, color = Slate500)
                }
                Surface(
                    onClick = onStatusClick,
                    color = when(order.status) {
                        "Pending" -> Color(0xFFFEF3C7)
                        "Shipped" -> Brand50
                        "Delivered" -> Color(0xFFECFDF5)
                        "Processing" -> Color(0xFFE0F2FE)
                        else -> Color(0xFFFEE2E2)
                    },
                    shape = RoundedCornerShape(99.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            order.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when(order.status) {
                                "Pending" -> Color(0xFFD97706)
                                "Shipped" -> Brand600
                                "Delivered" -> Color(0xFF059669)
                                "Processing" -> Color(0xFF0284C7)
                                else -> Color(0xFFDC2626)
                            }
                        )
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(10.dp).padding(start = 4.dp), tint = Color.Unspecified)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Store, null, modifier = Modifier.size(14.dp), tint = Slate400)
                Spacer(Modifier.width(6.dp))
                Text("Fulfillment: ${order.vendorName}", fontSize = 13.sp, color = Slate700)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = Slate400)
                Spacer(Modifier.width(6.dp))
                Text("Customer: ${order.customerName}", fontSize = 13.sp, color = Slate700)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate50)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${order.itemsCount} Items", fontSize = 13.sp, color = Slate500)
                Text("KSh ${order.amount}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Brand600)
            }
        }
    }
}

@Composable
fun PayoutCard(payout: Map<String, Any>, allUsers: List<Map<String, Any>>, onUpdateClick: () -> Unit) {
    val profiles = payout["profiles"] as? Map<*, *>
    val vendorId = payout["vendor_id"]?.toString()
    val vendorName = profiles?.get("full_name")?.toString() ?: 
                    allUsers.find { it["id"] == vendorId }?.get("full_name")?.toString() ?: "Unknown Vendor"
    val businessName = profiles?.get("business_name")?.toString() ?: 
                      allUsers.find { it["id"] == vendorId }?.get("business_name")?.toString() ?: ""
    val amount = payout["amount"]?.toString() ?: "0"
    val status = payout["status"]?.toString() ?: "pending"
    val date = payout["created_at"]?.toString()?.split("T")?.firstOrNull() ?: ""

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(vendorName, fontWeight = FontWeight.Bold, color = Slate900)
                if (businessName.isNotEmpty()) Text(businessName, fontSize = 12.sp, color = Slate500)
                Text(date, fontSize = 11.sp, color = Slate400)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("KSh $amount", fontWeight = FontWeight.ExtraBold, color = Brand600)
                Surface(
                    onClick = if (status == "pending") onUpdateClick else ({}),
                    color = when(status) {
                        "paid" -> Color(0xFFECFDF5)
                        "pending" -> Color(0xFFFEF3C7)
                        else -> Color(0xFFFEE2E2)
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        status.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when(status) {
                            "paid" -> Color(0xFF059669)
                            "pending" -> Color(0xFFD97706)
                            else -> Color(0xFFDC2626)
                        }
                    )
                }
            }
        }
    }
}

data class AdminOrderItem(
    val id: String,
    val fullId: String,
    val vendorName: String,
    val customerName: String,
    val amount: Int,
    val status: String,
    val date: String,
    val itemsCount: Int
)

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            color = Slate50,
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Slate300
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Slate500,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
