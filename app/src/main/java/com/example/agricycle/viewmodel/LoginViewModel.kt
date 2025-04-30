package com.example.agricycle.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agricycle.data.LoginRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: LoginRepository) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isGuestMode = MutableStateFlow(false)
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

    private val _userName = MutableStateFlow("Guest")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language.asStateFlow()

    init {
        _currentUser.value = repository.getCurrentUser()
        viewModelScope.launch {
            repository.observeAuthState().collect { user ->
                _currentUser.value = user
                if (user != null) {
                    _userName.value = user.displayName ?: "User"
                }
            }
        }
        viewModelScope.launch {
            repository.userName.collect { name ->
                _userName.value = name
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                repository.signInWithGoogle(idToken)
                _loginState.value = LoginState.Success
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Failed to sign in with Google")
            }
        }
    }

    fun signOut() {
        repository.signOut()
        _loginState.value = LoginState.Idle
        _isGuestMode.value = false
        _userName.value = "Guest"
    }

    fun setGuestMode(isGuest: Boolean) {
        _isGuestMode.value = isGuest
        _userName.value = "Guest"
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            repository.updateUserName(name)
        }
    }

    fun setLanguage(lang: String) {
        _language.value = lang
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }
}