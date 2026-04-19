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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
fun AdminUserManagementScreen(
    onBackClick: () -> Unit,
    viewModel: HomeViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Students", "Vendors", "Admins")
    val allUsers by viewModel.allUsers.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAdminData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Add new user */ }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add User", tint = Brand600)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search and Filter
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        placeholder = { Text("Search users by name or email...") },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Slate400) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Brand600,
                            unfocusedBorderColor = Slate200
                        ),
                        singleLine = true
                    )
                    
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
                                text = { 
                                    Text(
                                        text = title,
                                        fontSize = 14.sp,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // User List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val filteredUsers = allUsers.filter { 
                    val role = it["role"]?.toString() ?: ""
                    val name = it["name"]?.toString() ?: ""
                    val email = it["email"]?.toString() ?: ""
                    
                    role.lowercase() == tabs[selectedTab].lowercase().removeSuffix("s") &&
                    (name.contains(searchQuery, ignoreCase = true) || email.contains(searchQuery, ignoreCase = true))
                }

                items(filteredUsers) { userMap ->
                    val user = AdminUserItem(
                        id = userMap["id"]?.toString() ?: "",
                        name = userMap["name"]?.toString() ?: "Unknown",
                        email = userMap["email"]?.toString() ?: "",
                        role = userMap["role"]?.toString() ?: "",
                        status = userMap["status"]?.toString() ?: "active",
                        institution = userMap["institution"]?.toString()
                    )
                    UserManagementCard(user)
                }
                
                if (filteredUsers.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                            Text("No users found", color = Slate400)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserManagementCard(user: AdminUserItem) {
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
            // Avatar
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = when(user.status) {
                    "Active" -> Brand50
                    "Pending" -> Color(0xFFFEF3C7)
                    else -> Color(0xFFFEE2E2)
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        user.name.take(1).uppercase(),
                        color = when(user.status) {
                            "Active" -> Brand600
                            "Pending" -> Color(0xFFD97706)
                            else -> Color(0xFFDC2626)
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, fontWeight = FontWeight.Bold, color = Slate900, fontSize = 15.sp)
                Text(user.email, fontSize = 12.sp, color = Slate500)
                
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when(user.status) {
                            "Active" -> Color(0xFFECFDF5)
                            "Pending" -> Color(0xFFFFFBEB)
                            else -> Color(0xFFFEF2F2)
                        }
                    ) {
                        Text(
                            user.status,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when(user.status) {
                                "Active" -> Color(0xFF059669)
                                "Pending" -> Color(0xFFD97706)
                                else -> Color(0xFFDC2626)
                            }
                        )
                    }
                    
                    if (user.institution != null) {
                        Text("•", color = Slate300)
                        Text(user.institution, fontSize = 11.sp, color = Slate400)
                    }
                }
            }

            IconButton(onClick = { /* Actions menu */ }) {
                Icon(Icons.Default.MoreVert, null, tint = Slate400)
            }
        }
    }
}

data class AdminUserItem(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val status: String,
    val institution: String? = null
)
