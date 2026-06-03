package com.example.nursewearconnect.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import java.util.Locale
import com.example.nursewearconnect.model.Product
import com.example.nursewearconnect.model.ProductColor
import com.example.nursewearconnect.ui.theme.*
import com.example.nursewearconnect.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    innerPadding: PaddingValues, 
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
    onNavigateToVendorAnalytics: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSizeQuiz by rememberSaveable { mutableStateOf(false) }

    if (showSizeQuiz) {
        SizeQuizBottomSheet(
            onDismiss = { showSizeQuiz = false },
            onComplete = { size ->
                showSizeQuiz = false
                // Handle size recommendation logic here
            }
        )
    }
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
                unreadMessagesCount = uiState.unreadMessagesCount,
                onNotificationsClick = onNavigateToNotifications,
                onMessagesClick = onNavigateToMessages,
                onProfileClick = onNavigateToProfile
            )
            
            SearchBar(uiState.searchQuery) { viewModel.onSearchQueryChanged(it) }
            
            if (userRole != "admin") {
                CategorySelector(
                    categories = uiState.categories, 
                    activeCat = uiState.activeCategory,
                    onCategorySelected = { viewModel.onCategorySelected(it) }
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            if (userRole == "vendor") {
                val totalRevenue = uiState.vendorOrders.sumOf { (it["total_amount"] as? Number)?.toDouble() ?: 0.0 }
                val lowStockCount = uiState.vendorProducts.count { it.inStock && it.stockCount in 1..5 }
                
                if (uiState.userStatus == "pending" || uiState.userStatus == "rejected") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = if (uiState.userStatus == "pending") Color(0xFFFEF9C3) else Color(0xFFFEE2E2),
                        border = BorderStroke(1.dp, if (uiState.userStatus == "pending") Color(0xFFFDE047) else Color(0xFFFECACA))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (uiState.userStatus == "pending") Icons.Default.Pending else Icons.Default.Block,
                                contentDescription = null,
                                tint = if (uiState.userStatus == "pending") Color(0xFF854D0E) else Color(0xFF991B1B)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (uiState.userStatus == "pending") "Onboarding Pending" else "Account Restricted",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (uiState.userStatus == "pending") Color(0xFF854D0E) else Color(0xFF991B1B)
                                )
                                Text(
                                    text = if (uiState.userStatus == "pending") "We're reviewing your shop. Full access coming soon!" else "Your account has been restricted. Contact support.",
                                    fontSize = 12.sp,
                                    color = if (uiState.userStatus == "pending") Color(0xFFA16207) else Color(0xFFB91C1C)
                                )
                            }
                        }
                    }
                }

                VendorStats(
                    orderCount = uiState.vendorOrders.size,
                    lowStockCount = lowStockCount,
                    totalRevenue = totalRevenue
                )
            } else if (userRole == "admin") {
                val allUsers by viewModel.allUsers.collectAsState()
                AdminStats(
                    userCount = allUsers.size,
                    pendingVendors = uiState.pendingVendors.size,
                    onInventoryClick = { onNavigateToAdminInventory() }
                )
            } else {
                HeroBanner(
                    featuredProduct = uiState.featuredProduct,
                    onShopNowClick = { onNavigateToCatalog() }
                )
                
                SizeFinderCard(onStartQuiz = { showSizeQuiz = true })
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
                onQuickReorderClick = { 
                    if (userRole == "vendor") onNavigateToMessages() else viewModel.setShowQuickReorder(true) 
                },
                onFavoritesClick = { 
                    if (userRole == "vendor") onNavigateToVendorInventory() else viewModel.setShowFavorites(true) 
                },
                onVendorAnalyticsClick = onNavigateToVendorAnalytics,
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
                AdminActivityList(
                    logs = uiState.systemLogs,
                    onSeeAllOrders = onNavigateToAdminOrders
                )
            } else {
                ProductGrid(
                    products = uiState.recommendations,
                    favoriteProductIds = uiState.favoriteProductIds,
                    onFavoriteToggle = { viewModel.toggleFavorite(it.id) },
                    onAddToCart = { viewModel.addToCart(it) },
                    onProductClick = { viewModel.setSelectedProduct(it) },
                    isLoading = uiState.isLoading
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

        // Product Detail Bottom Sheet
        if (uiState.selectedProduct != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.setSelectedProduct(null) },
                containerColor = Color.White,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                ProductDetailContent(
                    product = uiState.selectedProduct!!,
                    isFavorite = uiState.favoriteProductIds.contains(uiState.selectedProduct!!.id),
                    onFavoriteToggle = { viewModel.toggleFavorite(uiState.selectedProduct!!.id) },
                    selectedSize = uiState.selectedSize,
                    onSizeSelected = { viewModel.setSelectedSize(it) },
                    selectedColor = uiState.selectedColor,
                    onColorSelected = { viewModel.setSelectedColor(it) },
                    onAddToCart = { 
                        viewModel.addToCart(uiState.selectedProduct!!)
                        viewModel.setSelectedProduct(null)
                    },
                    reviews = uiState.productReviews,
                    isReviewsLoading = uiState.isReviewsLoading,
                    onSubmitReview = { rating, comment ->
                        viewModel.submitReview(uiState.selectedProduct!!.id, rating, comment)
                    },
                    isAdmin = userRole == "admin",
                    onEditProduct = { product ->
                        // Implementation for navigating to edit
                        viewModel.setSelectedProduct(null)
                        onNavigateToAdminInventory()
                    }
                )
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
    onAddToCart: () -> Unit,
    reviews: List<Map<String, Any>> = emptyList(),
    isReviewsLoading: Boolean = false,
    onSubmitReview: (Int, String) -> Unit = { _, _ -> },
    isAdmin: Boolean = false,
    onEditProduct: (Product) -> Unit = {}
) {
    var showReviewDialog by remember { mutableStateOf(false) }
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

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
                            "Theatre Shoes" -> Icons.Default.IceSkating
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
                    .padding(top = 16.dp, end = 16.dp)
                    .background(Color.White.copy(alpha = 0.8f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFFF43F5E) else Slate300
                )
            }

            if (isAdmin) {
                IconButton(
                    onClick = { onEditProduct(product) },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 16.dp, start = 16.dp)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Product",
                        tint = Brand600
                    )
                }
            }

            // 360 View Placeholder
            Surface(
                onClick = { /* Future: Open 360 viewer */ },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.6f),
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("360° View", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
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
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "KSh ${product.priceKes}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Brand600
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFD1FAE5),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        "PROFESSIONAL: KSh ${(product.priceKes * 0.9).toInt()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF065F46),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
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

        if (!product.vendorName.isNullOrEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(Slate50, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Brand100, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Store, null, tint = Brand600, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Sold by",
                        fontSize = 10.sp,
                        color = Slate500,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        product.vendorName ?: "",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }
                if (product.vendorRating != null) {
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                    Text(
                        " ${product.vendorRating}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700
                    )
                }
            }
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

        // Material & Features
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
        
        Spacer(Modifier.height(32.dp))

        // Reviews Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Customer Reviews", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate900)
            TextButton(onClick = { showReviewDialog = true }) {
                Text("Write Review", color = Brand600, fontWeight = FontWeight.Bold)
            }
        }

        if (isReviewsLoading) {
            Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Brand600)
            }
        } else if (reviews.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .background(Slate50, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.RateReview, null, tint = Slate300, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No reviews yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        "Be the first to review this product!",
                        fontSize = 14.sp,
                        color = Slate500,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Rating Summary
            val ratingCounts = IntArray(5)
            reviews.forEach {
                val r = (it["rating"] as? Number)?.toInt() ?: 5
                if (r in 1..5) ratingCounts[r - 1]++
            }
            val totalReviews = reviews.size
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate50.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "%.1f".format(product.rating),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Slate900
                        )
                        Row {
                            repeat(5) { index ->
                                Icon(
                                    Icons.Default.Star,
                                    null,
                                    modifier = Modifier.size(12.dp),
                                    tint = if (index < product.rating.toInt()) Color(0xFFF59E0B) else Slate200
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("${product.reviewsCount} reviews", fontSize = 10.sp, color = Slate500)
                    }
                    
                    VerticalDivider(modifier = Modifier.height(80.dp).padding(horizontal = 16.dp), color = Slate200)
                    
                    Column(modifier = Modifier.weight(1f)) {
                        (5 downTo 1).forEach { star ->
                            val count = ratingCounts[star - 1]
                            val progress = if (totalReviews > 0) count.toFloat() / totalReviews else 0f
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 1.dp)
                            ) {
                                Text(star.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate700, modifier = Modifier.width(10.dp))
                                Spacer(Modifier.width(8.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = Color(0xFFF59E0B),
                                    trackColor = Slate200,
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(count.toString(), fontSize = 10.sp, color = Slate500, modifier = Modifier.width(20.dp), textAlign = TextAlign.End)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            reviews.forEach { review ->
                val customerName = (review["profiles"] as? Map<*, *>)?.get("full_name")?.toString() ?: "Customer"
                val reviewRating = (review["rating"] as? Number)?.toInt() ?: 5
                val reviewComment = review["comment"]?.toString() ?: ""
                val createdAt = review["created_at"]?.toString()?.split("T")?.firstOrNull() ?: "Recently"
                
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Brand100, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    customerName.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = Brand700,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(customerName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                                Row {
                                    repeat(5) { index ->
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(10.dp),
                                            tint = if (index < reviewRating) Color(0xFFF59E0B) else Slate200
                                        )
                                    }
                                }
                            }
                        }
                        Text(createdAt, fontSize = 12.sp, color = Slate400)
                    }
                    if (reviewComment.isNotEmpty()) {
                        Text(
                            reviewComment,
                            fontSize = 14.sp,
                            color = Slate600,
                            modifier = Modifier.padding(top = 8.dp, start = 48.dp),
                            lineHeight = 20.sp
                        )
                    }
                    HorizontalDivider(color = Slate50, modifier = Modifier.padding(top = 16.dp))
                }
            }
        }

        Spacer(Modifier.height(40.dp))
    }

    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Write a Review") },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(5) { index ->
                            IconButton(onClick = { rating = index + 1 }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (index < rating) Color(0xFFF59E0B) else Slate200,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Your comment (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSubmitReview(rating, comment)
                        showReviewDialog = false
                        comment = ""
                        rating = 5
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Brand600)
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
fun VendorStats(
    orderCount: Int = 0,
    lowStockCount: Int = 0,
    totalRevenue: Double = 0.0,
    rating: Double = 0.0
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(Brand600, RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Text("Your Shop Performance", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
        Text("KSh ${String.format(Locale.US, "%,.0f", totalRevenue)}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        
        Spacer(Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatItem("Active Orders", orderCount.toString(), Icons.Default.Inventory)
            StatItem("Low Stock", lowStockCount.toString(), Icons.Default.Warning)
            StatItem("Reviews", if (rating > 0) rating.toString() else "N/A", Icons.Default.Star)
        }
    }
}

@Composable
fun AdminStats(
    userCount: Int = 0,
    pendingVendors: Int = 0,
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
            StatItem("Total Users", userCount.toString(), Icons.Default.People)
            StatItem("Pending Vendors", pendingVendors.toString(), Icons.Default.PendingActions)
            StatItem("Revenue (M)", "2.4", Icons.Default.Payments)
        }
    }
}

@Composable
fun AdminActivityList(
    logs: List<Map<String, Any>> = emptyList(),
    onSeeAllOrders: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (logs.isEmpty()) {
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
        } else {
            logs.take(5).forEach { log ->
                val action = log["action"] as? String ?: "System Action"
                val details = log["details"] as? String ?: ""
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
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
                                imageVector = when {
                                    action.contains("Login", true) -> Icons.Default.Login
                                    action.contains("Order", true) -> Icons.Default.ShoppingCart
                                    action.contains("Vendor", true) -> Icons.Default.Store
                                    else -> Icons.Default.History
                                },
                                contentDescription = null,
                                tint = Brand600,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(action, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Text(details, fontSize = 12.sp, color = Slate500, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SizeQuizBottomSheet(onDismiss: () -> Unit, onComplete: (String) -> Unit) {
    var step by remember { mutableStateOf(1) }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var fitPreference by remember { mutableStateOf("Regular") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Step $step of 3",
                style = MaterialTheme.typography.labelMedium,
                color = Brand600
            )
            
            Spacer(Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { step / 3f },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = Brand600,
                trackColor = Brand100
            )
            
            Spacer(Modifier.height(24.dp))
            
            when (step) {
                1 -> {
                    Text("What is your height?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("Height (cm)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                2 -> {
                    Text("What is your weight?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Weight (kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                3 -> {
                    Text("Preferred Fit?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        listOf("Slim", "Regular", "Loose").forEach { fit ->
                            FilterChip(
                                selected = fitPreference == fit,
                                onClick = { fitPreference = fit },
                                label = { Text(fit) }
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            Button(
                onClick = {
                    if (step < 3) step++ else onComplete("M") // Mock logic
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Brand600)
            ) {
                Text(if (step < 3) "Next" else "Find My Size")
            }
        }
    }
}

@Composable
fun SizeFinderCard(onStartQuiz: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Brand100),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Brand50, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Straighten, null, tint = Brand600, modifier = Modifier.size(28.dp))
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text("Find Your Perfect Fit", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Text("Take our 30-second size quiz", fontSize = 12.sp, color = Slate500)
            }
            
            Button(
                onClick = onStartQuiz,
                colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Start", fontWeight = FontWeight.Bold)
            }
        }
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
    onVendorAnalyticsClick: () -> Unit = {},
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
                modifier = if (userRole == "vendor") Modifier.width(100.dp) else Modifier.weight(1f),
                onClick = onQuickReorderClick
            )
            if (userRole == "vendor") {
                QuickActionCard(
                    title = "Analytics",
                    icon = Icons.Default.BarChart,
                    bgColor = Color(0xFFF5F3FF),
                    iconColor = Color(0xFF8B5CF6),
                    modifier = Modifier.width(100.dp),
                    onClick = onVendorAnalyticsClick
                )
            }
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
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp)
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            Slate100,
            Slate200,
            Slate100
        ),
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

@Composable
fun ProductGrid(
    products: List<Product>,
    favoriteProductIds: Set<String> = emptySet(),
    onFavoriteToggle: (Product) -> Unit = {},
    onAddToCart: (Product) -> Unit = {},
    onProductClick: (Product) -> Unit = {},
    isLoading: Boolean = false
) {
    BoxWithConstraints(
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        val columns = when {
            maxWidth < 600.dp -> 2
            maxWidth < 900.dp -> 3
            else -> 4
        }
        
        if (isLoading && products.isEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(3) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        repeat(columns) {
                            ShimmerPlaceholder(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(240.dp)
                            )
                        }
                    }
                }
            }
        } else {
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
                
                if (product.inStock && product.reviewsCount < 5) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = Brand50,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "LOW STOCK",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Brand600
                        )
                    }
                }
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
                Column {
                    Text(
                        "KSh ${product.priceKes}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Brand600
                    )
                    Text(
                        "Pro: KSh ${(product.priceKes * 0.9).toInt()}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669)
                    )
                }
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
