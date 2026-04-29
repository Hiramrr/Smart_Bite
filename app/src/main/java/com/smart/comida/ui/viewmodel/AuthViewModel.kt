package com.smart.comida.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smart.comida.data.network.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

class AuthViewModel : ViewModel() {

    private val _isUserLoggedIn = MutableStateFlow<Boolean?>(null)
    val isUserLoggedIn: StateFlow<Boolean?> = _isUserLoggedIn

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val session = SupabaseClient.client.auth.currentSessionOrNull()
            Log.d("AuthViewModel", "checkSession: session is ${if (session != null) "active" else "null"}")
            _isUserLoggedIn.value = session != null
            loadUserName()
        }
    }

    private fun loadUserName() {
        viewModelScope.launch {
            val user = SupabaseClient.client.auth.currentUserOrNull()
            val name = user?.userMetadata?.get("name")?.toString()?.removeSurrounding("\"")
                ?: user?.userMetadata?.get("full_name")?.toString()?.removeSurrounding("\"")
            _userName.value = name ?: "Usuario"
            Log.d("AuthViewModel", "loadUserName: ${_userName.value}")
        }
    }

    fun onLoginSuccess() {
        Log.d("AuthViewModel", "onLoginSuccess triggered")
        _isUserLoggedIn.value = true
        loadUserName()
    }

    fun signOut() {
        viewModelScope.launch {
            Log.d("AuthViewModel", "signOut triggered")
            SupabaseClient.client.auth.signOut()
            _isUserLoggedIn.value = false
            _userName.value = null
        }
    }
}
