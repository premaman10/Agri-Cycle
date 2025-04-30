package com.example.agricycle.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserDataRepository {
    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    fun saveUserData(data: UserData) {
        _userData.value = data
    }

    fun clearUserData() {
        _userData.value = null
    }
}

data class UserData(
    val name: String = "",
    val email: String = "",
    val location: String = "",
    val crops: List<String> = emptyList()
) 