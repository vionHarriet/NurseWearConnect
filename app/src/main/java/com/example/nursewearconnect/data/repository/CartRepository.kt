package com.example.nursewearconnect.data.repository

import com.example.nursewearconnect.data.security.SecurityManager
import com.example.nursewearconnect.ui.viewmodel.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CartRepository(private val securityManager: SecurityManager) {
    private val _cartItems = MutableStateFlow<List<CartItem>>(securityManager.getCart())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    fun addToCart(newItem: CartItem) {
        val currentList = _cartItems.value.toMutableList()
        val existingItemIndex = currentList.indexOfFirst { 
            it.product.id == newItem.product.id && it.size == newItem.size && it.color?.name == newItem.color?.name 
        }

        if (existingItemIndex != -1) {
            val existingItem = currentList[existingItemIndex]
            currentList[existingItemIndex] = existingItem.copy(quantity = existingItem.quantity + newItem.quantity)
        } else {
            currentList.add(newItem)
        }

        _cartItems.value = currentList
        securityManager.saveCart(currentList)
    }

    fun removeFromCart(cartItem: CartItem) {
        val updatedList = _cartItems.value.filter { it != cartItem }
        _cartItems.value = updatedList
        securityManager.saveCart(updatedList)
    }

    fun updateQuantity(cartItem: CartItem, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(cartItem)
            return
        }
        val updatedList = _cartItems.value.map {
            if (it == cartItem) it.copy(quantity = quantity) else it
        }
        _cartItems.value = updatedList
        securityManager.saveCart(updatedList)
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        securityManager.clearCart()
    }
}
