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

    override fun getAllFavorites(): Flow<List<FavoriteRecipeEntity>> = dao.getAllFavorites()

    override fun isFavorite(recipeId: Int): Flow<Boolean> = dao.isFavorite(recipeId)

    override suspend fun toggleFavorite(recipe: FavoriteRecipeEntity) {
        withContext(Dispatchers.IO) {
            val exists = dao.isFavorite(recipe.externalRecipeId).first()
            if (exists) {
                dao.deleteFavorite(recipe.externalRecipeId)
                // TODO: DELETE hacia el backend de Go
            } else {
                dao.insertFavorite(recipe)
                // TODO: POST hacia el backend de Go
            }
        }
    }
}