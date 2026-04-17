package com.example.nursewearconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.nursewearconnect.ui.screens.*
import com.example.nursewearconnect.ui.theme.*

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NurseWearConnectTheme {
                NurseWearConnectApp(showBiometricPrompt = ::showBiometricPrompt)
            }
        }
    }

    private fun showBiometricPrompt(onSuccess: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

@Composable
fun NurseWearConnectApp(showBiometricPrompt: (() -> Unit) -> Unit) {
    var showSplash by rememberSaveable { mutableStateOf(true) }
    var currentScreen by rememberSaveable { mutableStateOf(Screen.ONBOARDING) }
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    if (showSplash) {
        SplashScreen(onAnimationFinished = { showSplash = false })
    } else {
        when (currentScreen) {
            Screen.ONBOARDING -> {
                OnboardingScreen(
                    onSkip = { currentScreen = Screen.LOGIN },
                    onFinish = { currentScreen = Screen.LOGIN }
                )
            }
            Screen.LOGIN -> {
                LoginScreen(
                    onBack = { currentScreen = Screen.ONBOARDING },
                    onLoginSuccess = { currentScreen = Screen.MAIN },
                    onNavigateToRegister = { currentScreen = Screen.REGISTER },
                    onBiometricSignIn = {
                        showBiometricPrompt {
                            currentScreen = Screen.MAIN
                        }
                    }
                )
            }
            Screen.REGISTER -> {
                RegisterScreen(
                    onBack = { currentScreen = Screen.LOGIN },
                    onRegisterSuccess = { currentScreen = Screen.MAIN },
                    onNavigateToLogin = { currentScreen = Screen.LOGIN }
                )
            }
            Screen.MAIN -> {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NurseBottomNavigation(
                            currentDestination = currentDestination,
                            onDestinationSelected = { currentDestination = it }
                        )
                    },
                    containerColor = Slate50
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentDestination) {
                            AppDestinations.HOME -> HomeScreen(PaddingValues(0.dp))
                            AppDestinations.CATALOG -> CatalogScreen(PaddingValues(0.dp))
                            AppDestinations.CART -> CartScreen(PaddingValues(0.dp))
                            AppDestinations.ORDERS -> OrdersScreen(PaddingValues(0.dp))
                            AppDestinations.PROFILE -> ProfileScreen(PaddingValues(0.dp))
                        }
                    }
                }
            }
        }
    }
}

enum class Screen {
    ONBOARDING, LOGIN, REGISTER, MAIN
}

@Composable
fun NurseBottomNavigation(
    currentDestination: AppDestinations,
    onDestinationSelected: (AppDestinations) -> Unit
) {
    Surface(
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shadowElevation = 16.dp // Matching shadow-[0_-4px_24px_rgba(0,0,0,0.02)] with elevation
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppDestinations.entries.forEach { destination ->
                    if (destination == AppDestinations.CART) {
                        // Empty box to keep space for the FAB
                        Box(modifier = Modifier.width(56.dp))
                    } else {
                        val selected = destination == currentDestination
                        NavItem(
                            destination = destination,
                            selected = selected,
                            onClick = { onDestinationSelected(destination) }
                        )
                    }
                }
            }
            
            // Floating Cart Button (Centered)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-20).dp) // relative -top-5
            ) {
                CartFab(
                    onClick = { onDestinationSelected(AppDestinations.CART) }
                )
            }
        }
    }
}

@Composable
fun NavItem(
    destination: AppDestinations,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint by animateColorAsState(if (selected) Brand600 else Slate400, label = "tint")
    
    Column(
        modifier = Modifier
            .width(56.dp)
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (selected) {
            // Active indicator bar at the TOP
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(4.dp)
                    .background(Brand600, RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                    .align(Alignment.CenterHorizontally)
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.weight(1f))
        
        Icon(
            imageVector = if (selected) destination.filledIcon else destination.outlinedIcon,
            contentDescription = destination.label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = destination.label,
            color = tint,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun CartFab(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp) // w-14 h-14
            .background(Brand600, CircleShape)
            .border(4.dp, Color.White, CircleShape) // border-4 border-white
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        BadgedBox(
            badge = {
                Badge(
                    containerColor = Color(0xFFF43F5E), // rose-500
                    contentColor = Color.White,
                    modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                ) {
                    Text("2")
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.LocalMall,
                contentDescription = "Cart",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

enum class AppDestinations(
    val label: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    CATALOG("Catalog", Icons.Filled.Layers, Icons.Outlined.Layers),
    CART("Cart", Icons.Filled.LocalMall, Icons.Outlined.LocalMall),
    ORDERS("Orders", Icons.Filled.Inventory2, Icons.Outlined.Inventory2),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person),
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    NurseWearConnectTheme {
        HomeScreen(PaddingValues(0.dp))
    }
}
