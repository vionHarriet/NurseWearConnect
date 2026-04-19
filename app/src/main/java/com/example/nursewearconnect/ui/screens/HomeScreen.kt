package com.example.nursewearconnect.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.nursewearconnect.model.Product
import com.example.nursewearconnect.model.ProductColor
import com.example.nursewearconnect.ui.theme.*
import com.example.nursewearconnect.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    innerPadding: PaddingValues, 
    userRole: String = "student",
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToCatalog: () -> Unit = {},
    onNavigateToUserLogs: () -> Unit = {},
    onNavigateToAdminUsers: () -> Unit = {},
    onNavigateToAdminVendors: () -> Unit = {},
    onNavigateToAdminInventory: () -> Unit = {},
    onNavigateToAdminOrders: () -> Unit = {},
    onNavigateToAdminMarketing: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToVendorInventory: () -> Unit = {},
    onNavigateToVendorOrders: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userRole = uiState.userRole

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
        // Decorative Background Gradients
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Brand100.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.statusBarsPadding())
            HomeHeader(
                userRole = userRole,
                userName = uiState.userName,
                greeting = uiState.greeting,
                unreadNotificationsCount = uiState.unreadNotificationsCount,
                unreadMessagesCount = 2, // Example static for now
                onNotificationsClick = onNavigateToNotifications,
                onMessagesClick = onNavigateToMessages,
                onProfileClick = onNavigateToProfile
            )
            
            SearchBar(uiState.searchQuery) { viewModel.onSearchQueryChanged(it) }
            
            if (userRole != "admin") {
                CategorySelector(
                    categories = uiState.categories, 
                    activeCat = uiState.activeCategory
                ) { viewModel.onCategorySelected(it) }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            if (userRole == "vendor") {
                VendorStats()
            } else if (userRole == "admin") {
                AdminStats(onInventoryClick = { onNavigateToAdminInventory() })
            } else {
                HeroBanner(
                    featuredProduct = uiState.featuredProduct,
                    onShopNowClick = { onNavigateToCatalog() }
                )
            }
            
            // New Arrivals Section
            if ((userRole == "student" || userRole == "professional") && uiState.newArrivals.isNotEmpty()) {
                SectionHeader(
                    title = "New Arrivals",
                    subtitle = "Fresh styles for your shift",
                    onSeeAllClick = { onNavigateToCatalog() }
                )
                NewArrivalsRow(
                    products = uiState.newArrivals,
                    onProductClick = { viewModel.setSelectedProduct(it) },
                    onAddToCart = { viewModel.addToCart(it) }
                )
            }
            
            QuickActions(
                userRole = userRole,
                onQuickReorderClick = { viewModel.setShowQuickReorder(true) },
                onFavoritesClick = { viewModel.setShowFavorites(true) },
                onUserLogsClick = onNavigateToUserLogs,
                onAdminUsersClick = onNavigateToAdminUsers,
                onAdminVendorsClick = onNavigateToAdminVendors,
                onAdminMarketingClick = onNavigateToAdminMarketing,
                onReportsClick = onNavigateToReports
            )
            
            SectionHeader(
                title = when(userRole) {
                    "vendor" -> "Your Recent Orders"
                    "admin" -> "Recent System Activity"
                    else -> "Recommended for You"
                },
                subtitle = when(userRole) {
                    "vendor" -> "Track your sales performance"
                    "admin" -> "Overview of latest registrations and orders"
                    else -> "Based on your sizing profile"
                },
                onSeeAllClick = { onNavigateToCatalog() }
            )
            
            if (userRole == "admin") {
                AdminActivityList(onSeeAllOrders = onNavigateToAdminOrders)
            } else {
                ProductGrid(
                    products = uiState.recommendations,
                    favoriteProductIds = uiState.favoriteProductIds,
                    onFavoriteToggle = { viewModel.toggleFavorite(it.id) },
                    onAddToCart = { viewModel.addToCart(it) },
                    onProductClick = { viewModel.setSelectedProduct(it) }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Favorites Bottom Sheet
        if (uiState.showFavorites) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.setShowFavorites(false) },
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text("Your Favorites", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    Spacer(Modifier.height(16.dp))
                    
                    val favoriteProducts = uiState.products.filter { uiState.favoriteProductIds.contains(it.id) }
                    favoriteProducts.forEach { product ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp), color = Slate50) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (product.images.isNotEmpty()) {
                                        AsyncImage(
                                            model = product.images.first(),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(Icons.Default.Inventory, contentDescription = null, tint = Slate300)
                                    }
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(product.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("KSh ${product.priceKes}", fontSize = 12.sp, color = Slate500)
                            }
                            IconButton(onClick = { viewModel.toggleFavorite(product.id) }) {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFF43F5E))
                            }
                        }
                        HorizontalDivider(color = Slate100)
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun ProductDetailContent(
    product: Product,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    selectedSize: String?,
    onSizeSelected: (String) -> Unit,
    selectedColor: ProductColor?,
    onColorSelected: (ProductColor) -> Unit,
    onAddToCart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Image Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Slate50, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (product.images.isNotEmpty()) {
                AsyncImage(
                    model = product.images.first(),
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Brand50, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when(product.category) {
                            "Equipment" -> Icons.Default.MedicalServices
                            "Theatre Shoes" -> Icons.Default.IceSkating // Best fit for shoes in default icons
                            else -> Icons.Default.Checkroom
                        },
                        contentDescription = null,
                        tint = Brand600,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.White.copy(alpha = 0.8f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFFF43F5E) else Slate300
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${product.gender} • ${product.category}".uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Brand600,
                    letterSpacing = 1.sp
                )
                Text(
                    product.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    lineHeight = 30.sp
                )
            }
            Text(
                "KSh ${product.priceKes}",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Brand600
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Icon(Icons.Default.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
            Text(
                " ${product.rating} ",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Text(
                "(${product.reviewsCount} reviews)",
                fontSize = 14.sp,
                color = Slate500
            )
        }

        HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 8.dp))

        // Size Selection
        if (product.availableSizes.isNotEmpty()) {
            Text("Select Size", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                product.availableSizes.forEach { size ->
                    val isSelected = size == selectedSize
                    Box(
                        modifier = Modifier
                            .size(width = 56.dp, height = 40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Brand600 else Slate50)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Brand600 else Slate200,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSizeSelected(size) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = size,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Slate700
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // Color Selection
        if (product.availableColors.isNotEmpty()) {
            Text("Select Color", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                product.availableColors.forEach { color ->
                    val isSelected = color == selectedColor
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onColorSelected(color) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(color.hex))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Brand600 else Slate200,
                                    shape = CircleShape
                                )
                                .padding(2.dp)
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (color.name == "White") Slate900 else Color.White,
                                    modifier = Modifier.size(16.dp).align(Alignment.Center)
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            color.name,
                            fontSize = 12.sp,
                            color = if (isSelected) Brand600 else Slate500,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // Description Section
        Text("Description", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
        Spacer(Modifier.height(8.dp))
        Text(
            product.description,
            fontSize = 14.sp,
            color = Slate600,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(16.dp))

        // Material & Features (Inspired by AlphaMed)
        Text("Material", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, null, tint = Brand500, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(product.material, fontSize = 14.sp, color = Slate600)
        }

        Spacer(Modifier.height(16.dp))

        Text("Key Features", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
        Spacer(Modifier.height(8.dp))
        product.features.forEach { feature ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(modifier = Modifier.size(6.dp).background(Brand600, CircleShape))
                Spacer(Modifier.width(12.dp))
                Text(feature, fontSize = 14.sp, color = Slate600)
            }
        }

        Spacer(Modifier.height(32.dp))

        // Add to Cart Button
        Button(
            onClick = onAddToCart,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Brand600)
        ) {
            Icon(Icons.Default.ShoppingCart, null)
            Spacer(Modifier.width(12.dp))
            Text("Add to Cart", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun HomeHeader(
    userRole: String,
    userName: String,
    greeting: String,
    unreadNotificationsCount: Int,
    unreadMessagesCount: Int,
    onNotificationsClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = greeting,
                fontSize = 14.sp,
                color = Slate500,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = when(userRole) {
                    "vendor" -> "$userName (Vendor)"
                    "admin" -> "$userName (Admin)"
                    "professional" -> "$userName (Pro)"
                    else -> userName
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeaderIconButton(
                icon = Icons.Outlined.Notifications,
                badgeCount = unreadNotificationsCount,
                onClick = onNotificationsClick
            )
            HeaderIconButton(
                icon = Icons.Default.ChatBubbleOutline,
                badgeCount = unreadMessagesCount,
                onClick = onMessagesClick
            )
            Surface(
                onClick = onProfileClick,
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Brand100,
                border = BorderStroke(2.dp, Color.White)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = userName.take(1).uppercase(),
                        color = Brand600,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderIconButton(
    icon: ImageVector,
    badgeCount: Int,
    onClick: () -> Unit
) {
    Box {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(1.dp, Slate100)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Slate600
                )
            }
        }
        
        if (badgeCount > 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp),
                color = Color(0xFFF43F5E),
                shape = CircleShape,
                border = BorderStroke(2.dp, Color.White)
            ) {
                Text(
                    text = badgeCount.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, null, tint = Slate400)
            Spacer(Modifier.width(12.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text("Search scrubs, shoes, equipment...", color = Slate400, fontSize = 14.sp)
                    }
                    innerTextField()
                }
            )
            Icon(Icons.Default.FilterList, null, tint = Brand600)
        }
    }
}

@Composable
fun CategorySelector(
    categories: List<String>,
    activeCat: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == activeCat
            Surface(
                onClick = { onCategorySelected(category) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Brand600 else Color.White,
                border = BorderStroke(1.dp, if (isSelected) Brand600 else Slate200)
            ) {
                Text(
                    text = category,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    color = if (isSelected) Color.White else Slate600,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun VendorStats() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(Brand600, RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Text("Your Shop Performance", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
        Text("KSh 142,500", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        
        Spacer(Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatItem("Active Orders", "12", Icons.Default.Inventory)
            StatItem("Low Stock", "3", Icons.Default.Warning)
            StatItem("Reviews", "4.9", Icons.Default.Star)
        }
    }
}

@Composable
fun AdminStats(
    onInventoryClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(Slate900, RoundedCornerShape(24.dp))
            .clickable { onInventoryClick() }
            .padding(24.dp)
    ) {
        Text("System Overview", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
        Text("Active System Health", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        
        Spacer(Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatItem("Total Users", "1,240", Icons.Default.People)
            StatItem("Pending Vendors", "8", Icons.Default.PendingActions)
            StatItem("Revenue (M)", "2.4", Icons.Default.Payments)
        }
    }
}

@Composable
fun AdminActivityList(
    onSeeAllOrders: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) { index ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (index == 1) onSeeAllOrders() },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Slate100)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Brand50, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when(index) {
                                0 -> Icons.Default.PersonAdd
                                1 -> Icons.Default.ShoppingCart
                                else -> Icons.Default.Report
                            },
                            contentDescription = null,
                            tint = Brand600,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when(index) {
                                0 -> "New Vendor Registration"
                                1 -> "High Value Order Placed"
                                else -> "System Update Complete"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = when(index) {
                                0 -> "Elite Uniforms Ltd"
                                1 -> "Order #8921 - KSh 15,000"
                                else -> "v1.2.4 deployed successfully"
                            },
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                    Text("2m ago", fontSize = 10.sp, color = Slate400)
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, icon: ImageVector) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }
        Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HeroBanner(
    featuredProduct: Product?,
    onShopNowClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        color = Brand600
    ) {
        Box {
            // Decorative background patterns
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = Color.White.copy(alpha = 0.1f), radius = 100.dp.toPx(), center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.2f))
                drawCircle(color = Color.White.copy(alpha = 0.05f), radius = 150.dp.toPx(), center = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.8f))
            }
            
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(99.dp)
                    ) {
                        Text(
                            "NEW ARRIVAL",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        featuredProduct?.name ?: "Premium Scrub Collection",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 26.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onShopNowClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Shop Now", color = Brand600, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                
                // Product Image Placeholder
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActions(
    userRole: String,
    onQuickReorderClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onUserLogsClick: () -> Unit = {},
    onAdminUsersClick: () -> Unit = {},
    onAdminVendorsClick: () -> Unit = {},
    onAdminMarketingClick: () -> Unit = {},
    onReportsClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (userRole == "admin") {
            QuickActionCard(
                title = "Users",
                icon = Icons.Default.People,
                bgColor = Color(0xFFEFF6FF),
                iconColor = Color(0xFF3B82F6),
                modifier = Modifier.width(100.dp),
                onClick = onAdminUsersClick
            )
            QuickActionCard(
                title = "Vendors",
                icon = Icons.Default.Store,
                bgColor = Color(0xFFF0FDF4),
                iconColor = Color(0xFF22C55E),
                modifier = Modifier.width(100.dp),
                onClick = onAdminVendorsClick
            )
            QuickActionCard(
                title = "Marketing",
                icon = Icons.Default.Campaign,
                bgColor = Color(0xFFFEF3C7),
                iconColor = Color(0xFFD97706),
                modifier = Modifier.width(100.dp),
                onClick = onAdminMarketingClick
            )
            QuickActionCard(
                title = "Reports",
                icon = Icons.Default.BarChart,
                bgColor = Color(0xFFF5F3FF),
                iconColor = Color(0xFF8B5CF6),
                modifier = Modifier.width(100.dp),
                onClick = onReportsClick
            )
            QuickActionCard(
                title = "Logs",
                icon = Icons.Default.History,
                bgColor = Color(0xFFF3F4F6),
                iconColor = Color(0xFF4B5563),
                modifier = Modifier.width(100.dp),
                onClick = onUserLogsClick
            )
        } else {
            QuickActionCard(
                title = if (userRole == "vendor") "Inventory" else "Favorites",
                icon = if (userRole == "vendor") Icons.Default.List else Icons.Default.Favorite,
                bgColor = Color(0xFFFDF2F8),
                iconColor = Color(0xFFF472B6),
                modifier = Modifier.weight(1f),
                onClick = onFavoritesClick
            )
            QuickActionCard(
                title = if (userRole == "vendor") "Messages" else "Quick Reorder",
                icon = if (userRole == "vendor") Icons.Default.ChatBubble else Icons.Default.Autorenew,
                bgColor = Color(0xFFEFF6FF),
                iconColor = Color(0xFF60A5FA),
                modifier = Modifier.weight(1f),
                onClick = onQuickReorderClick
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    bgColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(bgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate800)
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    onSeeAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Text(subtitle, fontSize = 12.sp, color = Slate500)
        }
        TextButton(onClick = onSeeAllClick) {
            Text("See All", color = Brand600, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NewArrivalsRow(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        items(products) { product ->
            NewArrivalCard(
                product = product,
                onClick = { onProductClick(product) },
                onAddToCart = { onAddToCart(product) }
            )
        }
    }
}

@Composable
fun NewArrivalCard(
    product: Product,
    onClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Slate50, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (product.images.isNotEmpty()) {
                    AsyncImage(
                        model = product.images.first(),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Brand50, CircleShape),
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
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                if (product.tag != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        shape = RoundedCornerShape(99.dp),
                        color = Brand500
                    ) {
                        Text(
                            product.tag,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                product.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                "${product.gender} • ${product.category}",
                fontSize = 10.sp,
                color = Slate500,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "KSh ${product.priceKes}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Brand600
                )
                IconButton(
                    onClick = { if (product.inStock) onAddToCart() },
                    modifier = Modifier.size(28.dp),
                    enabled = product.inStock
                ) {
                    Icon(
                        imageVector = if (product.inStock) Icons.Default.AddCircle else Icons.Default.RemoveCircleOutline,
                        contentDescription = "Add",
                        tint = if (product.inStock) Slate900 else Slate300,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProductGrid(
    products: List<Product>,
    favoriteProductIds: Set<String> = emptySet(),
    onFavoriteToggle: (Product) -> Unit = {},
    onAddToCart: (Product) -> Unit = {},
    onProductClick: (Product) -> Unit = {}
) {
    BoxWithConstraints(
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        val columns = when {
            maxWidth < 600.dp -> 2
            maxWidth < 900.dp -> 3
            else -> 4
        }
        
        val chunks = products.chunked(columns)
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            chunks.forEach { rowProducts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowProducts.forEach { product ->
                        ProductCard(
                            product = product,
                            isFavorite = favoriteProductIds.contains(product.id),
                            modifier = Modifier.weight(1f),
                            onFavoriteClick = { onFavoriteToggle(product) },
                            onAddToCart = { onAddToCart(product) },
                            onClick = { onProductClick(product) }
                        )
                    }
                    // Fill empty spaces in the row to maintain alignment
                    if (rowProducts.size < columns) {
                        repeat(columns - rowProducts.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product, 
    isFavorite: Boolean = false,
    modifier: Modifier = Modifier,
    onFavoriteClick: () -> Unit = {},
    onAddToCart: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100),
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Slate50, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (product.images.isNotEmpty()) {
                    AsyncImage(
                        model = product.images.first(),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Brand50, CircleShape),
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
                }
                
                if (product.tag != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        shape = RoundedCornerShape(99.dp),
                        color = Brand500
                    ) {
                        Text(
                            product.tag,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                
                Surface(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(32.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.8f)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        modifier = Modifier
                            .padding(8.dp)
                            .size(14.dp),
                        tint = if (isFavorite) Color(0xFFF43F5E) else Slate300
                    )
                }
                
                if (!product.inStock) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(8.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "OUT OF STOCK",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                "${product.gender} • ${product.category}".uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Brand600,
                letterSpacing = 0.5.sp
            )
            
            Text(
                product.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate900,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(10.dp))
                Text(
                    " ${product.rating} ",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    "(${product.reviewsCount})",
                    fontSize = 10.sp,
                    color = Slate500
                )
            }
            
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                val attrs = if (product.category == "Equipment") {
                    listOf("Professional", "Durable")
                } else if (product.category == "Theatre Shoes") {
                    listOf("Non-slip", "Waterproof")
                } else {
                    listOf("4-Way Stretch", "Antimicrobial")
                }
                attrs.take(2).forEach { attr ->
                    Surface(
                        shape = RoundedCornerShape(4.dp), 
                        color = if (attr == "Antimicrobial" || attr == "Professional" || attr == "Non-slip") Brand50 else Slate100
                    ) {
                        Text(
                            attr, 
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), 
                            fontSize = 8.sp, 
                            fontWeight = FontWeight.Medium, 
                            color = if (attr == "Antimicrobial" || attr == "Professional" || attr == "Non-slip") Brand600 else Slate600
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                }
            }
            
            Spacer(Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "KSh ${product.priceKes}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Brand600
                )
                IconButton(
                    onClick = { if (product.inStock) onAddToCart() },
                    modifier = Modifier.size(32.dp),
                    enabled = product.inStock
                ) {
                    Icon(
                        imageVector = if (product.inStock) Icons.Default.AddCircle else Icons.Default.RemoveCircleOutline,
                        contentDescription = "Add",
                        tint = if (product.inStock) Slate900 else Slate300,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
