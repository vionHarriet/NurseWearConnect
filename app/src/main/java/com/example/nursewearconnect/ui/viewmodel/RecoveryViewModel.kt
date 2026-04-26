package com.example.nursewearconnect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nursewearconnect.data.repository.AuthRepository
import com.example.nursewearconnect.utils.AppUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecoveryViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success.asStateFlow()

    fun requestPasswordReset(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = authRepository.requestPasswordReset(email)
            _isLoading.value = false
            
            result.onSuccess {
                _success.value = true
            }
            result.onFailure {
                _error.value = AppUtils.mapThrowable(it)
            }
        }
    }

    fun resetPassword(email: String, token: String, newPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = authRepository.resetPassword(email, token, newPassword)
            _isLoading.value = false
            
            result.onSuccess {
                _success.value = true
            }
            result.onFailure {
                _error.value = AppUtils.mapThrowable(it)
            }
        }
    }

    fun clearState() {
        _error.value = null
        _success.value = false
        _isLoading.value = false
    }
}
