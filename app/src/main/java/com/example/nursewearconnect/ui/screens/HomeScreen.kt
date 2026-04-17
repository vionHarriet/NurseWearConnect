package com.example.nursewearconnect.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nursewearconnect.model.MOCK_PRODUCTS
import com.example.nursewearconnect.model.Product
import com.example.nursewearconnect.ui.theme.*

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nursewearconnect.ui.viewmodel.HomeViewModel
import com.example.nursewearconnect.ui.viewmodel.HomeUiState

@Composable
fun HomeScreen(
    innerPadding: PaddingValues, 
    userRole: String = "student",
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
            
            CategorySelector(
                categories = uiState.categories, 
                activeCat = uiState.activeCategory
            ) { viewModel.onCategorySelected(it) }
            
            if (userRole == "vendor") {
                VendorStats()
            } else {
                HeroBanner()
            }
            
            QuickActions(userRole)
            
            SectionHeader(
                if (userRole == "vendor") "Your Recent Orders" else "Recommended for You",
                if (userRole == "vendor") "Track your sales performance" else "Based on your sizing profile"
            )
            
            ProductGrid(
                products = uiState.recommendations,
                onFavoriteToggle = { viewModel.toggleFavorite(it.id) },
                onAddToCart = { viewModel.addToCart(it) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


@Composable
fun VendorStats() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard("Total Sales", "KSh 12,450", Brand600, modifier = Modifier.weight(1f))
        StatCard("Active Orders", "8", Color(0xFF3B82F6), modifier = Modifier.weight(1f))
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 12.sp, color = Slate500)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun HomeHeader(
    userRole: String,
    userName: String = "",
    greeting: String = "Good Morning",
    unreadNotificationsCount: Int = 0,
    unreadMessagesCount: Int = 0,
    onNotificationsClick: () -> Unit = {},
    onMessagesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onProfileClick
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = if (userRole == "vendor") Color(0xFFDBEAFE) else Brand100,
                border = BorderStroke(2.dp, Color.White),
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(if (userRole == "vendor") "🏪" else "👩‍⚕️", fontSize = 22.sp)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = greeting,
                    fontSize = 12.sp,
                    color = Slate500,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = userName.ifEmpty { if (userRole == "vendor") "Elite Scrubs Vendor" else "Dr. Sarah Jenkins" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Messages Icon
            HeaderIconButton(
                icon = Icons.Default.ChatBubbleOutline,
                badgeCount = unreadMessagesCount,
                onClick = onMessagesClick
            )

            // Notifications Icon
            HeaderIconButton(
                icon = Icons.Outlined.Notifications,
                badgeCount = unreadNotificationsCount,
                onClick = onNotificationsClick
            )
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
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.8f), CircleShape)
                .border(1.dp, Slate100, CircleShape)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Slate400,
                modifier = Modifier.size(20.dp)
            )
        }
        
        if (badgeCount > 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(18.dp),
                shape = CircleShape,
                color = Brand500,
                border = BorderStroke(2.dp, Color.White)
            ) {
                Text(
                    text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.wrapContentSize()
                )
            }
        }
    }
}



@Composable
fun SearchBar(text: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            placeholder = { Text("Search scrubs, shoes, accessories...", fontSize = 14.sp, color = Slate400) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400, modifier = Modifier.size(20.dp)) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Slate200,
                focusedBorderColor = Brand500,
                cursorColor = Brand500
            ),
            singleLine = true
        )
        IconButton(
            onClick = { /* TODO */ },
            modifier = Modifier
                .size(48.dp)
                .background(Brand50, RoundedCornerShape(16.dp))
        ) {
            Icon(Icons.Default.Tune, contentDescription = "Filter", tint = Brand600, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun CategorySelector(categories: List<String>, activeCat: String, onCatSelected: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { cat ->
            val isActive = cat == activeCat
            Surface(
                modifier = Modifier.clickable { onCatSelected(cat) },
                shape = RoundedCornerShape(99.dp),
                color = if (isActive) Brand600 else Color.White,
                border = if (isActive) null else BorderStroke(1.dp, Slate200),
                shadowElevation = if (isActive) 8.dp else 0.dp
            ) {
                Text(
                    text = cat,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    color = if (isActive) Color.White else Slate600,
                    fontSize = 13.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun HeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .background(Brand50, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
    ) {
        // Decorative blur
        Box(
            modifier = Modifier
                .size(128.dp)
                .offset(x = 240.dp, y = (-40).dp)
                .background(Brand200.copy(alpha = 0.5f), CircleShape)
        )

        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Surface(shape = RoundedCornerShape(6.dp), color = Color.White) {
                Text(
                    "NEW ARRIVAL",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Brand600
                )
            }
            Spacer(Modifier.height(8.dp))
            Text("Premium Flex\nScrub Sets", fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp, color = Slate900)
            Text("4-way stretch & anti-microbial", fontSize = 12.sp, color = Slate500)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Shop Now", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
        
        // Placeholder for hero image
        Text(
            "👩‍⚕️",
            fontSize = 100.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 10.dp, y = 20.dp)
        )
    }
}

@Composable
fun QuickActions(userRole: String) {
    Row(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (userRole == "vendor") {
            QuickActionCard("Inventory", Icons.Default.Inventory2, Brand50, Brand600, modifier = Modifier.weight(1f))
            QuickActionCard("Analytics", Icons.Default.BarChart, Color(0xFFF0F9FF), Color(0xFF0EA5E9), modifier = Modifier.weight(1f))
        } else {
            QuickActionCard("Quick Reorder", Icons.AutoMirrored.Filled.RotateLeft, Brand50, Brand600, modifier = Modifier.weight(1f))
            QuickActionCard("Favorites", Icons.Default.Favorite, Color(0xFFFFF1F2), Color(0xFFF43F5E), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun QuickActionCard(label: String, icon: ImageVector, iconBg: Color, iconColor: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Text(subtitle, fontSize = 13.sp, color = Slate500)
        }
        Text("See All", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Brand600)
    }
}

@Composable
fun ProductGrid(
    products: List<Product>,
    onFavoriteToggle: (Product) -> Unit = {},
    onAddToCart: (Product) -> Unit = {}
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
                            modifier = Modifier.weight(1f),
                            onFavoriteClick = { onFavoriteToggle(product) },
                            onAddToCart = { onAddToCart(product) }
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
    modifier: Modifier = Modifier,
    onFavoriteClick: () -> Unit = {},
    onAddToCart: () -> Unit = {}
) {
    Surface(
        modifier = modifier.clickable { /* Navigate to Product Detail */ },
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
                Text(product.emoji, fontSize = 48.sp)
                
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
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        modifier = Modifier
                            .padding(8.dp)
                            .size(14.dp),
                        tint = Slate300
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                product.category.uppercase(),
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
                Spacer(Modifier.width(4.dp))
                Text(
                    "${product.rating} ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate600
                )
                Text(
                    "(${product.reviewsCount})",
                    fontSize = 11.sp,
                    color = Slate400
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "KSh ${product.priceKes}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                
                Surface(
                    onClick = onAddToCart,
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = Slate900
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp).wrapContentSize())
                }
            }
        }
    }
}
