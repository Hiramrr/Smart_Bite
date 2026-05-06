package com.smart.comida.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.smart.comida.data.local.entity.FavoriteRecipeEntity

@Dao
interface FavoriteRecipeDao {

    @Query("SELECT * FROM favorite_recipes ORDER BY createdAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteRecipeEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_recipes WHERE externalRecipeId = :recipeId)")
    fun isFavorite(recipeId: Int): Flow<Boolean>

    // KSP generará esto en Java estándar sin inyectar objetos Continuation
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFavorite(recipe: FavoriteRecipeEntity): Long

    // Mantenemos la eficiencia O(1) de borrar directo por ID
    @Query("DELETE FROM favorite_recipes WHERE externalRecipeId = :recipeId")
    fun deleteFavorite(recipeId: Int): Int
}