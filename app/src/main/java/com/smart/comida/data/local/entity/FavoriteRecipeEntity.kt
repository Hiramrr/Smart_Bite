package com.smart.comida.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "favorite_recipes",
    primaryKeys = ["externalRecipeId", "userId"]
)
data class FavoriteRecipeEntity(
    @ColumnInfo(name = "externalRecipeId")
    val externalRecipeId: Int,
    val title: String,
    val imageUrl: String,
    val recipeDataJson: String,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
)
