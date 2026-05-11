package com.smart.comida.data.repository

import com.smart.comida.data.local.dao.FavoriteRecipeDao
import com.smart.comida.data.local.entity.FavoriteRecipeEntity
import com.smart.comida.domain.repository.FavoritesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class FavoritesRepositoryImpl(
    private val dao: FavoriteRecipeDao
) : FavoritesRepository {

    override fun getAllFavorites(userId: String): Flow<List<FavoriteRecipeEntity>> =
        dao.getAllFavorites(userId)

    override fun isFavorite(recipeId: Int, userId: String): Flow<Boolean> =
        dao.isFavorite(recipeId, userId)

    override suspend fun toggleFavorite(recipe: FavoriteRecipeEntity, userId: String) {
        withContext(Dispatchers.IO) {
            val exists = dao.isFavorite(recipe.externalRecipeId, userId).first()
            if (exists) {
                dao.deleteFavorite(recipe.externalRecipeId, userId)
            } else {
                dao.insertFavorite(recipe)
            }
        }
    }
}
