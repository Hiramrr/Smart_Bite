package com.smart.comida.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smart.comida.data.local.entity.FavoriteRecipeEntity
import com.smart.comida.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    fun onToggleFavorite(recipe: FavoriteRecipeEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(recipe, userId)
        }
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
