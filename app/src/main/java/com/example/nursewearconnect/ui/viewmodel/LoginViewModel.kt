package com.example.nursewearconnect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nursewearconnect.data.repository.AuthRepository
import com.example.nursewearconnect.utils.AppUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _loginSuccess = MutableStateFlow<String?>(null) // Stores role on success
    val loginSuccess: StateFlow<String?> = _loginSuccess.asStateFlow()

    private val _failedAttempts = MutableStateFlow(0)
    val failedAttempts: StateFlow<Int> = _failedAttempts.asStateFlow()

    private val _lockoutTimeRemaining = MutableStateFlow(0L)
    val lockoutTimeRemaining: StateFlow<Long> = _lockoutTimeRemaining.asStateFlow()

    private val MAX_ATTEMPTS = 5
    private val LOCKOUT_DURATION = 60000L // 1 minute

    fun login(email: String, password: String) {
        if (_lockoutTimeRemaining.value > System.currentTimeMillis()) {
            val remainingSecs = (_lockoutTimeRemaining.value - System.currentTimeMillis()) / 1000
            _error.value = "Too many failed attempts. Please try again in $remainingSecs seconds."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val result = authRepository.login(email, password)
            _isLoading.value = false
            
            result.onSuccess {
                _failedAttempts.value = 0
                _loginSuccess.value = authRepository.getUserRole()
            }
            result.onFailure {
                _failedAttempts.value += 1
                if (_failedAttempts.value >= MAX_ATTEMPTS) {
                    _lockoutTimeRemaining.value = System.currentTimeMillis() + LOCKOUT_DURATION
                    _error.value = "Account locked due to too many failed attempts. Try again in 1 minute."
                } else {
                    _error.value = AppUtils.mapThrowable(it)
                }
            }
        }
    }

    fun resetLoginState() {
        _loginSuccess.value = null
        _error.value = null
    }
}
