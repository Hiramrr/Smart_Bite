package com.smart.comida.domain.repository

import com.smart.comida.data.local.entity.FavoriteRecipeEntity
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun getAllFavorites(): Flow<List<FavoriteRecipeEntity>>
    fun isFavorite(recipeId: Int): Flow<Boolean>
    suspend fun toggleFavorite(recipe: FavoriteRecipeEntity)
}