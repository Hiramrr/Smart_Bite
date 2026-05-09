package com.smart.comida.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smart.comida.data.local.entity.FavoriteRecipeEntity
import com.smart.comida.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// Representación exhaustiva de los estados de la pantalla (CU-11, FA-01, Ex-01)
sealed interface RecipeBookUiState {
    object Loading : RecipeBookUiState
    object Empty : RecipeBookUiState // FA-01: Recetario vacío
    data class Success(val recipes: List<FavoriteRecipeEntity>) : RecipeBookUiState
    data class Error(val message: String, val throwable: Throwable? = null) : RecipeBookUiState // Ex-01: Error al cargar
}

class RecipeBookViewModel(
    repository: FavoritesRepository
) : ViewModel() {

    // Transformamos el Flow crudo de Room en un StateFlow procesado para la UI
    val uiState: StateFlow<RecipeBookUiState> = repository.getAllFavorites()
        .map { recipes ->
            if (recipes.isEmpty()) {
                RecipeBookUiState.Empty
            } else {
                RecipeBookUiState.Success(recipes)
            }
        }
        .catch { exception ->
            emit(RecipeBookUiState.Error("No se pudieron cargar las recetas guardadas", exception))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RecipeBookUiState.Loading
        )
}

// Factory para inyección de dependencias
class RecipeBookViewModelFactory(
    private val repository: FavoritesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeBookViewModel::class.java)) {
            return RecipeBookViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}