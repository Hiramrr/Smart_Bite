package com.smart.comida.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smart.comida.data.local.entity.FavoriteRecipeEntity
import com.smart.comida.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class FavoriteActionUiState {
    object Idle : FavoriteActionUiState()
    data class Message(val text: String) : FavoriteActionUiState()
}

class RecipeDetailViewModel(
    private val repository: FavoritesRepository,
    private val recipeId: Int,
    private val userId: String
) : ViewModel() {

    val isFavorite: StateFlow<Boolean> = repository.isFavorite(recipeId, userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _actionUiState = MutableStateFlow<FavoriteActionUiState>(FavoriteActionUiState.Idle)
    val actionUiState: StateFlow<FavoriteActionUiState> = _actionUiState.asStateFlow()

    suspend fun getFavoriteRecipe(): FavoriteRecipeEntity? =
        repository.getFavorite(userId, recipeId)

    fun saveFavorite(recipe: FavoriteRecipeEntity) {
        viewModelScope.launch {
            runCatching {
                repository.saveToFavorites(recipe, userId)
            }.onSuccess { saved ->
                _actionUiState.value = FavoriteActionUiState.Message(
                    if (saved) "Receta guardada en favoritos."
                    else "Esta receta ya está guardada en favoritos."
                )
            }.onFailure {
                _actionUiState.value = FavoriteActionUiState.Message(
                    "No se pudo guardar la receta. Inténtalo de nuevo."
                )
            }
        }
    }

    fun clearActionMessage() {
        _actionUiState.value = FavoriteActionUiState.Idle
    }
}

class RecipeDetailViewModelFactory(
    private val repository: FavoritesRepository,
    private val recipeId: Int,
    private val userId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeDetailViewModel::class.java)) {
            return RecipeDetailViewModel(repository, recipeId, userId) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}
