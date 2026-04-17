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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nursewearconnect.model.MOCK_PRODUCTS
import com.example.nursewearconnect.model.Product
import com.example.nursewearconnect.ui.theme.*

@Composable
fun CartScreen(innerPadding: PaddingValues) {
    val cartItems = MOCK_PRODUCTS.take(2) // Mocking items in cart

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(bottom = innerPadding.calculateBottomPadding())
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            CheckoutHeader()

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item { CheckoutStepper() }
                
                item {
                    Text(
                        text = "Order Review",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
                
                items(cartItems) { product ->
                    CheckoutItemCard(product)
                }

                item { ReorderHistorySection() }

                item { DeliverySection() }

                item { ShippingMethodSection() }

                item { PromoCodeSection() }

                item { PaymentSummarySection() }

                item { ReceiptToggleSection() }
                
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }

        // Sticky Bottom CTA
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 80.dp), // Height of bottom nav
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, Slate100)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Brand600)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Continue to Payment", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Secure checkout powered by M-Pesa & Stripe",
                    fontSize = 10.sp,
                    color = Slate500
                )
            }
        }
    }
}

@Composable
fun CheckoutHeader() {
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
                onClick = { },
                modifier = Modifier
                    .size(40.dp)
                    .background(Slate50, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Slate600)
            }
            Text(text = "Checkout", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
            Spacer(modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
fun CheckoutStepper() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StepItem(icon = Icons.Default.Check, label = "Review", isActive = true, isCompleted = true)
        StepperLine(isActive = true)
        StepItem(text = "2", label = "Address", isActive = true)
        StepperLine(isActive = false)
        StepItem(text = "3", label = "Payment", isActive = false)
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
fun CheckoutItemCard(product: Product) {
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
                Text(product.emoji, fontSize = 32.sp)
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
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Brand600, modifier = Modifier.size(14.dp))
                }
                Text(text = "Navy Blue • Size M", fontSize = 11.sp, color = Slate500)
                
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
                        text = "KSh ${product.priceKes}",
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
                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(10.dp), tint = Slate400)
                        Text("1", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate700)
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(10.dp), tint = Slate400)
                    }
                }
            }
        }
    }
}

@Composable
fun ReorderHistorySection() {
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
            items(2) { index ->
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
                            Text(if (index == 0) "🧦" else "🩺", fontSize = 32.sp)
                        }
                        Text(
                            text = if (index == 0) "Compression Socks" else "Classic Stethoscope",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate800,
                            maxLines = 1,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(text = if (index == 0) "KSh 1,800" else "KSh 11,000", fontSize = 10.sp, color = Slate500)
                        
                        Button(
                            onClick = { },
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
fun DeliverySection() {
    Column(modifier = Modifier.padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Delivery Address", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Text("Change", fontSize = 12.sp, color = Brand600, fontWeight = FontWeight.Medium)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Brand200),
            shadowElevation = 2.dp
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
                            Text("Nairobi Hospital", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Slate100
                            ) {
                                Text(
                                    "WORK",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate600,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            "Argwings Kodhek Rd, Nairobi\nWard 4B, Staff Quarters",
                            fontSize = 12.sp,
                            color = Slate600,
                            lineHeight = 18.sp
                        )
                        Text("+254 712 345 678", fontSize = 11.sp, color = Slate500, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ShippingMethodSection() {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text("Shipping Method", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
        Spacer(modifier = Modifier.height(12.dp))
        
        ShippingOption(
            title = "Standard Delivery",
            duration = "2-3 Business Days",
            price = "Free",
            isSelected = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        ShippingOption(
            title = "Express Delivery",
            duration = "Same Day (Order before 2PM)",
            price = "KSh 500",
            isSelected = false
        )
    }
}

@Composable
fun ShippingOption(title: String, duration: String, price: String, isSelected: Boolean) {
    Surface(
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
fun PaymentSummarySection() {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        HorizontalDivider(color = Slate100, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Payment Summary", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
        Spacer(modifier = Modifier.height(16.dp))
        
        SummaryRow("Subtotal (2 items)", "KSh 8,800")
        SummaryRow("Embroidery Add-on", "KSh 1,000")
        SummaryRow("Shipping", "Free", isFree = true)
        SummaryRow("Tax (16% VAT)", "KSh 1,660")
        
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Slate200, thickness = 1.dp, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Total Amount", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Text("KSh 11,460", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Brand600)
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isFree: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Slate500)
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isFree) Brand600 else Slate900
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
