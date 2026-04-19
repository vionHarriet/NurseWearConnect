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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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

import com.example.nursewearconnect.ui.viewmodel.HomeViewModel
import com.example.nursewearconnect.ui.viewmodel.LoginViewModel
import com.example.nursewearconnect.ui.viewmodel.RegistrationViewModel
import com.example.nursewearconnect.ui.viewmodel.ViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NurseWearConnectApp(showBiometricPrompt: (() -> Unit) -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as NurseWearApplication
    val viewModelFactory = remember { ViewModelFactory(app) }

    var showSplash by rememberSaveable { mutableStateOf(true) }
    var currentScreen by rememberSaveable { mutableStateOf(Screen.ONBOARDING) }
    var userRole by rememberSaveable { mutableStateOf("student") }
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
                val loginViewModel: LoginViewModel = viewModel(factory = viewModelFactory)
                val loginSuccess by loginViewModel.loginSuccess.collectAsState()
                val loginError by loginViewModel.error.collectAsState()
                val loginLoading by loginViewModel.isLoading.collectAsState()

                LaunchedEffect(loginSuccess) {
                    loginSuccess?.let { role ->
                        userRole = role
                        app.userRepository.initFromCache() // Refresh role in flow
                        currentScreen = Screen.MAIN
                        // After login, we might want to default the destination based on role
                        currentDestination = if (role == "admin") AppDestinations.CATALOG else AppDestinations.HOME
                        loginViewModel.resetLoginState()
                    }
                }

                LoginScreen(
                    onBack = { currentScreen = Screen.ONBOARDING },
                    onLoginSuccess = { email, password ->
                        loginViewModel.login(email, password)
                    },
                    onNavigateToRegister = { currentScreen = Screen.REGISTER },
                    onNavigateToRecovery = { currentScreen = Screen.RECOVERY },
                    onBiometricLogin = {
                        showBiometricPrompt {
                            // In real app, we'd check if token exists
                            userRole = app.authRepository.getUserRole()
                            currentScreen = Screen.MAIN
                        }
                    },
                    isLoading = loginLoading,
                    externalError = loginError
                )
            }
            Screen.REGISTER -> {
                val registrationViewModel: RegistrationViewModel = viewModel(factory = viewModelFactory)
                val registrationSuccess by registrationViewModel.registrationSuccess.collectAsState()
                val registrationError by registrationViewModel.error.collectAsState()
                val registrationLoading by registrationViewModel.isLoading.collectAsState()

                LaunchedEffect(registrationSuccess) {
                    registrationSuccess?.let { role ->
                        if (role == "vendor") {
                            // Vendors stay on the success state which shows VendorAwaitingApproval
                            // Wait for user to click "Back to Login"
                        } else if (role == "admin") {
                            userRole = role
                            currentScreen = Screen.MAIN
                            registrationViewModel.resetRegistrationState()
                        } else {
                            userRole = role
                            currentScreen = Screen.MAIN
                            registrationViewModel.resetRegistrationState()
                        }
                    }
                }

                RegisterScreen(
                    onBack = { currentScreen = Screen.LOGIN },
                    onRegisterSuccess = { role, fullName, email, phone, password, businessName, location, description ->
                        registrationViewModel.register(
                            email = email,
                            password = password,
                            fullName = fullName,
                            phoneNumber = phone,
                            role = role,
                            businessName = businessName,
                            location = location,
                            businessDescription = description
                        )
                    },
                    onNavigateToLogin = { currentScreen = Screen.LOGIN },
                    isLoading = registrationLoading,
                    externalError = registrationError,
                    isExternalSuccess = registrationSuccess != null
                )
            }
            Screen.RECOVERY -> {
                PasswordRecoveryScreen(
                    onBack = { currentScreen = Screen.LOGIN },
                    onSuccess = { currentScreen = Screen.LOGIN }
                )
            }
            Screen.MAIN -> {
                val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
                val homeUiState by homeViewModel.uiState.collectAsState()
                val snackhostState = remember { SnackbarHostState() }

                LaunchedEffect(homeUiState.error) {
                    homeUiState.error?.let {
                        val result = snackhostState.showSnackbar(
                            message = it,
                            actionLabel = "Dismiss",
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed || result == SnackbarResult.Dismissed) {
                            homeViewModel.clearError()
                        }
                    }
                }

                LaunchedEffect(homeUiState.isLoading) {
                    if (!homeUiState.isLoading && app.authRepository.getUserRole() == "student" && homeUiState.userName.isEmpty() && app.securityManager.getToken() == null) {
                        currentScreen = Screen.LOGIN
                    }
                }

                val destinations = remember(homeUiState.userRole) {
                    AppDestinations.entries.filter { 
                        it != AppDestinations.CART || (homeUiState.userRole == "student" || homeUiState.userRole == "professional")
                    }
                }
                
                val pagerState = rememberPagerState(
                    initialPage = destinations.indexOf(currentDestination).coerceAtLeast(0),
                    pageCount = { destinations.size }
                )

                LaunchedEffect(currentDestination) {
                    val targetPage = destinations.indexOf(currentDestination)
                    if (targetPage != -1 && targetPage != pagerState.currentPage) {
                        pagerState.animateScrollToPage(targetPage)
                    }
                }

                LaunchedEffect(pagerState.currentPage) {
                    currentDestination = destinations[pagerState.currentPage]
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackhostState) },
                    bottomBar = {
                        NurseBottomNavigation(
                            userRole = homeUiState.userRole,
                            currentDestination = currentDestination,
                            onDestinationSelected = { currentDestination = it },
                            cartCount = homeUiState.cartCount
                        )
                    },
                    containerColor = Slate50
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                            userScrollEnabled = true,
                            beyondViewportPageCount = 1
                        ) { page ->
                            val destination = destinations[page]
                            when (destination) {
                                AppDestinations.HOME -> HomeScreen(
                                    innerPadding = innerPadding,
                                    userRole = homeUiState.userRole,
                                    onNavigateToNotifications = { currentScreen = Screen.NOTIFICATIONS },
                                    onNavigateToMessages = { currentScreen = Screen.MESSAGES },
                                    onNavigateToProfile = { currentDestination = AppDestinations.PROFILE },
                                    onNavigateToUserLogs = { currentScreen = Screen.USER_LOGS },
                                    onNavigateToAdminUsers = { currentScreen = Screen.ADMIN_USERS },
                                    onNavigateToAdminVendors = { currentScreen = Screen.ADMIN_VENDORS },
                                    onNavigateToAdminInventory = { currentScreen = Screen.ADMIN_INVENTORY },
                                    onNavigateToAdminOrders = { currentScreen = Screen.ADMIN_ORDERS },
                                    onNavigateToAdminMarketing = { currentScreen = Screen.ADMIN_MARKETING },
                                    onNavigateToReports = { currentScreen = Screen.ADMIN_REPORTS },
                                    onNavigateToVendorInventory = { currentScreen = Screen.VENDOR_INVENTORY },
                                    onNavigateToVendorOrders = { currentScreen = Screen.VENDOR_ORDERS },
                                    viewModel = homeViewModel
                                )
                                AppDestinations.CATALOG -> {
                                    if (homeUiState.userRole == "admin") {
                                        LaunchedEffect(Unit) { homeViewModel.loadAdminData() }
                                        ReportsScreen(
                                            innerPadding = innerPadding,
                                            viewModel = homeViewModel,
                                            onNavigateToInventory = { currentScreen = Screen.ADMIN_INVENTORY }
                                        )
                                    } else {
                                        CatalogScreen(
                                            innerPadding = innerPadding,
                                            viewModel = homeViewModel,
                                            onBack = { currentDestination = AppDestinations.HOME }
                                        )
                                    }
                                }
                                AppDestinations.CART -> CartScreen(
                                    innerPadding = innerPadding,
                                    viewModel = homeViewModel,
                                    onNavigateToCatalog = { currentDestination = AppDestinations.CATALOG }
                                )
                                AppDestinations.ORDERS -> OrdersScreen(
                                    innerPadding = innerPadding,
                                    viewModel = homeViewModel,
                                    onNavigateToNotifications = { currentScreen = Screen.NOTIFICATIONS },
                                    onSupportClick = { currentScreen = Screen.MESSAGES }
                                )
                                AppDestinations.PROFILE -> ProfileScreen(innerPadding, homeViewModel)
                            }
                        }

                        // Global Bottom Sheets managed by HomeViewModel
                        if (homeUiState.selectedProduct != null) {
                            val product = homeUiState.selectedProduct!!
                            ModalBottomSheet(
                                onDismissRequest = { homeViewModel.setSelectedProduct(null) },
                                containerColor = Color.White,
                                dragHandle = { BottomSheetDefaults.DragHandle() }
                            ) {
                                ProductDetailContent(
                                    product = product,
                                    isFavorite = homeUiState.favoriteProductIds.contains(product.id),
                                    onFavoriteToggle = { homeViewModel.toggleFavorite(product.id) },
                                    selectedSize = homeUiState.selectedSize,
                                    onSizeSelected = { homeViewModel.setSelectedSize(it) },
                                    selectedColor = homeUiState.selectedColor,
                                    onColorSelected = { homeViewModel.setSelectedColor(it) },
                                    onAddToCart = { 
                                        homeViewModel.addToCart(product)
                                        homeViewModel.setSelectedProduct(null)
                                    }
                                )
                            }
                        }
                    }
                }
            }
            Screen.NOTIFICATIONS -> {
                val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
                NotificationScreen(
                    onBackClick = { currentScreen = Screen.MAIN },
                    viewModel = homeViewModel
                )
            }
            Screen.MESSAGES -> {
                val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
                MessagesScreen(
                    onBackClick = { currentScreen = Screen.MAIN },
                    viewModel = homeViewModel
                )
            }
            Screen.USER_LOGS -> {
                val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
                UserLogsScreen(
                    onBackClick = { currentScreen = Screen.MAIN },
                    viewModel = homeViewModel
                )
            }
            Screen.ADMIN_USERS -> {
                val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
                AdminUserManagementScreen(
                    onBackClick = { currentScreen = Screen.MAIN },
                    viewModel = homeViewModel
                )
            }
            Screen.ADMIN_VENDORS -> {
                val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
                LaunchedEffect(Unit) { homeViewModel.loadAdminData() }
                AdminVendorApprovalsScreen(
                    onBackClick = { currentScreen = Screen.MAIN },
                    viewModel = homeViewModel
                )
            }
            Screen.ADMIN_INVENTORY -> {
                val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
                AdminInventoryScreen(
                    onBackClick = { currentScreen = Screen.MAIN },
                    viewModel = homeViewModel
                )
            }
            Screen.ADMIN_ORDERS -> {
                val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
                AdminOrderManagementScreen(
                    onBackClick = { currentScreen = Screen.MAIN },
                    viewModel = homeViewModel
                )
            }
            Screen.VENDOR_INVENTORY -> {
                val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
                VendorInventoryScreen(
                    onBackClick = { currentScreen = Screen.MAIN },
                    viewModel = homeViewModel
                )
            }
            Screen.VENDOR_ORDERS -> {
                val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
                VendorOrdersScreen(
                    onBackClick = { currentScreen = Screen.MAIN },
                    viewModel = homeViewModel
                )
            }
            Screen.ADMIN_MARKETING -> {
                val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
                LaunchedEffect(Unit) { homeViewModel.loadAdminData() }
                AdminMarketingScreen(
                    onBackClick = { currentScreen = Screen.MAIN },
                    viewModel = homeViewModel
                )
            }
            Screen.ADMIN_REPORTS -> {
                val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Reports", fontWeight = FontWeight.Bold) },
                            navigationIcon = {
                                IconButton(onClick = { currentScreen = Screen.MAIN }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    ReportsScreen(
                        innerPadding = innerPadding,
                        viewModel = homeViewModel,
                        onNavigateToInventory = { currentScreen = Screen.ADMIN_INVENTORY }
                    )
                }
            }
        }
    }
}

enum class Screen {
    ONBOARDING, LOGIN, REGISTER, RECOVERY, MAIN, NOTIFICATIONS, MESSAGES, USER_LOGS, ADMIN_USERS, ADMIN_VENDORS, ADMIN_INVENTORY, ADMIN_ORDERS, VENDOR_INVENTORY, VENDOR_ORDERS, ADMIN_MARKETING, ADMIN_REPORTS
}

@Composable
fun NurseBottomNavigation(
    userRole: String = "student",
    currentDestination: AppDestinations,
    onDestinationSelected: (AppDestinations) -> Unit,
    cartCount: Int = 0
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
                    if (destination == AppDestinations.CART && (userRole == "student" || userRole == "professional")) {
                        // Empty box to keep space for the FAB
                        Box(modifier = Modifier.width(56.dp))
                    } else if (destination == AppDestinations.CART && (userRole != "student" && userRole != "professional")) {
                        // Don't show cart for admin/vendor
                    } else {
                        val selected = destination == currentDestination
                        val label = if (destination == AppDestinations.CATALOG && userRole == "admin") "Reports" else destination.label
                        val filledIcon = if (destination == AppDestinations.CATALOG && userRole == "admin") Icons.Filled.Assessment else destination.filledIcon
                        val outlinedIcon = if (destination == AppDestinations.CATALOG && userRole == "admin") Icons.Outlined.Assessment else destination.outlinedIcon

                        NavItem(
                            label = label,
                            filledIcon = filledIcon,
                            outlinedIcon = outlinedIcon,
                            selected = selected,
                            onClick = { onDestinationSelected(destination) }
                        )
                    }
                }
            }
            
            // Floating Cart Button (Centered) - For students and professionals
            if (userRole == "student" || userRole == "professional") {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-20).dp) // relative -top-5
                ) {
                    CartFab(
                        count = cartCount,
                        onClick = { onDestinationSelected(AppDestinations.CART) }
                    )
                }
            }
        }
    }
}

@Composable
fun NavItem(
    label: String,
    filledIcon: ImageVector,
    outlinedIcon: ImageVector,
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
            imageVector = if (selected) filledIcon else outlinedIcon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            color = tint,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun CartFab(count: Int, onClick: () -> Unit) {
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
                if (count > 0) {
                    Badge(
                        containerColor = Color(0xFFF43F5E), // rose-500
                        contentColor = Color.White,
                        modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                    ) {
                        Text(count.toString())
                    }
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
