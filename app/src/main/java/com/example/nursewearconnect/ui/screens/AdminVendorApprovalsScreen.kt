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
    var showRejectDialog by remember { mutableStateOf<String?>(null) }
    var showCorrectionDialog by remember { mutableStateOf<String?>(null) }
    var actionNotes by remember { mutableStateOf("") }

    if (showRejectDialog != null) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = null; actionNotes = "" },
            title = { Text("Reject Vendor") },
            text = {
                Column {
                    Text("Are you sure you want to reject this vendor? This is a permanent action.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = actionNotes,
                        onValueChange = { actionNotes = it },
                        label = { Text("Rejection Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectVendor(showRejectDialog!!, actionNotes)
                        showRejectDialog = null
                        actionNotes = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Confirm Rejection")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = null; actionNotes = "" }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCorrectionDialog != null) {
        AlertDialog(
            onDismissRequest = { showCorrectionDialog = null; actionNotes = "" },
            title = { Text("Request Corrections") },
            text = {
                Column {
                    Text("Provide feedback to the vendor on what needs to be corrected in their application.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = actionNotes,
                        onValueChange = { actionNotes = it },
                        label = { Text("Correction Instructions") },
                        placeholder = { Text("e.g. Please upload a clearer copy of your business permit.") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectVendor(showCorrectionDialog!!, actionNotes)
                        showCorrectionDialog = null
                        actionNotes = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Brand600)
                ) {
                    Text("Send Feedback")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCorrectionDialog = null; actionNotes = "" }) {
                    Text("Cancel")
                }
            }
        )
    }

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
                    id = vendorMap["user_id"]?.toString() ?: vendorMap["id"]?.toString() ?: "0",
                    businessName = vendorMap["business_name"]?.toString() ?: vendorMap["full_name"]?.toString() ?: "Unknown",
                    email = vendorMap["email"]?.toString() ?: "No Email",
                    description = vendorMap["business_description"]?.toString() ?: vendorMap["bio"]?.toString() ?: "No Description",
                    dateJoined = vendorMap["created_at"]?.toString() ?: "N/A",
                    status = vendorMap["status"]?.toString() ?: "pending"
                )
                VendorApprovalCard(vendor, 
                    onApprove = { viewModel.approveVendor(vendor.id) },
                    onReject = { showRejectDialog = vendor.id },
                    onCorrection = { showCorrectionDialog = vendor.id }
                )
            }
        }
    }
}

@Composable
fun VendorApprovalCard(
    vendor: PendingVendor, 
    onApprove: () -> Unit, 
    onReject: () -> Unit,
    onCorrection: () -> Unit
) {
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
                
                val statusColor = if (vendor.status == "rejected") Color(0xFFEF4444) else Color(0xFFC2410C)
                val statusBg = if (vendor.status == "rejected") Color(0xFFFEF2F2) else Color(0xFFFFF7ED)
                
                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(99.dp)
                ) {
                    Text(
                        vendor.status.replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Business Description", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate400)
            Text(vendor.description, fontSize = 13.sp, color = Slate700, lineHeight = 18.sp)

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Brand600)
                ) {
                    Text("Approve", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                
                OutlinedButton(
                    onClick = onCorrection,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Brand200)
                ) {
                    Text("Correction", color = Brand700, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onReject,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFFEF2F2), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Reject", tint = Color(0xFFEF4444))
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
    val dateJoined: String,
    val status: String = "pending"
)
