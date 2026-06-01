package com.smart.comida.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.smart.comida.data.local.entity.FavoriteRecipeEntity

@Dao
interface FavoriteRecipeDao {

    @Query("SELECT * FROM favorite_recipes WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllFavorites(userId: String): Flow<List<FavoriteRecipeEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_recipes WHERE externalRecipeId = :recipeId AND userId = :userId)")
    fun isFavorite(recipeId: Int, userId: String): Flow<Boolean>

    @Query("SELECT * FROM favorite_recipes WHERE externalRecipeId = :recipeId AND userId = :userId LIMIT 1")
    fun getFavorite(recipeId: Int, userId: String): FavoriteRecipeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFavorite(recipe: FavoriteRecipeEntity): Long

    @Query("DELETE FROM favorite_recipes WHERE externalRecipeId = :recipeId AND userId = :userId")
    fun deleteFavorite(recipeId: Int, userId: String): Int
}
