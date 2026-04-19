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
import kotlinx.coroutines.delay
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
    val orderId: String? = null,
    val checkoutError: String? = null,
    val checkoutLoading: Boolean = false,
    val paymentStatus: String? = null,
    val error: String? = null
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
    private val adminRepository: com.example.nursewearconnect.data.repository.AdminRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _allUsers = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val allUsers: StateFlow<List<Map<String, Any>>> = _allUsers

    init {
        observeProducts()
        observeCart()
        observeUserProfile()
        loadHomeData()
    }

    private fun observeProducts() {
        viewModelScope.launch {
            productRepository.products.collectLatest { products ->
                _uiState.update { it.copy(
                    products = products,
                    featuredProduct = products.find { p -> p.tag == "NEW" },
                    newArrivals = products.filter { p -> p.tag == "NEW" },
                    recommendations = products.take(4),
                    reorderItems = products.takeLast(2)
                ) }
            }
        }
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            userRepository.userProfile.collectLatest { profile ->
                profile?.let {
                    val newRole = (it["role"] as? String ?: "student").lowercase()
                    val currentRole = _uiState.value.userRole
                    val name = it["full_name"] as? String ?: ""
                    
                    _uiState.update { state ->
                        state.copy(
                            userName = name,
                            userRole = newRole,
                            userType = if (newRole == "student") UserType.STUDENT else UserType.PROFESSIONAL
                        )
                    }

                    if (newRole != currentRole) {
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

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // 1. Refresh Products
            val productsResult = productRepository.refreshProducts()
            if (productsResult.isFailure) {
                _uiState.update { it.copy(error = "Offline mode: Failed to refresh products.") }
            }
            
            // 2. Refresh Profile if logged in
            val userId = userRepository.getUserId()
            if (userId != null && userId != "demo_user") {
                userRepository.fetchProfile(userId)
                    .onFailure { error ->
                        _uiState.update { it.copy(error = "Profile sync error: ${error.message}") }
                    }

                // 3. Load other user data
                val notificationsResult = userRepository.getNotifications(userId)
                val messagesResult = userRepository.getMessages(userId)
                val userOrdersResult = orderRepository.getUserOrders("eq.$userId")
                
                val orders = userOrdersResult.getOrDefault(emptyList()).map { map ->
                    val profiles = map["profiles"] as? Map<*, *>
                    val customerName = profiles?.get("full_name")?.toString() ?: "Customer"
                    map + ("customer_name" to customerName)
                }

                _uiState.update { it.copy(
                    notifications = notificationsResult,
                    messages = messagesResult,
                    allOrders = orders,
                    unreadNotificationsCount = notificationsResult.count { n -> !(n["isRead"] as? Boolean ?: true) }
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
        _uiState.value = _uiState.value.copy(searchQuery = query)
        // Implement debouncing here for API calls
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
        _uiState.value = _uiState.value.copy(
            selectedProduct = product,
            selectedSize = null,
            selectedColor = product?.availableColors?.firstOrNull()
        )
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
            _uiState.value = _uiState.value.copy(checkoutLoading = true, checkoutError = null)
            val result = orderRepository.placeOrder(userId, _uiState.value.cartItems, totalAmount, address)
            when (result) {
                is OrderResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        orderId = result.orderId,
                        checkoutLoading = false
                    )
                    cartRepository.clearCart()
                }
                is OrderResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        checkoutError = result.message,
                        checkoutLoading = false
                    )
                }
                OrderResult.Loading -> {
                    _uiState.value = _uiState.value.copy(checkoutLoading = true)
                }
            }
        }
    }

    fun initiatePayment(orderId: String, phoneNumber: String, amount: Double) {
        viewModelScope.launch {
            val result = paymentRepository.initiateMpesaPayment(orderId, phoneNumber, amount)
            when (result) {
                is PaymentStatus.Success -> {
                    _uiState.value = _uiState.value.copy(paymentStatus = "STK Push Sent")
                }
                is PaymentStatus.Error -> {
                    _uiState.value = _uiState.value.copy(paymentStatus = "Payment Error: ${result.message}")
                }
                PaymentStatus.Loading -> {
                    _uiState.value = _uiState.value.copy(paymentStatus = "Processing Payment...")
                }
                PaymentStatus.Idle -> {}
            }
        }
    }

    // User Profile, Messages, Notifications
    fun updateProfile(data: Map<String, Any>) {
        viewModelScope.launch {
            val userId = userRepository.getUserId() ?: return@launch
            val result = userRepository.updateProfile(userId, data)
            if (result.isSuccess) {
                userRepository.fetchProfile(userId)
            } else {
                _uiState.update { it.copy(error = "Failed to update profile: ${result.exceptionOrNull()?.message}") }
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

    fun markNotificationAsRead(id: Int) {
        viewModelScope.launch {
            // repository call
        }
    }

    fun logout() {
        userRepository.logout()
    }

    fun getUserRole(): String {
        return _uiState.value.userRole
    }

    fun getUserRepository(): UserRepository = userRepository

    // Vendor Actions
    private fun loadVendorData(vendorId: String) {
        viewModelScope.launch {
            val productsResult = vendorRepository.getVendorProducts(vendorId)
            val ordersResult = vendorRepository.getVendorOrders(vendorId)
            
            _uiState.value = _uiState.value.copy(
                vendorProducts = productsResult.getOrDefault(emptyList()),
                vendorOrders = ordersResult.getOrDefault(emptyList())
            )
        }
    }

    fun addVendorProduct(product: Product) {
        viewModelScope.launch {
            val userId = userRepository.getUserId() ?: return@launch
            val result = vendorRepository.addProduct(product.copy(vendor_id = userId))
            if (result.isSuccess) {
                loadVendorData(userId)
            } else {
                _uiState.update { it.copy(error = "Failed to add product: ${result.exceptionOrNull()?.message}") }
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
            val result = vendorRepository.deleteProduct(productId)
            if (result.isSuccess) {
                loadVendorData(userId)
            }
        }
    }

    fun updateVendorOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            val userId = userRepository.getUserId() ?: return@launch
            val result = vendorRepository.updateOrderStatus(orderId, status)
            if (result.isSuccess) {
                loadVendorData(userId)
            }
        }
    }

    // Admin Actions
    fun loadAdminData() {
        viewModelScope.launch {
            val pendingResult = adminRepository.getPendingVendors()
            val couponsResult = productRepository.getCoupons()
            val bannersResult = productRepository.getBanners()
            val ordersResult = adminRepository.getAllOrders()
            val logsResult = adminRepository.getSystemLogs()
            val users = userRepository.getAllUsers()

            val orders = ordersResult.getOrDefault(emptyList()).map { map ->
                val profiles = map["profiles"] as? Map<*, *>
                val customerName = profiles?.get("full_name")?.toString() ?: "Customer"
                
                map + ("customer_name" to customerName)
            }

            _allUsers.value = users
            _uiState.update { it.copy(
                pendingVendors = pendingResult.getOrDefault(emptyList()),
                coupons = couponsResult.getOrDefault(emptyList()),
                banners = bannersResult.getOrDefault(emptyList()),
                allOrders = orders,
                systemLogs = logsResult.getOrDefault(emptyList())
            ) }
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            productRepository.addCategory(name).onSuccess {
                loadHomeData()
            }
        }
    }

    fun deleteCategory(name: String) {
        viewModelScope.launch {
            productRepository.deleteCategory(name).onSuccess {
                loadHomeData()
            }
        }
    }

    fun addCoupon(coupon: Map<String, Any>) {
        viewModelScope.launch {
            productRepository.addCoupon(coupon).onSuccess {
                loadAdminData()
            }
        }
    }

    fun deleteCoupon(id: String) {
        viewModelScope.launch {
            productRepository.deleteCoupon(id).onSuccess {
                loadAdminData()
            }
        }
    }

    fun approveVendor(vendorId: String) {
        viewModelScope.launch {
            val result = adminRepository.approveVendor(vendorId)
            if (result.isSuccess) {
                loadAdminData()
            }
        }
    }

    fun rejectVendor(vendorId: String) {
        viewModelScope.launch {
            val result = adminRepository.rejectVendor(vendorId)
            if (result.isSuccess) {
                loadAdminData()
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class CartItem(
    val product: Product,
    val size: String,
    val color: ProductColor?,
    val quantity: Int
)
