package com.smart.comida.domain.repository

import com.smart.comida.data.local.entity.FavoriteRecipeEntity
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    // Método original requerido por la app para el recetario (CU-11)
    fun getAllFavorites(userId: String): Flow<List<FavoriteRecipeEntity>>

    // Método original para verificar estado en el detalle (CU-09)
    fun isFavorite(recipeId: Int, userId: String): Flow<Boolean>

    // Permite abrir el detalle local sin depender de la API (CU-09)
    suspend fun getFavorite(userId: String, externalRecipeId: Int): FavoriteRecipeEntity?

    // Guarda una receta una sola vez; retorna false cuando ya existía (CU-10)
    suspend fun saveToFavorites(recipe: FavoriteRecipeEntity, userId: String): Boolean

    // NUEVO: Método explícito y atómico para la eliminación directa en CU-12
    suspend fun deleteFromFavorites(userId: String, externalRecipeId: Int)
}
