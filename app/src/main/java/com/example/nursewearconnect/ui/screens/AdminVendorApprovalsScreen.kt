package com.example.nursewearconnect.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
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
fun AdminVendorApprovalsScreen(
    onBackClick: () -> Unit,
    viewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vendor Approvals", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Pending Applications",
                    style = MaterialTheme.typography.titleMedium,
                    color = Slate700,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            items(uiState.pendingVendors) { vendorMap ->
                val vendor = PendingVendor(
                    id = vendorMap["id"]?.toString() ?: "0",
                    businessName = vendorMap["business_name"]?.toString() ?: vendorMap["full_name"]?.toString() ?: "Unknown",
                    email = vendorMap["email"]?.toString() ?: "No Email",
                    description = vendorMap["bio"]?.toString() ?: vendorMap["description"]?.toString() ?: "No Description",
                    dateJoined = vendorMap["created_at"]?.toString() ?: "N/A"
                )
                VendorApprovalCard(vendor, 
                    onApprove = { viewModel.approveVendor(vendor.id) },
                    onReject = { viewModel.rejectVendor(vendor.id) }
                )
            }
        }
    }
}

@Composable
fun VendorApprovalCard(vendor: PendingVendor, onApprove: () -> Unit, onReject: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Brand50
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Storefront, tint = Brand600, contentDescription = null)
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(vendor.businessName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                    Text(vendor.email, fontSize = 12.sp, color = Slate500)
                }
                Surface(
                    color = Color(0xFFFFF7ED),
                    shape = RoundedCornerShape(99.dp)
                ) {
                    Text(
                        "Reviewing",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC2410C)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Business Description", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate400)
            Text(vendor.description, fontSize = 13.sp, color = Slate700, lineHeight = 18.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Brand600)
                ) {
                    Text("Approve", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Text("Reject", color = Color(0xFFEF4444), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            TextButton(
                onClick = { /* View Documents */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("View Verification Documents", fontSize = 12.sp, color = Brand600)
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(12.dp), tint = Brand600)
                }
            }
        }
    }
}

data class PendingVendor(
    val id: String,
    val businessName: String,
    val email: String,
    val description: String,
    val dateJoined: String
)
