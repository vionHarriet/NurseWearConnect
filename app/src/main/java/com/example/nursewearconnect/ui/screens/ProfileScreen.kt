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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nursewearconnect.ui.theme.*

@Composable
fun ProfileScreen(innerPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(bottom = innerPadding.calculateBottomPadding())
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ProfileHeader()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                UserSummarySection()
                PersonalInfoSection()
                MeasurementsSection()
                QuickReorderSection()
                AddressesAndFavoritesSection()
                SecuritySettingsSection()
                
                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Slate700)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Out", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate700)
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun ProfileHeader() {
    Surface(
        color = Color.White.copy(alpha = 0.9f),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Slate100)
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Profile & Security", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Slate900)
            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(40.dp)
                    .background(Slate50, CircleShape)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Slate600, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun UserSummarySection() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100),
        shadowElevation = 2.dp
    ) {
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
                        Text("👩‍⚕️", fontSize = 32.sp)
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
                Text("Sarah Jenkins, RN", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Text("sarah.j@example.com", fontSize = 13.sp, color = Slate500)
                Row(modifier = Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Badge(containerColor = Color(0xFFECFDF5), contentColor = Color(0xFF059669)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(10.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Verified", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Badge(containerColor = Slate100, contentColor = Slate600) {
                        Text("ICU Dept", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalInfoSection() {
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
                Text("Edit", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Brand600)
            }
            HorizontalDivider(color = Slate100)
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("FIRST NAME", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Medium)
                        Text("Sarah", fontSize = 14.sp, color = Slate800, fontWeight = FontWeight.Medium)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("LAST NAME", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Medium)
                        Text("Jenkins", fontSize = 14.sp, color = Slate800, fontWeight = FontWeight.Medium)
                    }
                }
                Column {
                    Text("PHONE", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Medium)
                    Text("+254 712 345 678", fontSize = 14.sp, color = Slate800, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun MeasurementsSection() {
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
                Text("Update", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Brand600)
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
                    MeasurementCard("Bust", "36\"", modifier = Modifier.weight(1f))
                    MeasurementCard("Waist", "29\"", modifier = Modifier.weight(1f))
                    MeasurementCard("Hips", "39\"", modifier = Modifier.weight(1f))
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
fun AddressesAndFavoritesSection() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100)
    ) {
        Column {
            ProfileLinkItem(Icons.Default.LocationOn, "Saved Addresses", "Manage delivery locations", Slate100, Slate600)
            HorizontalDivider(color = Slate100)
            ProfileLinkItem(Icons.Default.Favorite, "Favorites", "12 items saved", Color(0xFFFFF1F2), Color(0xFFF43F5E))
            HorizontalDivider(color = Slate100)
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
