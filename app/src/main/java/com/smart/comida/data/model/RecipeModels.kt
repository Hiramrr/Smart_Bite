package com.example.smartbite.data

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    @SerializedName("results") val results: List<Recipe>
)

data class Recipe(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("image") val image: String
)

// Modelo para los detalles de la receta (CU-09)
data class RecipeDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("image") val image: String,
    @SerializedName("readyInMinutes") val readyInMinutes: Int,
    @SerializedName("servings") val servings: Int,
    @SerializedName("extendedIngredients") val extendedIngredients: List<Ingredient>,
    @SerializedName("analyzedInstructions") val analyzedInstructions: List<Instruction>
)

data class Ingredient(
    @SerializedName("original") val original: String
)

data class Instruction(
    @SerializedName("steps") val steps: List<InstructionStep>
)

data class InstructionStep(
    @SerializedName("number") val number: Int,
    @SerializedName("step") val step: String
)