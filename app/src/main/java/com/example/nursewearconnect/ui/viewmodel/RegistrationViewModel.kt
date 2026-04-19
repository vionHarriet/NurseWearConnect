package com.example.nursewearconnect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nursewearconnect.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegistrationViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _registrationSuccess = MutableStateFlow<String?>(null) // Stores role on success
    val registrationSuccess: StateFlow<String?> = _registrationSuccess.asStateFlow()

    fun register(
        email: String,
        password: String,
        fullName: String,
        phoneNumber: String,
        role: String,
        businessName: String? = null,
        location: String? = null,
        businessDescription: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val result = authRepository.register(
                email = email,
                password = password,
                fullName = fullName,
                phoneNumber = phoneNumber,
                role = role,
                businessName = businessName,
                location = location,
                businessDescription = businessDescription
            )
            
            _isLoading.value = false
            
            result.onSuccess {
                _registrationSuccess.value = role
            }
            result.onFailure {
                _error.value = it.message ?: "Registration failed"
            }
        }
    }

    fun resetRegistrationState() {
        _registrationSuccess.value = null
        _error.value = null
    }
}
