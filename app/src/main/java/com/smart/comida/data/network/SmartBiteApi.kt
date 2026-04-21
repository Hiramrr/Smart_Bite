package com.smart.comida.data.network

import com.example.smartbite.data.RecipeDetail
import com.example.smartbite.data.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SmartBiteApi {

    // CU-08: Buscar Recetas
    @GET("api/recetas/buscar")
    suspend fun searchRecipes(
        @Query("query") query: String
    ): SearchResponse

    // CU-09: Detalles de Receta
    @GET("api/recetas/detalle")
    suspend fun getRecipeDetails(
        @Query("id") id: Int
    ): RecipeDetail
}