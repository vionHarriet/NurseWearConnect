package com.example.nursewearconnect.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nursewearconnect.model.MOCK_PRODUCTS
import com.example.nursewearconnect.model.Product
import com.example.nursewearconnect.ui.theme.*

@Composable
fun CatalogScreen(innerPadding: PaddingValues) {
    var searchQuery by remember { mutableStateOf("Core scrub set") }
    var isGridView by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(bottom = innerPadding.calculateBottomPadding())
    ) {
        // Sticky Header
        Surface(
            color = Color.White.copy(alpha = 0.9f),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(top = 48.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconButton(
                            onClick = { },
                            modifier = Modifier.size(40.dp).background(Slate50, CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(20.dp))
                        }
                        Text(
                            text = "Women's Scrubs",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }
                    
                    Row(
                        modifier = Modifier.background(Slate50, RoundedCornerShape(99.dp)).padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { isGridView = true },
                            modifier = Modifier.size(32.dp).background(if (isGridView) Color.White else Color.Transparent, CircleShape)
                        ) {
                            Icon(Icons.Default.GridView, contentDescription = "Grid", tint = if (isGridView) Brand600 else Slate400, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = { isGridView = false },
                            modifier = Modifier.size(32.dp).background(if (!isGridView) Brand600 else Color.Transparent, CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "List", tint = if (!isGridView) Color.White else Slate400, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Search Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f).height(48.dp),
                        placeholder = { Text("Search products...", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        trailingIcon = { if (searchQuery.isNotEmpty()) IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) } },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Slate50,
                            focusedContainerColor = Slate50,
                            unfocusedBorderColor = Slate200
                        ),
                        singleLine = true
                    )
                    
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = Color(0xFFF43F5E),
                                contentColor = Color.White,
                                modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                            ) { Text("3") }
                        }
                    ) {
                        IconButton(
                            onClick = { },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Brand600, RoundedCornerShape(16.dp))
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.White)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Sort by:", fontSize = 12.sp, color = Slate500)
                        Text("Recommended", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                        Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(14.dp), tint = Slate400)
                    }
                    Text("124 Results", fontSize = 12.sp, color = Slate500)
                }
            }
        }

        // Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Brand50,
                    border = BorderStroke(1.dp, Brand100)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("My Size (M)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Brand700)
                    }
                }
            }
            items(listOf("Color: Navy", "4-Way Stretch", "Antimicrobial")) { filter ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Slate900
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(filter, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(12.dp), tint = Slate300)
                    }
                }
            }
        }

        if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(MOCK_PRODUCTS) { product ->
                    ProductCard(product = product)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(MOCK_PRODUCTS) { product ->
                    ProductListItem(product = product)
                }
            }
        }
    }
}

@Composable
fun ProductListItem(product: Product) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 110.dp, height = 130.dp)
                    .background(Slate50, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(product.emoji, fontSize = 48.sp)
                if (product.tag != null) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                        shape = RoundedCornerShape(99.dp),
                        color = Brand500
                    ) {
                        Text(product.tag, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(product.category.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Brand600, letterSpacing = 0.5.sp)
                Text(product.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900, lineHeight = 18.sp)
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    repeat(4) { Icon(Icons.Filled.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(10.dp)) }
                    Icon(Icons.Filled.Star, null, tint = Slate200, modifier = Modifier.size(10.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${product.rating}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Slate600)
                    Text(" (${product.reviewsCount})", fontSize = 11.sp, color = Slate400)
                }

                Row(modifier = Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("4-Way Stretch", "Antimicrobial").forEach { attr ->
                        Surface(shape = RoundedCornerShape(4.dp), color = if (attr == "Antimicrobial") Brand50 else Slate100) {
                            Text(attr, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 9.sp, fontWeight = FontWeight.Medium, color = if (attr == "Antimicrobial") Brand600 else Slate600)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text("KSh ${product.priceKes}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                        Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(12.dp).background(Color(0xFF1E3A8A), CircleShape).border(1.dp, Slate200, CircleShape))
                            Box(modifier = Modifier.size(12.dp).background(Color(0xFF0F766E), CircleShape).border(1.dp, Slate200, CircleShape))
                            Box(modifier = Modifier.size(12.dp).background(Color.Black, CircleShape).border(1.dp, Slate200, CircleShape))
                        }
                    }
                    IconButton(
                        onClick = { },
                        modifier = Modifier.size(36.dp).background(Slate900, CircleShape)
                    ) {
                        Text("+", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
