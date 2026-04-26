package com.example.nursewearconnect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nursewearconnect.data.repository.CartRepository
import com.example.nursewearconnect.data.repository.OrderRepository
import com.example.nursewearconnect.data.repository.OrderResult
import com.example.nursewearconnect.data.repository.PaymentRepository
import com.example.nursewearconnect.data.repository.PaymentStatus
import com.example.nursewearconnect.data.repository.ProductRepository
import com.example.nursewearconnect.data.repository.UserRepository
import com.example.nursewearconnect.model.Product
import com.example.nursewearconnect.model.ProductColor
import com.example.nursewearconnect.utils.AppUtils
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.Job
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val userName: String = "",
    val greeting: String = "Good Morning",
    val unreadNotificationsCount: Int = 0,
    val unreadMessagesCount: Int = 0,
    val categories: List<String> = listOf("All", "Scrubs", "Jackets", "Shoes", "Accessories"),
    val activeCategory: String = "All",
    val searchQuery: String = "",
    val featuredProduct: Product? = null,
    val newArrivals: List<Product> = emptyList(),
    val recommendations: List<Product> = emptyList(),
    val favoriteProductIds: Set<String> = emptySet(),
    val reorderItems: List<Product> = emptyList(),
    val cartItems: List<CartItem> = emptyList(),
    val cartCount: Int = 0,
    val isLoading: Boolean = true,
    val showQuickReorder: Boolean = false,
    val showFavorites: Boolean = false,
    val selectedProduct: Product? = null,
    val selectedSize: String? = null,
    val selectedColor: ProductColor? = null,
    val catalogSearchQuery: String = "",
    val catalogSelectedCategory: String = "All",
    val catalogSelectedSubCategory: String? = null,
    val catalogSelectedGender: String = "All",
    val catalogSortOption: CatalogSortOption = CatalogSortOption.NEWEST,
    val catalogMinPrice: Float = 0f,
    val catalogMaxPrice: Float = 20000f,
    val catalogSelectedSizes: Set<String> = emptySet(),
    val catalogSelectedMaterials: Set<String> = emptySet(),
    val userType: UserType = UserType.PROFESSIONAL,
    val userRole: String = "student",
    val userStatus: String = "active",
    val statusNotes: String? = null,
    val products: List<Product> = emptyList(),
    val vendorProducts: List<Product> = emptyList(),
    val vendorOrders: List<Map<String, Any>> = emptyList(),
    val allOrders: List<Map<String, Any>> = emptyList(),
    val pendingVendors: List<Map<String, Any>> = emptyList(),
    val coupons: List<Map<String, Any>> = emptyList(),
    val banners: List<Map<String, Any>> = emptyList(),
    val systemLogs: List<Map<String, Any>> = emptyList(),
    val notifications: List<Map<String, Any>> = emptyList(),
    val messages: List<Map<String, Any>> = emptyList(),
    val productReviews: List<Map<String, Any>> = emptyList(),
    val isReviewsLoading: Boolean = false,
    val activeSessions: List<Map<String, Any>> = emptyList(),
    val biometricEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val orderId: String? = null,
    val checkoutError: String? = null,
    val checkoutLoading: Boolean = false,
    val paymentStatus: String? = null,
    val error: String? = null,
    val successMessage: String? = null
)

enum class UserType {
    STUDENT, PROFESSIONAL
}

enum class CatalogSortOption {
    PRICE_LOW_HIGH, PRICE_HIGH_LOW, RATING, NEWEST
}

class HomeViewModel(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository,
    private val userRepository: UserRepository,
    private val vendorRepository: com.example.nursewearconnect.data.repository.VendorRepository,
    private val adminRepository: com.example.nursewearconnect.data.repository.AdminRepository,
    private val authRepository: com.example.nursewearconnect.data.repository.AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _allUsers = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val allUsers: StateFlow<List<Map<String, Any>>> = _allUsers

    private var searchJob: Job? = null

    init {
        observeProducts()
        observeCategories()
        observeCart()
        observeUserProfile()
        loadHomeData()
        startRealtimeUpdates()
        setupSearchDebounce()
    }

    private fun setupSearchDebounce() {
        viewModelScope.launch {
            _uiState
                .map { state -> state.searchQuery }
                .distinctUntilChanged()
                .debounce(500)
                .collectLatest { query ->
                    searchCatalog(query)
                }
        }
    }

    private fun startRealtimeUpdates() {
        val userId = userRepository.getUserId() ?: return
        
        // Listen for message changes
        viewModelScope.launch {
            userRepository.getMessagesRealtime(userId).collect { action ->
                // Reload messages on any change to keep it simple and consistent
                val messages = userRepository.getMessages(userId)
                _uiState.update { it.copy(
                    messages = messages,
                    unreadMessagesCount = messages.count { m -> !(m["isRead"] as? Boolean ?: true) }
                ) }
            }
        }

        // Listen for notification changes
        viewModelScope.launch {
            userRepository.getNotificationsRealtime(userId).collect { action ->
                val notifications = userRepository.getNotifications(userId)
                _uiState.update { it.copy(
                    notifications = notifications,
                    unreadNotificationsCount = notifications.count { n -> !(n["isRead"] as? Boolean ?: true) }
                ) }
            }
        }
    }

    private fun observeProducts() {
        viewModelScope.launch {
            productRepository.products.collectLatest { products ->
                _uiState.update { it.copy(
                    products = products,
                    featuredProduct = products.find { p -> p.tag == "Featured" || p.tag == "NEW" },
                    newArrivals = products.filter { p -> p.tag == "NEW" || p.tag == "New Arrival" },
                    recommendations = products.filter { p -> p.tag == "Recommended" }.ifEmpty { products.take(4) },
                    reorderItems = products.takeLast(2)
                ) }
            }
        }
    }

    private fun observeCategories() {
        viewModelScope.launch {
            productRepository.categories.collectLatest { categories ->
                if (categories.isNotEmpty()) {
                    _uiState.update { it.copy(
                        categories = listOf("All") + categories.map { it.name }
                    ) }
                }
            }
        }
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            userRepository.userProfile.collectLatest { profile ->
                profile?.let {
                    val newRole = (it["role"] as? String ?: "student").lowercase()
                    val status = (it["status"] as? String ?: "active").lowercase()
                    val notes = it["status_notes"] as? String
                    val currentRole = _uiState.value.userRole
                    val name = it["full_name"] as? String ?: ""
                    
                    _uiState.update { state ->
                        state.copy(
                            userName = name,
                            userRole = newRole,
                            userStatus = status,
                            statusNotes = notes,
                            // Ensure userType (discount logic) is synced with role
                            userType = if (newRole == "student") UserType.STUDENT else UserType.PROFESSIONAL
                        )
                    }

                    // Trigger data reload if role changed (e.g., promoted to admin)
                    if (newRole != currentRole && currentRole.isNotEmpty()) {
                        when (newRole) {
                            "admin" -> loadAdminData()
                            "vendor" -> userRepository.getUserId()?.let { id -> loadVendorData(id) }
                        }
                    }
                }
            }
        }
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.cartItems.collectLatest { items ->
                _uiState.value = _uiState.value.copy(
                    cartItems = items,
                    cartCount = items.sumOf { it.quantity }
                )
            }
        }
    }

    private fun getGreeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..4 -> "Good Midnight"
            in 5..10 -> "Good Morning"
            in 11..12 -> "Good Mid-morning"
            in 13..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, greeting = getGreeting()) }
            
            // 1. Refresh Products
            val productsResult = productRepository.refreshProducts()
            if (productsResult.isFailure) {
                _uiState.update { it.copy(error = "Offline mode: Failed to refresh products.") }
            }
            
            // 1.5 Refresh Categories
            productRepository.getCategories()

            val userId = userRepository.getUserId()
            if (userId != null && userId != "demo_user") {
                userRepository.fetchProfile(userId)
                    .onFailure { error ->
                        _uiState.update { it.copy(error = "Profile sync error: ${AppUtils.mapThrowable(error)}") }
                    }

                // 3. Load other user data
                val notificationsResult = userRepository.getNotifications(userId)
                val messagesResult = userRepository.getMessages(userId)
                val userOrdersResult = orderRepository.getUserOrders("eq.$userId")
                val sessions = userRepository.getActiveSessions(userId)
                
                val orders = userOrdersResult.getOrDefault(emptyList()).map { map ->
                    val profiles = map["profiles"] as? Map<*, *>
                    val customerName = profiles?.get("full_name")?.toString() ?: "Customer"
                    map + ("customer_name" to customerName)
                }

                _uiState.update { it.copy(
                    notifications = notificationsResult,
                    messages = messagesResult,
                    allOrders = orders,
                    activeSessions = sessions,
                    biometricEnabled = userRepository.isBiometricEnabled(),
                    unreadNotificationsCount = notificationsResult.count { n -> !(n["isRead"] as? Boolean ?: true) },
                    unreadMessagesCount = messagesResult.count { m -> !(m["isRead"] as? Boolean ?: true) }
                ) }
            } else {
                // Default state for demo/guest
                _uiState.update { it.copy(
                    userName = "Guest User",
                    userRole = "student"
                ) }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onCategorySelected(category: String) {
        _uiState.value = _uiState.value.copy(activeCategory = category)
        // Trigger filtered fetch from Product Catalog Module
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleFavorite(productId: String) {
        val currentFavorites = _uiState.value.favoriteProductIds
        val newFavorites = if (currentFavorites.contains(productId)) {
            currentFavorites - productId
        } else {
            currentFavorites + productId
        }
        _uiState.value = _uiState.value.copy(favoriteProductIds = newFavorites)
    }

    fun quickReorder(product: Product) {
        addToCart(product)
    }

    fun setShowQuickReorder(show: Boolean) {
        _uiState.value = _uiState.value.copy(showQuickReorder = show)
    }

    fun setShowFavorites(show: Boolean) {
        _uiState.value = _uiState.value.copy(showFavorites = show)
    }

    fun setSelectedProduct(product: Product?) {
        _uiState.update { it.copy(
            selectedProduct = product,
            selectedSize = null,
            selectedColor = product?.availableColors?.firstOrNull()
        ) }
        
        if (product != null) {
            loadProductReviews(product.id)
        }
    }

    private fun loadProductReviews(productId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isReviewsLoading = true) }
            productRepository.getProductReviews(productId).onSuccess { reviews ->
                _uiState.update { it.copy(productReviews = reviews, isReviewsLoading = false) }
            }.onFailure {
                _uiState.update { it.copy(isReviewsLoading = false) }
            }
        }
    }

    fun submitReview(productId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            val userId = userRepository.getUserId() ?: return@launch
            productRepository.addReview(productId, userId, rating, comment).onSuccess {
                loadProductReviews(productId)
                // Also refresh product to get new average rating
                productRepository.refreshProducts()
            }
        }
    }

    fun setSelectedSize(size: String) {
        _uiState.value = _uiState.value.copy(selectedSize = size)
    }

    fun setSelectedColor(color: ProductColor) {
        _uiState.value = _uiState.value.copy(selectedColor = color)
    }

    fun setCatalogSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(catalogSearchQuery = query)
    }

    fun setCatalogCategory(category: String) {
        _uiState.value = _uiState.value.copy(
            catalogSelectedCategory = category,
            catalogSelectedSubCategory = null // Reset sub-category when changing main category
        )
    }

    fun setCatalogSubCategory(subCategory: String?) {
        _uiState.value = _uiState.value.copy(catalogSelectedSubCategory = subCategory)
    }

    fun setCatalogGender(gender: String) {
        _uiState.value = _uiState.value.copy(catalogSelectedGender = gender)
    }

    fun setCatalogSortOption(option: CatalogSortOption) {
        _uiState.value = _uiState.value.copy(catalogSortOption = option)
    }

    fun setCatalogPriceRange(min: Float, max: Float) {
        _uiState.value = _uiState.value.copy(catalogMinPrice = min, catalogMaxPrice = max)
    }

    fun setUserType(userType: UserType) {
        _uiState.value = _uiState.value.copy(userType = userType)
    }

    fun toggleCatalogSize(size: String) {
        val current = _uiState.value.catalogSelectedSizes
        val new = if (current.contains(size)) current - size else current + size
        _uiState.value = _uiState.value.copy(catalogSelectedSizes = new)
    }

    fun toggleCatalogMaterial(material: String) {
        val current = _uiState.value.catalogSelectedMaterials
        val new = if (current.contains(material)) current - material else current + material
        _uiState.value = _uiState.value.copy(catalogSelectedMaterials = new)
    }

    fun resetFilters() {
        _uiState.value = _uiState.value.copy(
            catalogMinPrice = 0f,
            catalogMaxPrice = 20000f,
            catalogSelectedSizes = emptySet(),
            catalogSelectedMaterials = emptySet(),
            catalogSelectedGender = "All"
        )
    }

    fun addToCart(product: Product) {
        // Validation: If product requires size selection and none is selected, open details
        if (product.availableSizes.isNotEmpty() && _uiState.value.selectedSize == null) {
            setSelectedProduct(product)
            return
        }

        // Validation: Check if in stock
        if (!product.inStock) {
            _uiState.update { it.copy(error = "Sorry, this item is currently out of stock.") }
            return
        }

        val newItem = CartItem(
            product = product,
            size = _uiState.value.selectedSize ?: "One Size",
            color = _uiState.value.selectedColor ?: product.availableColors.firstOrNull(),
            quantity = 1
        )

        cartRepository.addToCart(newItem)
    }

    fun removeFromCart(cartItem: CartItem) {
        cartRepository.removeFromCart(cartItem)
    }

    fun updateCartItemQuantity(cartItem: CartItem, newQuantity: Int) {
        cartRepository.updateQuantity(cartItem, newQuantity)
    }

    fun checkout(userId: String, totalAmount: Double, address: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(checkoutLoading = true, checkoutError = null) }
            val result = orderRepository.placeOrder(userId, _uiState.value.cartItems, totalAmount, address)
            when (result) {
                is OrderResult.Success -> {
                    _uiState.update { it.copy(
                        orderId = result.orderId,
                        checkoutLoading = false
                    ) }
                    cartRepository.clearCart()
                }
                is OrderResult.Error -> {
                    _uiState.update { it.copy(
                        checkoutError = result.message,
                        checkoutLoading = false
                    ) }
                }
                OrderResult.Loading -> {
                    _uiState.update { it.copy(checkoutLoading = true) }
                }
            }
        }
    }

    fun initiatePayment(orderId: String, phoneNumber: String, amount: Double) {
        viewModelScope.launch {
            val result = paymentRepository.initiateMpesaPayment(orderId, phoneNumber, amount)
            when (result) {
                is PaymentStatus.Success -> {
                    _uiState.update { it.copy(paymentStatus = result.checkoutId) }
                }
                is PaymentStatus.Error -> {
                    _uiState.update { it.copy(paymentStatus = "Error: ${result.message}") }
                    // Log payment failure
                    val userId = userRepository.getUserId() ?: "unknown"
                    adminRepository.logAction(userId, "PAYMENT_FAILURE", "STK Push failed for order $orderId: ${result.message}", "error")
                }
                else -> {}
            }
        }
    }

    fun checkPaymentStatus(checkoutId: String) {
        viewModelScope.launch {
            val status = paymentRepository.checkStatus(checkoutId)
            val resultCode = status["ResultCode"]?.toString()
            if (resultCode == "0") {
                val receipt = status["MpesaReceiptNumber"]?.toString() ?: "Completed"
                _uiState.update { it.copy(paymentStatus = receipt) }
            } else if (resultCode != null && resultCode != "PENDING") {
                val errorMsg = status["ResultDesc"]?.toString() ?: "Unknown error"
                _uiState.update { it.copy(paymentStatus = "Error: $errorMsg") }
                
                // Log polling failure
                val userId = userRepository.getUserId() ?: "unknown"
                adminRepository.logAction(userId, "PAYMENT_POLLING_ERROR", "Payment status check failed for $checkoutId: $errorMsg", "warning")
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null, checkoutError = null, successMessage = null) }
    }



    // Vendor Actions
    fun loadVendorData(vendorId: String) {
        viewModelScope.launch {
            try {
                val productsResult = vendorRepository.getVendorProducts(vendorId)
                val ordersResult = vendorRepository.getVendorOrders(vendorId)
                
                _uiState.update { it.copy(
                    vendorProducts = productsResult.getOrDefault(emptyList()),
                    vendorOrders = ordersResult.getOrDefault(emptyList())
                ) }

                // Check for low stock alerts (less than 5 items)
                val lowStockItems = productsResult.getOrDefault(emptyList()).filter { it.inStock && it.stockCount < 5 }
                if (lowStockItems.isNotEmpty()) {
                    _uiState.update { it.copy(successMessage = "Alert: ${lowStockItems.size} products are low on stock!") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to load vendor data") }
            }
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                vendorRepository.addProduct(product)
                userRepository.getUserId()?.let { loadVendorData(it) }
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to add product") }
            }
        }
    }

    // Admin Actions
    fun loadAdminData() {
        viewModelScope.launch {
            try {
                val pendingResult = adminRepository.getPendingVendors()
                val logsResult = adminRepository.getSystemLogs()
                val usersResult = adminRepository.getAllUsers()
                val ordersResult = adminRepository.getAllOrders()
                
                _uiState.update { it.copy(
                    pendingVendors = pendingResult.getOrDefault(emptyList()),
                    systemLogs = logsResult.getOrDefault(emptyList()),
                    allOrders = ordersResult.getOrDefault(emptyList())
                ) }
                _allUsers.value = usersResult.getOrDefault(emptyList())
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to load admin dashboard") }
            }
        }
    }

    fun approveVendor(vendorId: String) {
        viewModelScope.launch {
            val adminId = userRepository.getUserId() ?: "unknown"
            adminRepository.approveVendor(vendorId, adminId)
            loadAdminData()
        }
    }

    fun rejectVendor(vendorId: String, notes: String? = null) {
        viewModelScope.launch {
            val adminId = userRepository.getUserId() ?: "unknown"
            val result = adminRepository.rejectVendor(vendorId, adminId, notes)
            if (result.isSuccess) {
                loadAdminData()
            }
        }
    }

    fun clearSystemLogs() {
        viewModelScope.launch {
            val adminId = userRepository.getUserId() ?: "unknown"
            adminRepository.clearSystemLogs().onSuccess {
                adminRepository.logAction(adminId, "CLEAR_LOGS", "Admin cleared all system logs")
                loadAdminData()
            }
        }
    }

    fun markNotificationAsRead(id: Int) {
        viewModelScope.launch {
            // repository call
        }
    }

    fun revokeSession(sessionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = userRepository.revokeSession(sessionId)
            if (result.isSuccess) {
                val userId = userRepository.getUserId() ?: return@launch
                val newSessions = userRepository.getActiveSessions(userId)
                _uiState.update { it.copy(activeSessions = newSessions, isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Failed to revoke session") }
            }
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        userRepository.setBiometricEnabled(enabled)
        _uiState.update { it.copy(biometricEnabled = enabled) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        // Here you would typically also register/unregister from FCM
    }

    // User Profile, Messages, Notifications
    fun updateProfile(data: Map<String, Any>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = userRepository.getUserId() ?: return@launch
            val result = userRepository.updateProfile(userId, data)
            if (result.isSuccess) {
                // Profile observer will pick up the change
                _uiState.update { it.copy(isLoading = false, successMessage = "Profile updated successfully!") }
            } else {
                val error = result.exceptionOrNull()
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Update failed: ${error?.let { AppUtils.mapThrowable(it) } ?: "Unknown error"}"
                ) }
            }
        }
    }

    fun uploadAvatar(bytes: ByteArray) {
        viewModelScope.launch {
            val userId = userRepository.getUserId() ?: return@launch
            _uiState.update { it.copy(isLoading = true) }
            
            // Optimize image before upload
            val optimizedBytes = AppUtils.optimizeImage(bytes)
            
            val result = userRepository.uploadImage(userId, optimizedBytes, "avatars")
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, successMessage = "Profile photo updated!") }
            } else {
                val error = result.exceptionOrNull()
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Upload failed: ${error?.let { AppUtils.mapThrowable(it) } ?: "Unknown error"}"
                ) }
            }
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            val userId = userRepository.getUserId() ?: return@launch
            val messageData = mapOf(
                "userId" to userId,
                "text" to text,
                "timestamp" to System.currentTimeMillis()
            )
            userRepository.sendMessage(messageData)
        }
    }

    fun searchCatalog(query: String) {
        _uiState.update { it.copy(catalogSearchQuery = query) }
        // Local filtering is already handled by the remember block in CatalogScreen
        // But for Home screen search or more aggressive caching, we can optionally
        // try to refresh if query is empty or just let the UI handle it.
    }
    fun logout() {
        authRepository.logout()
        userRepository.logout()
    }

    fun getUserRole(): String {
        return _uiState.value.userRole
    }

    fun getUserRepository(): UserRepository = userRepository

    fun addVendorProduct(product: Product, imageBytes: ByteArray? = null) {
        viewModelScope.launch {
            val userId = userRepository.getUserId() ?: return@launch
            _uiState.update { it.copy(isLoading = true) }

            var updatedProduct = product.copy(vendor_id = userId)

            // Optimize and upload image if provided
            if (imageBytes != null) {
                val optimizedBytes = AppUtils.optimizeImage(imageBytes)
                val uploadResult = userRepository.uploadImage(userId, optimizedBytes, "products")
                if (uploadResult.isSuccess) {
                    updatedProduct = updatedProduct.copy(images = listOf(uploadResult.getOrThrow()))
                }
            }

            val result = vendorRepository.addProduct(updatedProduct)
            if (result.isSuccess) {
                loadVendorData(userId)
                _uiState.update { it.copy(isLoading = false, successMessage = "Product added successfully") }
            } else {
                val error = result.exceptionOrNull()
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Failed to add product: ${error?.let { AppUtils.mapThrowable(it) } ?: "Unknown error"}"
                ) }
            }
        }
    }

    fun updateVendorProduct(product: Product) {
        viewModelScope.launch {
            val userId = userRepository.getUserId() ?: return@launch
            val result = vendorRepository.updateProduct(product)
            if (result.isSuccess) {
                loadVendorData(userId)
            }
        }
    }

    fun deleteVendorProduct(productId: String) {
        viewModelScope.launch {
            val userId = userRepository.getUserId() ?: return@launch
            val result = vendorRepository.deleteProduct(productId, userId)
            if (result.isSuccess) {
                loadVendorData(userId)
            }
        }
    }

    fun updateVendorOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            val userId = userRepository.getUserId() ?: return@launch
            val result = vendorRepository.updateOrderStatus(orderId, status, userId)
            if (result.isSuccess) {
                if (_uiState.value.userRole == "admin") {
                    loadAdminData()
                } else {
                    loadVendorData(userId)
                }
            }
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            val adminId = userRepository.getUserId() ?: "unknown"
            productRepository.addCategory(name).onSuccess {
                adminRepository.logAction(adminId, "ADD_CATEGORY", "Added category: $name")
                loadHomeData()
            }
        }
    }

    fun deleteCategory(name: String) {
        viewModelScope.launch {
            val adminId = userRepository.getUserId() ?: "unknown"
            productRepository.deleteCategory(name).onSuccess {
                adminRepository.logAction(adminId, "DELETE_CATEGORY", "Deleted category: $name")
                loadHomeData()
            }
        }
    }

    fun addCoupon(coupon: Map<String, Any>) {
        viewModelScope.launch {
            val adminId = userRepository.getUserId() ?: "unknown"
            productRepository.addCoupon(coupon).onSuccess {
                adminRepository.logAction(adminId, "ADD_COUPON", "Created coupon: ${coupon["code"]}")
                loadAdminMarketingData()
            }
        }
    }

    fun deleteCoupon(id: String) {
        viewModelScope.launch {
            val adminId = userRepository.getUserId() ?: "unknown"
            productRepository.deleteCoupon(id).onSuccess {
                adminRepository.logAction(adminId, "DELETE_COUPON", "Deleted coupon ID: $id")
                loadAdminMarketingData()
            }
        }
    }

    fun addBanner(banner: Map<String, Any>) {
        viewModelScope.launch {
            val adminId = userRepository.getUserId() ?: "unknown"
            productRepository.addBanner(banner).onSuccess {
                adminRepository.logAction(adminId, "ADD_BANNER", "Added banner: ${banner["title"]}")
                loadAdminMarketingData()
            }
        }
    }

    fun deleteBanner(id: String) {
        viewModelScope.launch {
            val adminId = userRepository.getUserId() ?: "unknown"
            productRepository.deleteBanner(id).onSuccess {
                adminRepository.logAction(adminId, "DELETE_BANNER", "Deleted banner ID: $id")
                loadAdminMarketingData()
            }
        }
    }

    fun loadAdminMarketingData() {
        viewModelScope.launch {
            productRepository.getCoupons().onSuccess { coupons ->
                _uiState.update { it.copy(coupons = coupons) }
            }
            productRepository.getBanners().onSuccess { banners ->
                _uiState.update { it.copy(banners = banners) }
            }
        }
    }

    fun uploadBannerImage(bytes: ByteArray, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val userId = userRepository.getUserId() ?: return@launch
            val optimizedBytes = AppUtils.optimizeImage(bytes)
            userRepository.uploadImage(userId, optimizedBytes, "banners").onSuccess { url ->
                onComplete(url)
            }
        }
    }

    fun exportLogsToCSV(): String {
        val logs = _uiState.value.systemLogs
        if (logs.isEmpty()) return ""
        
        val sb = StringBuilder()
        sb.append("Date,User,Action,Details\n")
        
        logs.forEach { log ->
            val date = log["created_at"]?.toString()?.split("T")?.firstOrNull() ?: ""
            val profiles = log["profiles"] as? Map<*, *>
            val user = profiles?.get("full_name")?.toString() ?: "System"
            val action = log["action"]?.toString() ?: ""
            val details = log["details"]?.toString()?.replace(",", ";") ?: ""
            
            sb.append("$date,$user,$action,$details\n")
        }
        
        return sb.toString()
    }

}

data class CartItem(
    val product: Product,
    val size: String,
    val color: ProductColor?,
    val quantity: Int
)
