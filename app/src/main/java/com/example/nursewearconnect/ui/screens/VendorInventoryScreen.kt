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
import com.example.nursewearconnect.model.Product
import com.example.nursewearconnect.ui.theme.*
import com.example.nursewearconnect.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorInventoryScreen(
    onBackClick: () -> Unit,
    viewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddProduct by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    if (showAddProduct || productToEdit != null) {
        ProductDialog(
            product = productToEdit,
            onDismiss = {
                showAddProduct = false
                productToEdit = null
            },
            onConfirm = { product ->
                if (productToEdit != null) {
                    viewModel.updateVendorProduct(product)
                } else {
                    viewModel.addVendorProduct(product)
                }
                showAddProduct = false
                productToEdit = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Inventory", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddProduct = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Product", tint = Brand600)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Slate900
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddProduct = true },
                containerColor = Brand600,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Add Product") }
            )
        },
        containerColor = Slate50
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
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
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search your inventory...") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Slate400) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brand600,
                        unfocusedBorderColor = Slate200
                    ),
                    singleLine = true
                )
            }

            // Inventory Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val totalItems = uiState.vendorProducts.size
                val lowStock = uiState.vendorProducts.count { !it.inStock }
                InventoryStatCard("Total Items", totalItems.toString(), Brand50, Brand600, Modifier.weight(1f))
                InventoryStatCard("Low Stock", "0", Color(0xFFFFF7ED), Color(0xFFEA580C), Modifier.weight(1f))
                InventoryStatCard("Out of Stock", lowStock.toString(), Color(0xFFFEF2F2), Color(0xFFDC2626), Modifier.weight(1f))
            }

            Text(
                "Product List",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Slate700,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val products = uiState.vendorProducts
                val filteredProducts = products.filter {
                    it.name.contains(searchQuery, ignoreCase = true)
                }

                items(filteredProducts) { product ->
                    VendorProductCard(
                        product = product,
                        onEdit = { productToEdit = it },
                        onDelete = { viewModel.deleteVendorProduct(product.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onConfirm: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var price by remember { mutableStateOf(product?.priceKes?.toString() ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var inStock by remember { mutableStateOf(product?.inStock ?: true) }
    
    // Measurement Guide state: List of pairs (Label, Value) e.g., ("Chest", "40-42 in")
    var measurementGuide by remember { 
        mutableStateOf(product?.measurementGuide?.toList() ?: emptyList()) 
    }

    // Validation state
    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Add Product" else "Edit Product") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { 
                            name = it
                            nameError = it.isBlank()
                        },
                        label = { Text("Product Name") },
                        isError = nameError,
                        supportingText = { if (nameError) Text("Name cannot be empty") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { 
                            price = it
                            priceError = it.toIntOrNull() == null
                        },
                        label = { Text("Price (KSh)") },
                        isError = priceError,
                        supportingText = { if (priceError) Text("Enter a valid number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = inStock, onCheckedChange = { inStock = it })
                        Text("In Stock")
                    }
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        "Measurement Guide",
                        style = MaterialTheme.typography.titleSmall,
                        color = Slate700
                    )
                }

                items(measurementGuide.size) { index ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = measurementGuide[index].first,
                            onValueChange = { newKey ->
                                val newList = measurementGuide.toMutableList()
                                newList[index] = Pair(newKey, newList[index].second)
                                measurementGuide = newList
                            },
                            label = { Text("Label (e.g. Chest)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = measurementGuide[index].second,
                            onValueChange = { newValue ->
                                val newList = measurementGuide.toMutableList()
                                newList[index] = Pair(newList[index].first, newValue)
                                measurementGuide = newList
                            },
                            label = { Text("Value") },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            measurementGuide = measurementGuide.toMutableList().apply { removeAt(index) }
                        }) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Remove", tint = Color.Red.copy(alpha = 0.6f))
                        }
                    }
                }

                item {
                    TextButton(
                        onClick = { measurementGuide = measurementGuide + Pair("", "") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Measurement Row")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val isNameValid = name.isNotBlank()
                    val isPriceValid = price.toIntOrNull() != null
                    
                    nameError = !isNameValid
                    priceError = !isPriceValid

                    if (isNameValid && isPriceValid) {
                        onConfirm(
                            Product(
                                id = product?.id ?: java.util.UUID.randomUUID().toString(),
                                name = name,
                                priceKes = price.toIntOrNull() ?: 0,
                                description = description,
                                inStock = inStock,
                                category = product?.category ?: "General",
                                gender = product?.gender ?: "Unisex",
                                tag = product?.tag,
                                rating = product?.rating ?: 0.0,
                                reviewsCount = product?.reviewsCount ?: 0,
                                images = product?.images ?: emptyList(),
                                availableSizes = product?.availableSizes ?: listOf("S", "M", "L", "XL"),
                                availableColors = product?.availableColors ?: emptyList(),
                                measurementGuide = measurementGuide.filter { it.first.isNotBlank() }.toMap()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Brand600)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun InventoryStatCard(label: String, value: String, bgColor: Color, iconColor: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100)
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(bgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when(label) {
                        "Total Items" -> Icons.Default.Inventory2
                        "Low Stock" -> Icons.Default.Warning
                        else -> Icons.Default.ErrorOutline
                    },
                    null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Text(label, fontSize = 12.sp, color = Slate500)
        }
    }
}

@Composable
fun VendorProductCard(
    product: Product,
    onEdit: (Product) -> Unit,
    onDelete: (Product) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Slate50, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(product.category) {
                        "Equipment" -> Icons.Default.MedicalServices
                        "Theatre Shoes" -> Icons.Default.IceSkating
                        else -> Icons.Default.Checkroom
                    },
                    contentDescription = null,
                    tint = Brand600,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                Text("KSh ${product.priceKes}", fontSize = 13.sp, color = Brand600, fontWeight = FontWeight.Bold)
                
                Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (product.inStock) Color(0xFF10B981) else Color(0xFFEF4444), CircleShape)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (product.inStock) "In Stock" else "Out of Stock",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Row {
                    IconButton(onClick = { onEdit(product) }) {
                        Icon(Icons.Default.Edit, null, tint = Slate400, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { onDelete(product) }) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                    }
                }
                Text("24 sold", fontSize = 11.sp, color = Slate400)
            }
        }
    }
}
