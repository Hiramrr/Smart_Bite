package com.smart.comida.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "settings")

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private val THEME_MODE_KEY = intPreferencesKey("theme_mode")
    }

    private val dataStore = application.dataStore

    val themeMode: StateFlow<ThemeMode> = dataStore.data
        .map { preferences ->
            val value = preferences[THEME_MODE_KEY] ?: 0
            ThemeMode.values()[value]
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[THEME_MODE_KEY] = mode.ordinal
            }
        }
    }
}
