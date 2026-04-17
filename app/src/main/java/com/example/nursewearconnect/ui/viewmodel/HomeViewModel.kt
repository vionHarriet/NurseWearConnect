package com.example.nursewearconnect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nursewearconnect.model.MOCK_PRODUCTS
import com.example.nursewearconnect.model.Product
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val userName: String = "",
    val greeting: String = "Good Morning",
    val unreadNotificationsCount: Int = 0,
    val categories: List<String> = listOf("All", "Scrubs", "Jackets", "Shoes", "Accessories"),
    val activeCategory: String = "All",
    val searchQuery: String = "",
    val featuredProducts: List<Product> = emptyList(),
    val recommendations: List<Product> = emptyList(),
    val cartCount: Int = 0,
    val isLoading: Boolean = false
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Simulating Parallel Processes (Authentication, Profile, Promotions, etc.)
            // In a real app, these would be separate API calls
            delay(1000) 
            
            _uiState.value = _uiState.value.copy(
                userName = "Dr. Sarah Jenkins",
                unreadNotificationsCount = 3,
                recommendations = MOCK_PRODUCTS.take(4),
                cartCount = 2,
                isLoading = false
            )
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
        // Interact with Wishlist Module
    }

    fun addToCart(product: Product) {
        // Interact with Cart Module
        _uiState.value = _uiState.value.copy(cartCount = _uiState.value.cartCount + 1)
    }
}
