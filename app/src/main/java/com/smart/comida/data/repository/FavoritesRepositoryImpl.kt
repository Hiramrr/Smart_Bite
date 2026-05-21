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

    // Asegúrate de que permanezca así de limpio:
    override fun getAllFavorites(userId: String): Flow<List<FavoriteRecipeEntity>> =
        dao.getAllFavorites(userId)
    // Retorna directamente el Flow de Room sin inspeccionar la red.
    override fun isFavorite(recipeId: Int, userId: String): Flow<Boolean> =
        dao.isFavorite(recipeId, userId)

    /**
     * Implementación atómica para el flujo normal de CU-12 (Paso 5).
     * Elimina directamente de la persistencia local sin consultas previas redundantes.
     */
    override suspend fun deleteFromFavorites(userId: String, externalRecipeId: Int) {
        withContext(Dispatchers.IO) {
            dao.deleteFavorite(externalRecipeId, userId)
        }
    }

    /**
     * Mantiene la lógica original del Toggle para no romper el comportamiento de otras vistas.
     */
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