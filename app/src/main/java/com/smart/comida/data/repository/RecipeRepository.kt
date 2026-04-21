package com.example.smartbite.data.repository

import com.example.smartbite.data.RecipeDetail
import com.example.smartbite.data.SearchResponse
import com.smart.comida.data.network.RetrofitClient

class RecipeRepository {

    private val api = RetrofitClient.api

    suspend fun searchRecipes(query: String): Result<SearchResponse> {
        return runCatching {
            api.searchRecipes(query)
        }
    }

    suspend fun getRecipeDetails(id: Int): Result<RecipeDetail> {
        return runCatching {
            api.getRecipeDetails(id)
        }
    }
}