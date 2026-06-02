package com.example.nursewearconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nursewearconnect.ui.theme.*
import com.example.nursewearconnect.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMarketingScreen(
    onBackClick: () -> Unit,
    viewModel: HomeViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Categories", "Coupons", "Banners")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Marketing & Catalog", fontWeight = FontWeight.Bold) },
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
        Column(modifier = Modifier.padding(innerPadding)) {
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
                        text = { Text(title, fontSize = 14.sp) }
                    )
                }
            }

            when (selectedTab) {
                0 -> CategoryManagerContent(viewModel)
                1 -> CouponManagerContent(viewModel)
                2 -> BannerManagerContent(viewModel)
            }
        }
    }
}

@Composable
fun CategoryManagerContent(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAddDialog = false 
                nameError = false
            },
            title = { Text("Add New Category") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { 
                        newCategoryName = it
                        nameError = it.isBlank()
                    },
                    label = { Text("Category Name") },
                    isError = nameError,
                    supportingText = { if (nameError) Text("Name cannot be empty") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            viewModel.addCategory(newCategoryName)
                            newCategoryName = ""
                            showAddDialog = false
                            nameError = false
                        } else {
                            nameError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Brand600)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddDialog = false
                    nameError = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Brand600)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add New Category")
            }
        }
        
        items(uiState.categories) { category ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Brand50, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = Brand600)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(category, fontWeight = FontWeight.Bold, color = Slate900)
                        Text("Active in Catalog", fontSize = 12.sp, color = Slate500)
                    }
                    IconButton(onClick = { viewModel.deleteCategory(category) }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

@Composable
fun CouponManagerContent(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    
    // States for new coupon
    var code by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var discountPercent by remember { mutableStateOf("") }
    var maxDiscount by remember { mutableStateOf("") }

    // Validation states
    var codeError by remember { mutableStateOf(false) }
    var discountError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAdminMarketingData()
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAddDialog = false
                codeError = false
                discountError = false
            },
            title = { Text("Create New Coupon") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = code, 
                        onValueChange = { 
                            code = it
                            codeError = it.isBlank()
                        }, 
                        label = { Text("Coupon Code (e.g. SAVE20)") },
                        isError = codeError,
                        supportingText = { if (codeError) Text("Code cannot be empty") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description, 
                        onValueChange = { description = it }, 
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = discountPercent, 
                            onValueChange = { 
                                discountPercent = it
                                discountError = it.isBlank()
                            }, 
                            label = { Text("Discount %") },
                            isError = discountError,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            )
                        )
                        OutlinedTextField(
                            value = maxDiscount, 
                            onValueChange = { maxDiscount = it }, 
                            label = { Text("Max KES") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val isCodeValid = code.isNotBlank()
                        val isDiscountValid = discountPercent.isNotBlank()
                        
                        codeError = !isCodeValid
                        discountError = !isDiscountValid

                        if (isCodeValid && isDiscountValid) {
                            viewModel.addCoupon(mapOf(
                                "code" to code,
                                "description" to description,
                                "discount_percent" to (discountPercent.toIntOrNull() ?: 0),
                                "max_discount_kes" to (maxDiscount.toIntOrNull() ?: 0),
                                "active" to true
                            ))
                            showAddDialog = false
                            code = ""
                            description = ""
                            discountPercent = ""
                            maxDiscount = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Brand600)
                ) {
                    Text("Create")
                }
            },
            dismissButton = { 
                TextButton(onClick = { 
                    showAddDialog = false
                    codeError = false
                    discountError = false
                }) { 
                    Text("Cancel") 
                } 
            }
        )
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Active Promotions", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            TextButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Create")
            }
        }
        Spacer(Modifier.height(16.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(uiState.coupons) { coupon ->
                val couponId = coupon["id"]?.toString() ?: ""
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Brand600)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(coupon["code"]?.toString() ?: "N/A", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                            IconButton(onClick = { viewModel.deleteCoupon(couponId) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                            }
                        }
                        Text(
                            "Get ${coupon["discount_percent"]}% off (Max KES ${coupon["max_discount_kes"]})", 
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(coupon["description"]?.toString() ?: "", color = Color.White.copy(alpha = 0.8f))
                        Spacer(Modifier.height(12.dp))
                        Text("Active Status: ${if (coupon["active"] as? Boolean == true) "ON" else "OFF"}", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun BannerManagerContent(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var bannerTitle by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var actionLink by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bytes = context.contentResolver.openInputStream(it)?.readBytes()
            if (bytes != null) {
                viewModel.uploadBannerImage(bytes) { url ->
                    imageUrl = url
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAdminMarketingData()
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Banner") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = bannerTitle,
                        onValueChange = { bannerTitle = it },
                        label = { Text("Banner Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(Slate100, RoundedCornerShape(8.dp))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUrl.isEmpty()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Brand600)
                                Text("Upload Banner Image", color = Brand600, fontSize = 12.sp)
                            }
                        } else {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    OutlinedTextField(
                        value = actionLink,
                        onValueChange = { actionLink = it },
                        label = { Text("Action Link (e.g. category/Scrubs)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (imageUrl.isNotBlank()) {
                            viewModel.addBanner(mapOf(
                                "title" to bannerTitle,
                                "image_url" to imageUrl,
                                "action_link" to actionLink,
                                "active" to true
                            ))
                            showAddDialog = false
                            bannerTitle = ""
                            imageUrl = ""
                            actionLink = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Brand600)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Brand600)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add New Banner")
            }
        }

        items(uiState.banners) { banner ->
            val bannerId = banner["id"]?.toString() ?: ""
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    AsyncImage(
                        model = banner["image_url"]?.toString(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(Slate200)
                    )
                    
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                banner["title"]?.toString() ?: "Untitled Banner",
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Text(
                                "Link: ${banner["action_link"]?.toString() ?: "None"}",
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        }
                        IconButton(onClick = { viewModel.deleteBanner(bannerId) }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }
}
