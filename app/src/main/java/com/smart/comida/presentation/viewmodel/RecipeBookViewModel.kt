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

sealed interface RecipeBookUiState {
    data object Loading : RecipeBookUiState
    data object Empty : RecipeBookUiState
    data class Success(val recipes: List<FavoriteRecipeEntity>) : RecipeBookUiState
    data class Error(val message: String, val throwable: Throwable? = null) : RecipeBookUiState
}

class RecipeBookViewModel(
    private val repository: FavoritesRepository,
    private val userId: String
) : ViewModel() {

    val uiState: StateFlow<RecipeBookUiState> = repository.getAllFavorites(userId)
        .map { recipes ->
            if (recipes.isEmpty()) {
                RecipeBookUiState.Empty
            } else {
                RecipeBookUiState.Success(recipes)
            }
        }
        .catch { exception ->
            emit(RecipeBookUiState.Error(
                message = exception.message ?: "Ocurrió un error inesperado al cargar el recetario.",
                throwable = exception
            ))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = RecipeBookUiState.Loading
        )
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
