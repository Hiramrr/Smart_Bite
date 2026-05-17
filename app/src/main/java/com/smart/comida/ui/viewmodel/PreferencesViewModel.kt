package com.smart.comida.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smart.comida.data.model.UserPreferences
import com.smart.comida.data.network.SupabaseClient
import com.smart.comida.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class PreferencesViewModel(
    private val repository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreferencesUiState())
    val uiState: StateFlow<PreferencesUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        val userId = SupabaseClient.currentUserId ?: return

        _uiState.value = _uiState.value.copy(isLoading = true)

        // 1. Sincronizar con la nube (por si abrió sesión en otro dispositivo)
        //viewModelScope.launch {
          //  repository.fetchPreferencesFromRemote(userId)
        //}

        // 2. Suscribirse a la fuente de verdad local (DataStore)
        viewModelScope.launch {
            repository.getPreferencesFlow(userId)
                .catch { /* Manejar error de lectura local si es necesario */ }
                .collect { preferences ->
                    _uiState.value = _uiState.value.copy(
                        preferences = preferences,
                        isLoading = false
                    )
                }
        }
    }

    fun savePreferences(selectedDiets: List<String>, selectedIntolerances: List<String>) {
        val userId = SupabaseClient.currentUserId ?: return
        val currentPrefs = _uiState.value.preferences

        // Flujo Alternativo FA-01: 'Sin cambios'
        if (currentPrefs?.diets?.sorted() == selectedDiets.sorted() &&
            currentPrefs.intolerances.sorted() == selectedIntolerances.sorted()) {

            _uiState.value = _uiState.value.copy(message = "Guardado sin cambios.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            val result = repository.savePreferences(userId, selectedDiets, selectedIntolerances)

            result.onSuccess {
                // Flujo Normal: Éxito
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Preferencias guardadas correctamente."
                )
            }.onFailure {
                // Excepción Ex-01: Error al guardar
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Error al guardar. Inténtalo de nuevo."
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}

data class PreferencesUiState(
    val isLoading: Boolean = false,
    val preferences: UserPreferences? = null,
    val message: String? = null
)