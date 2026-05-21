package com.smart.comida.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smart.comida.data.local.entity.FavoriteRecipeEntity
import com.smart.comida.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface RecipeBookUiState {
    object Loading : RecipeBookUiState
    object Empty : RecipeBookUiState
    data class Success(val recipes: List<FavoriteRecipeEntity>) : RecipeBookUiState
    data class Error(val throwable: Throwable, val message: String = "") : RecipeBookUiState
}

class RecipeBookViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val userId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecipeBookUiState>(RecipeBookUiState.Loading)
    val uiState: StateFlow<RecipeBookUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
        refreshFavoritesFromNetwork() // Sincronización en background opcional
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.value = RecipeBookUiState.Loading

            // Consumo de la Fuente Única de Verdad (Local DB) de manera reactiva
            favoritesRepository.getAllFavorites(userId)
                .catch { throwable ->
                    // Solo falla si la base de datos local Room está corrupta o inaccesible
                    _uiState.value = RecipeBookUiState.Error(throwable, "Error crítico en el almacenamiento local")
                }
                .collect { recipes ->
                    if (recipes.isEmpty()) {
                        _uiState.value = RecipeBookUiState.Empty
                    } else {
                        _uiState.value = RecipeBookUiState.Success(recipes)
                    }
                }
        }
    }

    /**
     * Sincroniza en segundo plano con el servidor backend en Go.
     * Si el servidor está apagado, se captura el error silenciosamente o se registra,
     * pero JAMÁS se altera el estado de la UI que consume de la BD local.
     */
    private fun refreshFavoritesFromNetwork() {
        viewModelScope.launch {
            try {
                // Supongamos que tu repositorio tiene un método para refrescar datos en segundo plano
                // favoritesRepository.fetchAndSaveRemoteFavorites(userId)
            } catch (e: Exception) {
                // Servidor apagado o sin internet: Se ignora de forma segura para mantener el modo offline
                // Aquí podrías manejar un estado de aviso "Modo Offline" sin bloquear la pantalla
            }
        }
    }

    fun deleteRecipeFromFavorites(recipeId: Int) {
        viewModelScope.launch {
            try {
                favoritesRepository.deleteFromFavorites(userId, recipeId)
            } catch (t: Throwable) {
                _uiState.value = RecipeBookUiState.Error(t, "Error al intentar eliminar la receta")
            }
        }
    }
}

class RecipeBookViewModelFactory(
    private val repository: FavoritesRepository,
    private val userId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeBookViewModel::class.java)) {
            return RecipeBookViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}
