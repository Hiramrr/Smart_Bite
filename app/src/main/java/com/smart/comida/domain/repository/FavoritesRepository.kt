package com.smart.comida.domain.repository

import com.smart.comida.data.local.entity.FavoriteRecipeEntity
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    // Método original requerido por la app para el recetario (CU-11)
    fun getAllFavorites(userId: String): Flow<List<FavoriteRecipeEntity>>

    // Método original para verificar estado en el detalle (CU-09)
    fun isFavorite(recipeId: Int, userId: String): Flow<Boolean>

    // Método original para alternar estados desde el detalle (CU-10)
    suspend fun toggleFavorite(recipe: FavoriteRecipeEntity, userId: String)

    // NUEVO: Método explícito y atómico para la eliminación directa en CU-12
    suspend fun deleteFromFavorites(userId: String, externalRecipeId: Int)
}