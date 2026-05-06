package com.smart.comida.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_recipes")
data class FavoriteRecipeEntity(
    @PrimaryKey(autoGenerate = false)
    val externalRecipeId: Int,
    val title: String,
    val imageUrl: String,
    val recipeDataJson: String,
    val createdAt: Long = System.currentTimeMillis()
)