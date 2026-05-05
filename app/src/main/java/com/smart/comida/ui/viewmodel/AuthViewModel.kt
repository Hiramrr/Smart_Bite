package com.smart.comida.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smart.comida.data.network.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

class AuthViewModel : ViewModel() {

    private val _isUserLoggedIn = MutableStateFlow<Boolean?>(null)
    val isUserLoggedIn: StateFlow<Boolean?> = _isUserLoggedIn

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail

    private val _userAvatarUrl = MutableStateFlow<String?>(null)
    val userAvatarUrl: StateFlow<String?> = _userAvatarUrl

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            SupabaseClient.client.auth.sessionStatus.collect { status ->
                Log.d("AuthViewModel", "SessionStatus: $status")
                when (status) {
                    is SessionStatus.Authenticated -> {
                        _isUserLoggedIn.value = true
                        loadUserProfile()
                    }
                    is SessionStatus.NotAuthenticated -> {
                        _isUserLoggedIn.value = false
                    }
                    is SessionStatus.LoadingFromStorage -> {
                        // Keep null (show loading spinner) while checking storage
                    }
                    is SessionStatus.NetworkError -> {
                        Log.e("AuthViewModel", "Network error checking session")
                        _isUserLoggedIn.value = false
                    }
                }
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val user = SupabaseClient.client.auth.currentUserOrNull()
            val name = user?.userMetadata?.get("name")?.toString()?.removeSurrounding("\"")
                ?: user?.userMetadata?.get("full_name")?.toString()?.removeSurrounding("\"")
            _userName.value = name ?: "Usuario"
            _userEmail.value = user?.email ?: user?.userMetadata?.get("email")?.toString()?.removeSurrounding("\"")
            _userAvatarUrl.value = user?.userMetadata?.get("avatar_url")?.toString()?.removeSurrounding("\"")
                ?: user?.userMetadata?.get("picture")?.toString()?.removeSurrounding("\"")
            Log.d("AuthViewModel", "loadUserProfile: name=${_userName.value}, email=${_userEmail.value}")
        }
    }

    fun onLoginSuccess() {
        Log.d("AuthViewModel", "onLoginSuccess triggered")
        _isUserLoggedIn.value = true
        loadUserProfile()
    }

    fun signOut() {
        viewModelScope.launch {
            Log.d("AuthViewModel", "signOut triggered")
            SupabaseClient.client.auth.signOut()
            _isUserLoggedIn.value = false
            _userName.value = null
            _userEmail.value = null
            _userAvatarUrl.value = null
        }
    }
}
