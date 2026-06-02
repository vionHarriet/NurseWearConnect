package com.example.nursewearconnect.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import java.io.InputStream
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nursewearconnect.ui.theme.*
import com.example.nursewearconnect.ui.viewmodel.HomeViewModel
import com.example.nursewearconnect.ui.viewmodel.UserType
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(innerPadding: PaddingValues, viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val userRepository = viewModel.getUserRepository()
    val userProfile by userRepository.userProfile.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val inputStream: InputStream? = context.contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            if (bytes != null) {
                viewModel.uploadAvatar(bytes)
            }
        }
    }

    // Extracting data safely from the profile map
    val fullName = userProfile?.get("full_name") as? String ?: uiState.userName
    val email = userProfile?.get("email") as? String ?: ""
    val phoneNumber = userProfile?.get("phone_number") as? String ?: ""
    val businessName = userProfile?.get("business_name") as? String ?: ""
    val location = userProfile?.get("location") as? String ?: ""
    val address = userProfile?.get("address") as? String ?: ""
    val bio = userProfile?.get("bio") as? String ?: userProfile?.get("business_description") as? String ?: ""
    val avatarUrl = userProfile?.get("avatar_url") as? String
    
    // Handle JSONB measurements safely
    val measurements = userProfile?.get("measurements") as? Map<*, *>
    val bust = measurements?.get("bust")?.toString() ?: "0\""
    val waist = measurements?.get("waist")?.toString() ?: "0\""
    val hips = measurements?.get("hips")?.toString() ?: "0\""

    var showEditDialog by remember { mutableStateOf(false) }
    var showMeasurementsDialog by remember { mutableStateOf(false) }
    var showPhotoPreview by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    // Dialogs
    if (showPhotoPreview && !avatarUrl.isNullOrEmpty()) {
        PhotoPreviewDialog(
            avatarUrl = avatarUrl,
            onDismiss = { showPhotoPreview = false },
            onChangePhoto = {
                showPhotoPreview = false
                imagePickerLauncher.launch("image/*")
            }
        )
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Log Out") },
            text = { Text("Are you sure you want to log out of your account? You will need to sign in again to access your profile.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E))
                ) {
                    Text("Log Out", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditDialog) {
        EditProfileDialog(
            userRole = uiState.userRole,
            initialName = fullName,
            initialPhone = phoneNumber,
            initialAddress = address,
            initialBusinessName = businessName,
            initialLocation = location,
            initialBio = bio,
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate50)
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            ProfileHeader(isLoading = uiState.isLoading)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                UserSummarySection(
                    userName = fullName, 
                    userRole = uiState.userRole, 
                    userType = uiState.userType, 
                    avatarUrl = avatarUrl,
                    onAvatarClick = { 
                        if (!avatarUrl.isNullOrEmpty()) {
                            showPhotoPreview = true
                        } else {
                            imagePickerLauncher.launch("image/*")
                        }
                    },
                    onUserTypeChange = { viewModel.setUserType(it) }
                )
                
                if (uiState.userRole == "vendor") {
                    VendorBusinessSection(businessName, location, bio, onEditClick = { showEditDialog = true })
                }
                
                PersonalInfoSection(fullName, email, phoneNumber, address, onEditClick = { showEditDialog = true })
                
                if (uiState.userRole == "admin") {
                    AdminQuickActionsSection(
                        onApproveVendors = { 
                            // In a real app, use a NavController. Here we'll trigger a callback if needed
                            // For this context, we assume navigation is handled via state or specific triggers
                        },
                        onViewLogs = {
                            // Example trigger for logs
                        }
                    )
                }
                
                if (uiState.userRole == "student" || uiState.userRole == "professional") {
                    MeasurementsSection(bust, waist, hips, onEditClick = { showMeasurementsDialog = true })
                    QuickReorderSection()
                }
                
                AddressesAndFavoritesSection(uiState.userRole)
                SecuritySettingsSection(
                    biometricEnabled = uiState.biometricEnabled,
                    onBiometricToggle = { viewModel.setBiometricEnabled(it) },
                    activeSessions = uiState.activeSessions,
                    onRevokeSession = { viewModel.revokeSession(it) }
                )
                NotificationsSection(
                    enabled = uiState.notificationsEnabled,
                    onToggle = { viewModel.setNotificationsEnabled(it) }
                )
                
                Button(
                    onClick = { showLogoutConfirm = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Slate200),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color(0xFFF43F5E))
                    Spacer(Modifier.width(12.dp))
                    Text("Log Out", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate700)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Error Feedback
        uiState.error?.let { err ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                action = { TextButton(onClick = { viewModel.clearError() }) { Text("Dismiss", color = Color.White) } },
                containerColor = Color(0xFFF43F5E)
            ) { Text(err) }
        }

        // Loading Overlay
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Brand600)
            }
        }
    }
}

@Composable
fun ProfileHeader(isLoading: Boolean) {
    Surface(color = Color.White, shadowElevation = 1.dp) {
        Row(
            modifier = Modifier.statusBarsPadding().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Profile & Security", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Slate900, modifier = Modifier.weight(1f))
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Brand600)
            } else {
                Icon(Icons.Default.Settings, null, tint = Slate900)
            }
        }
    }
}

@Composable
fun UserSummarySection(userName: String, userRole: String, userType: UserType, avatarUrl: String?, onAvatarClick: () -> Unit, onUserTypeChange: (UserType) -> Unit) {
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
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.clickable { onAvatarClick() }
                ) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = Slate100,
                        border = BorderStroke(2.dp, Brand100)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (!avatarUrl.isNullOrEmpty()) {
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
                        Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.padding(4.dp))
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
                }
            }
            
            if (userRole == "student" || userRole == "professional") {
                HorizontalDivider(color = Slate50)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("ACCOUNT TYPE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate400)
                        Text(
                            if (userType == UserType.STUDENT) "Student (20% Off)" else "Medical Professional (10% Off)",
                            fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Brand700
                        )
                    }
                    Row(modifier = Modifier.background(Slate100, RoundedCornerShape(8.dp)).padding(2.dp)) {
                        FilterChip(
                            selected = userType == UserType.PROFESSIONAL,
                            onClick = { onUserTypeChange(UserType.PROFESSIONAL) },
                            label = { Text("Pro", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = userType == UserType.STUDENT,
                            onClick = { onUserTypeChange(UserType.STUDENT) },
                            label = { Text("Student", fontSize = 11.sp) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalInfoSection(fullName: String, email: String, phoneNumber: String, address: String, onEditClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Slate100)) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Personal Info", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                TextButton(onClick = onEditClick) { Text("Edit", color = Brand600) }
            }
            HorizontalDivider(color = Slate100)
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoRow("FULL NAME", fullName)
                InfoRow("EMAIL", email)
                InfoRow("PHONE", phoneNumber)
                InfoRow("DELIVERY ADDRESS", address)
            }
        }
    }
}

@Composable
fun AdminQuickActionsSection(onApproveVendors: () -> Unit, onViewLogs: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Brand100)) {
        Column {
            Text("Admin Quick Actions", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900, modifier = Modifier.padding(16.dp))
            HorizontalDivider(color = Slate100)
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminActionCard("Approvals", "✓", Brand50, Brand600, Modifier.weight(1f), onClick = onApproveVendors)
                AdminActionCard("System Logs", "📋", Slate50, Slate600, Modifier.weight(1f), onClick = onViewLogs)
            }
        }
    }
}

@Composable
fun AdminActionCard(label: String, icon: String, bg: Color, tint: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, bg.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 20.sp)
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tint)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Medium)
        Text(value.ifEmpty { "Not Set" }, fontSize = 14.sp, color = Slate800, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun VendorBusinessSection(name: String, location: String, bio: String, onEditClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Brand100)) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Business Profile", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                TextButton(onClick = onEditClick) { Text("Edit", color = Brand600) }
            }
            HorizontalDivider(color = Slate100)
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoRow("BUSINESS NAME", name)
                InfoRow("LOCATION", location)
                if (bio.isNotEmpty()) InfoRow("DESCRIPTION", bio)
            }
        }
    }
}

@Composable
fun MeasurementsSection(bust: String, waist: String, hips: String, onEditClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Brand100)) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Straighten, null, tint = Brand500, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Body Measurements", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                }
                TextButton(onClick = onEditClick) { Text("Update", color = Brand600) }
            }
            HorizontalDivider(color = Slate100)
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MeasurementCard("Bust", bust, Modifier.weight(1f))
                MeasurementCard("Waist", waist, Modifier.weight(1f))
                MeasurementCard("Hips", hips, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MeasurementCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp), color = Slate50, border = BorderStroke(1.dp, Slate100)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 11.sp, color = Slate400)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate800)
        }
    }
}

@Composable
fun QuickReorderSection() {
    Column {
        Text("Quick Reorder", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900, modifier = Modifier.padding(bottom = 12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReorderCard("Classic Navy Set", "🩵", Modifier.weight(1f))
            ReorderCard("Ceil Blue Top", "💙", Modifier.weight(1f))
        }
    }
}

@Composable
fun ReorderCard(name: String, emoji: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Slate100)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(Slate50, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = 32.sp)
            }
            Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate800, maxLines = 1, modifier = Modifier.padding(top = 8.dp))
            Button(onClick = { }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(32.dp), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(0.dp)) {
                Text("Order Again", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun AddressesAndFavoritesSection(userRole: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Slate100)) {
        Column {
            ProfileLinkItem(Icons.Default.LocationOn, "Saved Addresses", "Manage delivery locations", Slate100, Slate600)
            HorizontalDivider(color = Slate100)
            if (userRole != "vendor") {
                ProfileLinkItem(Icons.Default.Favorite, "Favorites", "Your saved items", Color(0xFFFFF1F2), Color(0xFFF43F5E))
                HorizontalDivider(color = Slate100)
            }
            ProfileLinkItem(Icons.Default.Star, "My Reviews", "History of your feedback", Color(0xFFFFFBEB), Color(0xFFF59E0B))
        }
    }
}

@Composable
fun ProfileLinkItem(icon: ImageVector, title: String, subtitle: String, iconBg: Color, iconTint: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
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
fun SecuritySettingsSection(
    biometricEnabled: Boolean,
    onBiometricToggle: (Boolean) -> Unit,
    activeSessions: List<Map<String, Any>>,
    onRevokeSession: (String) -> Unit
) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Slate100)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Security & Privacy", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Spacer(Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Biometric Login", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Unlock using fingerprint or face", fontSize = 12.sp, color = Slate500)
                }
                Switch(checked = biometricEnabled, onCheckedChange = onBiometricToggle)
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate50)

            Text("Active Sessions", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate700)
            if (activeSessions.isEmpty()) {
                Text("No other active sessions", fontSize = 12.sp, color = Slate400, modifier = Modifier.padding(top = 8.dp))
            } else {
                activeSessions.forEach { session ->
                    val sessionId = session["id"]?.toString() ?: ""
                    val device = session["device_name"]?.toString() ?: "Unknown Device"
                    val location = session["location"]?.toString() ?: "Unknown Location"
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(device, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(location, fontSize = 11.sp, color = Slate500)
                        }
                        TextButton(onClick = { onRevokeSession(sessionId) }) {
                            Text("Revoke", color = Color(0xFFF43F5E), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationsSection(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Slate100)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Notifications", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Push Notifications", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Receive alerts for orders and updates", fontSize = 12.sp, color = Slate500)
                }
                Switch(checked = enabled, onCheckedChange = onToggle)
            }
        }
    }
}

@Composable
fun PhotoPreviewDialog(avatarUrl: String, onDismiss: () -> Unit, onChangePhoto: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Profile Photo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Slate900)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Slate500)
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Slate100)
                ) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Profile Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Spacer(Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onChangePhoto,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp), tint = Slate700)
                        Spacer(Modifier.width(8.dp))
                        Text("Change", color = Slate700)
                    }
                    
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Brand600)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun EditProfileDialog(userRole: String, initialName: String, initialPhone: String, initialAddress: String, initialBusinessName: String, initialLocation: String, initialBio: String, onDismiss: () -> Unit, onSave: (Map<String, Any>) -> Unit) {
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var address by remember { mutableStateOf(initialAddress) }
    var busName by remember { mutableStateOf(initialBusinessName) }
    var loc by remember { mutableStateOf(initialLocation) }
    var bioText by remember { mutableStateOf(initialBio) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Delivery Address") }, modifier = Modifier.fillMaxWidth())
                
                if (userRole == "vendor") {
                    OutlinedTextField(value = busName, onValueChange = { busName = it }, label = { Text("Business Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = loc, onValueChange = { loc = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = bioText, onValueChange = { bioText = it }, label = { Text("Bio / Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val data = mutableMapOf<String, Any>(
                    "full_name" to name, 
                    "phone_number" to phone,
                    "address" to address
                )
                if (userRole == "vendor") {
                    data["business_name"] = busName
                    data["location"] = loc
                    data["bio"] = bioText
                    data["business_description"] = bioText // Keep both in sync for compatibility
                }
                onSave(data)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EditMeasurementsDialog(initialBust: String, initialWaist: String, initialHips: String, onDismiss: () -> Unit, onSave: (Map<String, String>) -> Unit) {
    var bust by remember { mutableStateOf(initialBust) }
    var waist by remember { mutableStateOf(initialWaist) }
    var hips by remember { mutableStateOf(initialHips) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Measurements") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = bust, onValueChange = { bust = it }, label = { Text("Bust") })
                OutlinedTextField(value = waist, onValueChange = { waist = it }, label = { Text("Waist") })
                OutlinedTextField(value = hips, onValueChange = { hips = it }, label = { Text("Hips") })
            }
        },
        confirmButton = {
            Button(onClick = { onSave(mapOf("bust" to bust, "waist" to waist, "hips" to hips)) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
