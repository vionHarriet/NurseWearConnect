package com.example.nursewearconnect.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import com.example.nursewearconnect.ui.theme.*
import com.example.nursewearconnect.ui.viewmodel.CartItem
import com.example.nursewearconnect.ui.viewmodel.HomeViewModel
import com.example.nursewearconnect.ui.viewmodel.UserType

@Composable
fun CartScreen(
    innerPadding: PaddingValues,
    viewModel: HomeViewModel,
    onNavigateToCatalog: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val cartItems = uiState.cartItems
    var checkoutStep by remember { mutableIntStateOf(1) } // 1: Review, 2: Address, 3: Payment, 4: Success

    // Address State
    var address by remember { mutableStateOf("Argwings Kodhek Rd, Nairobi\nWard 4B, Staff Quarters") }
    var locationName by remember { mutableStateOf("Nairobi Hospital") }
    var phoneNumber by remember { mutableStateOf("+254 712 345 678") }
    var locationType by remember { mutableStateOf("WORK") }
    var showAddressDialog by remember { mutableStateOf(false) }

    // Shipping State
    var selectedShippingMethod by remember { mutableStateOf("Standard") }

    // Calculation logic for total including discounts
    val subtotal = cartItems.sumOf { it.product.priceKes * it.quantity }
    val discountRate = when (uiState.userType) {
        UserType.STUDENT -> 0.20
        UserType.PROFESSIONAL -> 0.10
    }
    val discountAmount = (subtotal * discountRate).toInt()
    val shippingCost = if (selectedShippingMethod == "Express") 500 else 0
    val tax = ((subtotal - discountAmount) * 0.16).toInt()
    val finalTotal = subtotal - discountAmount + tax + shippingCost

    // Observe orderId to trigger M-Pesa payment
    LaunchedEffect(uiState.orderId) {
        if (uiState.orderId != null && checkoutStep == 3) {
            // Check if M-Pesa was selected (current default in state)
            // For now, automatically trigger for demonstration if we reach here from checkout
            viewModel.initiatePayment(
                orderId = uiState.orderId!!,
                phoneNumber = phoneNumber.replace("+", "").replace(" ", ""),
                amount = finalTotal.toDouble()
            )
            checkoutStep = 4
        }
    }

    if (showAddressDialog) {
        AlertDialog(
            onDismissRequest = { showAddressDialog = false },
            title = { Text("Edit Delivery Address", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = locationName,
                        onValueChange = { locationName = it },
                        label = { Text("Location Name (e.g. Home, Work)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Full Address") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = locationType == "WORK",
                            onClick = { locationType = "WORK" },
                            label = { Text("Work") }
                        )
                        FilterChip(
                            selected = locationType == "HOME",
                            onClick = { locationType = "HOME" },
                            label = { Text("Home") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAddressDialog = false }) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddressDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(bottom = innerPadding.calculateBottomPadding())
    ) {
        if (checkoutStep == 4) {
            OrderSuccessState(
                onContinueShopping = onNavigateToCatalog,
                paymentStatus = uiState.paymentStatus
            )
        } else if (cartItems.isEmpty()) {
            EmptyCartState(onNavigateToCatalog)
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                CheckoutHeader(
                    onBackClick = {
                        if (checkoutStep > 1) checkoutStep--
                        else onNavigateToCatalog()
                    },
                    step = checkoutStep
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 140.dp) // Extra padding for the sticky CTA
                ) {
                    item { CheckoutStepper(currentStep = checkoutStep) }

                    when (checkoutStep) {
                        1 -> {
                            item {
                                Text(
                                    text = "Order Review",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                )
                            }

                            items(cartItems) { item ->
                                CheckoutItemCard(
                                    item,
                                    onIncrease = { viewModel.updateCartItemQuantity(item, item.quantity + 1) },
                                    onDecrease = { viewModel.updateCartItemQuantity(item, item.quantity - 1) },
                                    onRemove = { viewModel.removeFromCart(item) }
                                )
                            }

                            item { 
                                ReorderHistorySection(
                                    reorderItems = uiState.reorderItems,
                                    onAddToCart = { viewModel.addToCart(it) }
                                ) 
                            }
                        }
                        2 -> {
                            item { 
                                DeliverySection(
                                    locationName = locationName,
                                    address = address,
                                    phone = phoneNumber,
                                    locationType = locationType,
                                    onEditClick = { showAddressDialog = true }
                                ) 
                            }
                            item { 
                                ShippingMethodSection(
                                    selectedMethod = selectedShippingMethod,
                                    onMethodSelected = { selectedShippingMethod = it }
                                ) 
                            }
                        }
                        3 -> {
                            item { PromoCodeSection() }
                            item { PaymentMethodSection(phoneNumber = phoneNumber) }
                            item { PaymentSummarySection(cartItems, uiState.userType, selectedShippingMethod) }
                            item { 
                                if (uiState.checkoutError != null) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp).fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = uiState.checkoutError ?: "An unexpected error occurred",
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                            item { ReceiptToggleSection() }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }

            // Sticky Bottom CTA
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = Color.White,
                shadowElevation = 16.dp,
                border = BorderStroke(1.dp, Slate100)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            if (checkoutStep < 3) {
                                checkoutStep++
                            } else {
                                val totalToCharge = finalTotal.toDouble()
                                viewModel.checkout(
                                    userId = uiState.userName.ifEmpty { "demo_user" },
                                    totalAmount = totalToCharge,
                                    address = address
                                )
                            }
                        },
                        enabled = !uiState.checkoutLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Brand600)
                    ) {
                        if (uiState.checkoutLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            val buttonText = when (checkoutStep) {
                                1 -> "Review Shipping Address"
                                2 -> "Continue to Payment"
                                3 -> "Complete Order - KSh ${"%,d".format(finalTotal)}"
                                else -> ""
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(buttonText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Lock, null, modifier = Modifier.size(12.dp), tint = Slate400)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Secure checkout powered by M-Pesa & Stripe",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCartState(onNavigateToCatalog: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🛒", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Your cart is empty", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Slate900)
        Text(
            "Looks like you haven't added anything to your cart yet.",
            fontSize = 14.sp,
            color = Slate500,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNavigateToCatalog,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Brand600)
        ) {
            Text("Start Shopping")
        }
    }
}

@Composable
fun OrderSuccessState(onContinueShopping: () -> Unit, paymentStatus: String? = null) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = if (paymentStatus?.contains("Error") == true) MaterialTheme.colorScheme.errorContainer else Brand50
        ) {
            Icon(
                imageVector = if (paymentStatus?.contains("Error") == true) Icons.Default.Close else Icons.Default.Check,
                contentDescription = null,
                tint = if (paymentStatus?.contains("Error") == true) MaterialTheme.colorScheme.error else Brand600,
                modifier = Modifier.padding(24.dp).size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (paymentStatus?.contains("Error") == true) "Payment Failed" else "Order Placed Successfully!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )
        Text(
            text = paymentStatus ?: "Your order has been placed and is being processed. You will receive an update soon.",
            fontSize = 14.sp,
            color = Slate500,
            modifier = Modifier.padding(top = 12.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onContinueShopping,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Brand600)
        ) {
            Text("Continue Shopping")
        }
    }
}

@Composable
fun CheckoutHeader(onBackClick: () -> Unit = {}, step: Int) {
    Surface(
        color = Color.White.copy(alpha = 0.9f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(Slate50, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Slate600)
            }
            Text(
                text = if (step == 3) "Payment" else if (step == 2) "Delivery" else "Checkout",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Slate900
            )
            Spacer(modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
fun CheckoutStepper(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StepItem(
            icon = if (currentStep > 1) Icons.Default.Check else null,
            text = if (currentStep <= 1) "1" else null,
            label = "Review",
            isActive = currentStep >= 1,
            isCompleted = currentStep > 1
        )
        StepperLine(isActive = currentStep > 1)
        StepItem(
            icon = if (currentStep > 2) Icons.Default.Check else null,
            text = if (currentStep <= 2) "2" else null,
            label = "Address",
            isActive = currentStep >= 2,
            isCompleted = currentStep > 2
        )
        StepperLine(isActive = currentStep > 2)
        StepItem(
            text = "3",
            label = "Payment",
            isActive = currentStep >= 3
        )
    }
}

@Composable
fun StepItem(
    icon: ImageVector? = null,
    text: String? = null,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    if (isActive) Brand500 else Color.White,
                    CircleShape
                )
                .then(
                    if (!isActive) Modifier.background(Color.White, CircleShape).border(2.dp, Slate200, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted && icon != null) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            } else if (text != null) {
                Text(
                    text = text,
                    color = if (isActive) Color.White else Slate400,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) Brand500 else Slate400,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun RowScope.StepperLine(isActive: Boolean) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(2.dp)
            .padding(horizontal = 8.dp)
            .background(if (isActive) Brand500 else Slate200)
    )
}

@Composable
fun CheckoutItemCard(
    cartItem: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    val product = cartItem.product
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100),
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 96.dp)
                    .background(Slate50, RoundedCornerShape(12.dp))
                    .border(1.dp, Slate100, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (product.images.isNotEmpty()) {
                    AsyncImage(
                        model = product.images.first(),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Slate300, modifier = Modifier.size(32.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = product.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Slate300, modifier = Modifier.size(18.dp))
                    }
                }
                Text(
                    text = "${cartItem.color?.name ?: "Default"} • Size ${cartItem.size}",
                    fontSize = 11.sp,
                    color = Slate500
                )
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Brand500, modifier = Modifier.size(10.dp))
                    Text(text = " + Embroidery (KSh 1,000)", fontSize = 10.sp, color = Brand600, fontWeight = FontWeight.Medium)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "KSh ${"%,d".format(product.priceKes)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .background(Slate50, RoundedCornerShape(8.dp))
                            .border(1.dp, Slate100, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        IconButton(onClick = onDecrease, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(12.dp), tint = Slate600)
                        }
                        Text("${cartItem.quantity}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate700)
                        IconButton(onClick = onIncrease, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp), tint = Slate600)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReorderHistorySection(
    reorderItems: List<com.example.nursewearconnect.model.Product>,
    onAddToCart: (com.example.nursewearconnect.model.Product) -> Unit
) {
    if (reorderItems.isEmpty()) return

    Column(modifier = Modifier.padding(vertical = 16.dp).background(Color.White).padding(vertical = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Buy it again", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Text("View All", fontSize = 12.sp, color = Brand600, fontWeight = FontWeight.Medium)
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(reorderItems) { product ->
                Surface(
                    modifier = Modifier.width(128.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Slate50,
                    border = BorderStroke(1.dp, Slate100)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(96.dp)
                                .background(Color.White, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (product.images.isNotEmpty()) {
                                AsyncImage(
                                    model = product.images.first(),
                                    contentDescription = product.name,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Slate200, modifier = Modifier.size(32.dp))
                            }
                        }
                        Text(
                            text = product.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate800,
                            maxLines = 1,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(text = "KSh ${"%,d".format(product.priceKes)}", fontSize = 10.sp, color = Slate500)
                        
                        Button(
                            onClick = { onAddToCart(product) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .height(28.dp),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Brand200)
                        ) {
                            Text("+ Add", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Brand600)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeliverySection(
    locationName: String,
    address: String,
    phone: String,
    locationType: String,
    onEditClick: () -> Unit
) {
    Column(modifier = Modifier.padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Delivery Address", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
            TextButton(onClick = onEditClick) {
                Text("Change", fontSize = 12.sp, color = Brand600, fontWeight = FontWeight.Medium)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Brand200),
            shadowElevation = 2.dp,
            onClick = onEditClick
        ) {
            Box {
                // Decorative circle
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .offset(x = 100.dp, y = (-20).dp)
                        .background(Brand50, CircleShape)
                        .align(Alignment.TopEnd)
                )
                
                Row(modifier = Modifier.padding(16.dp)) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = Brand50
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Brand600,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(locationName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Slate100
                            ) {
                                Text(
                                    locationType,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate600,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            address,
                            fontSize = 12.sp,
                            color = Slate600,
                            lineHeight = 18.sp
                        )
                        Text(phone, fontSize = 11.sp, color = Slate500, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ShippingMethodSection(
    selectedMethod: String,
    onMethodSelected: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text("Shipping Method", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
        Spacer(modifier = Modifier.height(12.dp))
        
        ShippingOption(
            title = "Standard Delivery",
            duration = "2-3 Business Days",
            price = "Free",
            isSelected = selectedMethod == "Standard",
            onClick = { onMethodSelected("Standard") }
        )
        Spacer(modifier = Modifier.height(12.dp))
        ShippingOption(
            title = "Express Delivery",
            duration = "Same Day (Order before 2PM)",
            price = "KSh 500",
            isSelected = selectedMethod == "Express",
            onClick = { onMethodSelected("Express") }
        )
    }
}

@Composable
fun ShippingOption(title: String, duration: String, price: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, if (isSelected) Brand500 else Slate200),
        color = if (isSelected) Brand50.copy(alpha = 0.3f) else Color.White
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .border(2.dp, if (isSelected) Brand500 else Slate300, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(modifier = Modifier.size(10.dp).background(Brand500, CircleShape))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Text(duration, fontSize = 11.sp, color = Slate500)
            }
            Text(price, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
        }
    }
}

@Composable
fun PromoCodeSection() {
    Surface(
        modifier = Modifier.padding(24.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Slate200),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ConfirmationNumber,
                contentDescription = null,
                tint = Brand500,
                modifier = Modifier.padding(start = 12.dp).size(16.dp)
            )
            TextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Enter promo code", fontSize = 13.sp, color = Slate400) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f).height(40.dp)
            )
            Button(
                onClick = { },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                modifier = Modifier.height(36.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text("Apply", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MpesaLogo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color(0xFFF1F8E9), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "M-",
            color = Color(0xFF4CAF50),
            fontWeight = FontWeight.Black,
            fontSize = 14.sp
        )
        Text(
            text = "PESA",
            color = Color(0xFFE91E63),
            fontWeight = FontWeight.Black,
            fontSize = 14.sp
        )
    }
}

@Composable
fun PaymentMethodSection(phoneNumber: String) {
    var selectedMethod by remember { mutableStateOf("M-Pesa") }

    Column(modifier = Modifier.padding(24.dp)) {
        Text("Payment Method", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PaymentMethodCard(
                name = "M-Pesa",
                content = { MpesaLogo() },
                isSelected = selectedMethod == "M-Pesa",
                onClick = { selectedMethod = "M-Pesa" },
                modifier = Modifier.weight(1f)
            )
            PaymentMethodCard(
                name = "Stripe",
                content = { Icon(Icons.Default.CreditCard, null, tint = if (selectedMethod == "Stripe") Brand600 else Slate400) },
                isSelected = selectedMethod == "Stripe",
                onClick = { selectedMethod = "Stripe" },
                modifier = Modifier.weight(1f)
            )
        }
        
        if (selectedMethod == "M-Pesa") {
            MpesaForm(phoneNumber)
        } else {
            StripeForm()
        }
    }
}

@Composable
fun PaymentMethodCard(
    name: String,
    content: @Composable () -> Unit,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, if (isSelected) Brand500 else Slate200),
        color = if (isSelected) Brand50.copy(alpha = 0.3f) else Color.White,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
            Spacer(modifier = Modifier.height(8.dp))
            Text(name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Brand600 else Slate700)
        }
    }
}

@Composable
fun MpesaForm(phoneNumber: String) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text("M-Pesa Number", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Slate500)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Text("🇰🇪", modifier = Modifier.padding(start = 12.dp)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Brand600,
                unfocusedBorderColor = Slate200
            )
        )
        Text(
            "You will receive an STK push on your phone to authorize the payment.",
            fontSize = 11.sp,
            color = Slate500,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun StripeForm() {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text("Card Information", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Slate500)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = "**** **** **** 4242",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.CreditCard, null, tint = Slate400) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Brand600,
                unfocusedBorderColor = Slate200
            )
        )
        Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = "12/26",
                onValueChange = {},
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("MM/YY") }
            )
            OutlinedTextField(
                value = "***",
                onValueChange = {},
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("CVC") }
            )
        }
    }
}

@Composable
fun PaymentSummarySection(
    cartItems: List<CartItem>, 
    userType: UserType,
    selectedShippingMethod: String
) {
    val subtotal = cartItems.sumOf { it.product.priceKes * it.quantity }
    
    val discountRate = when (userType) {
        UserType.STUDENT -> 0.20
        UserType.PROFESSIONAL -> 0.10
    }
    
    val discountAmount = (subtotal * discountRate).toInt()
    val shippingCost = if (selectedShippingMethod == "Express") 500 else 0
    val tax = ((subtotal - discountAmount) * 0.16).toInt()
    val total = subtotal - discountAmount + tax + shippingCost

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        HorizontalDivider(color = Slate100, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Payment Summary", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
        Spacer(modifier = Modifier.height(16.dp))
        
        SummaryRow("Subtotal (${cartItems.size} items)", "KSh ${"%,d".format(subtotal)}")
        
        val discountLabel = if (userType == UserType.STUDENT) "Student Discount (20%)" else "Medical Pro Discount (10%)"
        SummaryRow(discountLabel, "- KSh ${"%,d".format(discountAmount)}", isDiscount = true)
        
        SummaryRow("Embroidery Add-on", "KSh 0")
        SummaryRow(
            label = if (selectedShippingMethod == "Express") "Express Shipping" else "Standard Shipping",
            value = if (shippingCost == 0) "Free" else "KSh ${"%,d".format(shippingCost)}",
            isFree = shippingCost == 0
        )
        SummaryRow("Tax (16% VAT)", "KSh ${"%,d".format(tax)}")
        
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Slate200, thickness = 1.dp, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Total Amount", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Text("KSh ${"%,d".format(total)}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Brand600)
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isFree: Boolean = false, isDiscount: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = if (isDiscount) Color(0xFF059669) else Slate500)
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isFree || isDiscount) Color(0xFF059669) else Slate900
        )
    }
}

@Composable
fun ReceiptToggleSection() {
    Surface(
        modifier = Modifier.padding(horizontal = 24.dp),
        shape = RoundedCornerShape(12.dp),
        color = Brand50,
        border = BorderStroke(1.dp, Brand100)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Receipt, contentDescription = null, tint = Brand500, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Digital Receipt", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Text("Send to email for tax purposes", fontSize = 11.sp, color = Slate600)
            }
            Switch(
                checked = true,
                onCheckedChange = {},
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Brand500,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Slate200
                )
            )
        }
    }
}
