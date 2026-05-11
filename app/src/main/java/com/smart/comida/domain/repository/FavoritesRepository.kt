package com.smart.comida.domain.repository

import com.smart.comida.data.local.entity.FavoriteRecipeEntity
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun getAllFavorites(userId: String): Flow<List<FavoriteRecipeEntity>>
    fun isFavorite(recipeId: Int, userId: String): Flow<Boolean>
    suspend fun toggleFavorite(recipe: FavoriteRecipeEntity, userId: String)
}
