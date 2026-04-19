package com.example.nursewearconnect.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nursewearconnect.ui.theme.*
import com.example.nursewearconnect.ui.viewmodel.HomeViewModel
import com.example.nursewearconnect.ui.viewmodel.UserType

@Composable
fun ProfileScreen(innerPadding: PaddingValues, viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val userRole = uiState.userRole
    val userRepository = viewModel.getUserRepository()
    val userProfile by userRepository.userProfile.collectAsState()

    val fullName = userProfile?.get("full_name") as? String ?: uiState.userName
    val email = userProfile?.get("email") as? String ?: ""
    val phoneNumber = userProfile?.get("phone_number") as? String ?: ""
    val businessName = userProfile?.get("business_name") as? String ?: ""
    val location = userProfile?.get("location") as? String ?: ""
    val bio = userProfile?.get("business_description") as? String ?: ""
    val avatarUrl = userProfile?.get("avatar_url") as? String
    
    val measurements = userProfile?.get("measurements") as? Map<*, *>
    val bust = measurements?.get("bust") as? String ?: "0\""
    val waist = measurements?.get("waist") as? String ?: "0\""
    val hips = measurements?.get("hips") as? String ?: "0\""

    var showEditDialog by remember { mutableStateOf(false) }
    var showMeasurementsDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        EditProfileDialog(
            userRole = userRole,
            initialName = fullName,
            initialPhone = phoneNumber,
            initialBusinessName = businessName,
            initialLocation = location,
            onDismiss = { showEditDialog = false },
            onSave = { data ->
                viewModel.updateProfile(data)
                showEditDialog = false
            }
        )
    }

    if (showMeasurementsDialog) {
        EditMeasurementsDialog(
            initialBust = bust,
            initialWaist = waist,
            initialHips = hips,
            onDismiss = { showMeasurementsDialog = false },
            onSave = { data ->
                viewModel.updateProfile(mapOf("measurements" to data))
                showMeasurementsDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Slate50
            .padding(bottom = innerPadding.calculateBottomPadding())
    ) {
        ProfileHeader()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            UserSummarySection(fullName, userRole, uiState.userType, avatarUrl) { viewModel.setUserType(it) }
            
            if (userRole == "vendor") {
                VendorBusinessSection(businessName, location, bio, onEditClick = { showEditDialog = true })
            }
            
            PersonalInfoSection(fullName, email, phoneNumber, onEditClick = { showEditDialog = true })
            
            if (userRole == "student" || userRole == "professional") {
                MeasurementsSection(bust, waist, hips, onEditClick = { showMeasurementsDialog = true })
                QuickReorderSection()
            }
            
            AddressesAndFavoritesSection(userRole)
            SecuritySettingsSection()
            
            Button(
                onClick = { viewModel.logout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)) // Slate200
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color(0xFFF43F5E))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Log Out", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155)) // Slate700
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun VendorBusinessSection(businessName: String, location: String, bio: String, onEditClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Brand100)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Business Profile", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                TextButton(onClick = onEditClick) {
                    Text("Edit", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Brand600)
                }
            }
            HorizontalDivider(color = Slate100)
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("BUSINESS NAME", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Bold)
                Text(businessName.ifEmpty { "Not Set" }, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                
                Text("LOCATION", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Bold)
                Text(location.ifEmpty { "Not Set" }, fontSize = 15.sp, fontWeight = FontWeight.Medium)

                if (bio.isNotEmpty()) {
                    Text("DESCRIPTION", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Bold)
                    Text(bio, fontSize = 14.sp, color = Slate600)
                }
            }
        }
    }
}

@Composable
fun ProfileHeader() {
    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Profile & Security", 
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                ),
                color = Slate900,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = { },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Settings, 
                    contentDescription = "Settings", 
                    tint = Slate900, 
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun UserSummarySection(userName: String, userRole: String, userType: UserType, avatarUrl: String?, onUserTypeChange: (UserType) -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100),
        shadowElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = Slate100,
                        border = BorderStroke(2.dp, Brand100)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (avatarUrl != null) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(if (userRole == "vendor") "🏢" else if (userRole == "admin") "🛡️" else "👩‍⚕️", fontSize = 32.sp)
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = CircleShape,
                        color = Brand600,
                        border = BorderStroke(2.dp, Color.White)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.padding(4.dp))
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(userName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    Text(
                        when (userRole) {
                            "admin" -> "Administrator"
                            "vendor" -> "Uniform Vendor"
                            "professional" -> "Medical Professional"
                            else -> "Student Nurse"
                        }, 
                        fontSize = 13.sp, 
                        color = Slate500
                    )
                    Row(modifier = Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Badge(containerColor = Color(0xFFECFDF5), contentColor = Color(0xFF059669)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(10.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Verified", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (userRole == "student" || userRole == "professional") {
                            Badge(containerColor = Slate100, contentColor = Slate600) {
                                Text("ICU Dept", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                            }
                        }
                    }
                }
            }
            
            if (userRole == "student" || userRole == "professional") {
                HorizontalDivider(color = Slate50)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("ACCOUNT TYPE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate400)
                        Text(
                            if (userType == UserType.STUDENT) "Student (20% Off)" else "Medical Professional (10% Off)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Brand700
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .background(Slate100, RoundedCornerShape(8.dp))
                            .padding(2.dp)
                    ) {
                        val buttonModifier = Modifier.height(32.dp)
                        FilterChip(
                            selected = userType == UserType.PROFESSIONAL,
                            onClick = { onUserTypeChange(UserType.PROFESSIONAL) },
                            label = { Text("Pro", fontSize = 11.sp) },
                            modifier = buttonModifier
                        )
                        FilterChip(
                            selected = userType == UserType.STUDENT,
                            onClick = { onUserTypeChange(UserType.STUDENT) },
                            label = { Text("Student", fontSize = 11.sp) },
                            modifier = buttonModifier
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalInfoSection(fullName: String, email: String, phoneNumber: String, onEditClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Personal Info", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                TextButton(onClick = onEditClick) {
                    Text("Edit", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Brand600)
                }
            }
            HorizontalDivider(color = Slate100)
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("FULL NAME", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Medium)
                        Text(fullName.ifEmpty { "Not Set" }, fontSize = 14.sp, color = Slate800, fontWeight = FontWeight.Medium)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("EMAIL", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Medium)
                        Text(email.ifEmpty { "Not Set" }, fontSize = 14.sp, color = Slate800, fontWeight = FontWeight.Medium)
                    }
                }
                Column {
                    Text("PHONE", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Medium)
                    Text(phoneNumber.ifEmpty { "Not Set" }, fontSize = 14.sp, color = Slate800, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    userRole: String,
    initialName: String,
    initialPhone: String,
    initialBusinessName: String,
    initialLocation: String,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var businessName by remember { mutableStateOf(initialBusinessName) }
    var location by remember { mutableStateOf(initialLocation) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (userRole == "vendor") {
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Business Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Business Location") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val data = mutableMapOf<String, Any>(
                    "full_name" to name,
                    "phone_number" to phone
                )
                if (userRole == "vendor") {
                    data["business_name"] = businessName
                    data["location"] = location
                }
                onSave(data)
            }) {
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
fun EditMeasurementsDialog(
    initialBust: String,
    initialWaist: String,
    initialHips: String,
    onDismiss: () -> Unit,
    onSave: (Map<String, String>) -> Unit
) {
    var bust by remember { mutableStateOf(initialBust) }
    var waist by remember { mutableStateOf(initialWaist) }
    var hips by remember { mutableStateOf(initialHips) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Measurements", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = bust,
                    onValueChange = { bust = it },
                    label = { Text("Bust (inches)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = waist,
                    onValueChange = { waist = it },
                    label = { Text("Waist (inches)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = hips,
                    onValueChange = { hips = it },
                    label = { Text("Hips (inches)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(mapOf("bust" to bust, "waist" to waist, "hips" to hips))
            }) {
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
fun MeasurementsSection(bust: String, waist: String, hips: String, onEditClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Brand100),
        shadowElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Straighten, contentDescription = null, tint = Brand500, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Body Measurements", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                }
                TextButton(onClick = onEditClick) {
                    Text("Update", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Brand600)
                }
            }
            HorizontalDivider(color = Slate100)
            Column(modifier = Modifier.padding(16.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Brand50,
                    border = BorderStroke(1.dp, Brand100),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("RECOMMENDED SIZE", fontSize = 11.sp, color = Brand600, fontWeight = FontWeight.Bold)
                            Text("Medium (M)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate900)
                        }
                        Icon(Icons.Default.Checkroom, contentDescription = null, tint = Brand300, modifier = Modifier.size(32.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MeasurementCard("Bust", bust, modifier = Modifier.weight(1f))
                    MeasurementCard("Waist", waist, modifier = Modifier.weight(1f))
                    MeasurementCard("Hips", hips, modifier = Modifier.weight(1f))
                }
                
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Brand200)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = Brand600, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guided Entry Wizard", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Brand600)
                    }
                }
            }
        }
    }
}

@Composable
fun MeasurementCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Slate50,
        border = BorderStroke(1.dp, Slate100)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate800)
        }
    }
}

@Composable
fun QuickReorderSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Quick Reorder", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Text("View All", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Brand600)
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReorderCard("Classic Navy Set", "🩵", modifier = Modifier.weight(1f))
            ReorderCard("Ceil Blue Top", "💙", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ReorderCard(name: String, emoji: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(Slate50, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 32.sp)
            }
            Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate800, maxLines = 1, modifier = Modifier.padding(top = 8.dp))
            Text("Size M", fontSize = 11.sp, color = Slate500)
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(32.dp),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Brand600)
            ) {
                Text("1-Tap Order", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AddressesAndFavoritesSection(userRole: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100)
    ) {
        Column {
            ProfileLinkItem(Icons.Default.LocationOn, "Saved Addresses", "Manage delivery locations", Slate100, Slate600)
            HorizontalDivider(color = Slate100)
            if (userRole == "student" || userRole == "professional") {
                ProfileLinkItem(Icons.Default.Favorite, "Favorites", "12 items saved", Color(0xFFFFF1F2), Color(0xFFF43F5E))
                HorizontalDivider(color = Slate100)
            }
            ProfileLinkItem(Icons.Default.Star, "My Reviews", "Drafts & published history", Color(0xFFFFFBEB), Color(0xFFF59E0B))
        }
    }
}

@Composable
fun ProfileLinkItem(icon: ImageVector, title: String, subtitle: String, iconBg: Color, iconTint: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = iconBg) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.padding(10.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate800)
            Text(subtitle, fontSize = 12.sp, color = Slate500)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Slate300, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun SecuritySettingsSection() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100)
    ) {
        Column {
            Text("Security & Privacy", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900, modifier = Modifier.padding(16.dp))
            HorizontalDivider(color = Slate100)
            
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Two-Factor Auth (2FA)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate800)
                    Text("Add an extra layer of security", fontSize = 12.sp, color = Slate500)
                }
                Switch(checked = true, onCheckedChange = {})
            }
            
            HorizontalDivider(color = Slate100)
            
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Active Sessions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate800)
                    Text("2 devices", fontSize = 12.sp, color = Slate500)
                }
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Slate50,
                    border = BorderStroke(1.dp, Slate100),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Smartphone, null, tint = Slate400, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("iPhone 13 Pro", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                            Text("Current device • Nairobi", fontSize = 11.sp, color = Color(0xFF059669), fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF1F2)),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("Revoke Other Sessions", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF43F5E))
                }
            }
            
            HorizontalDivider(color = Slate100)
            
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = Slate100) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Slate600, modifier = Modifier.padding(8.dp).size(14.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text("Export My Data", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate800, modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Slate300, modifier = Modifier.size(20.dp))
            }
        }
    }
}
