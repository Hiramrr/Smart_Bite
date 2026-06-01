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
    @SerializedName("analyzedInstructions") val analyzedInstructions: List<Instruction>,
    @SerializedName("nutrition") val nutrition: Nutrition? = null
)

data class Nutrition(
    @SerializedName("nutrients") val nutrients: List<Nutrient> = emptyList()
)

data class Nutrient(
    @SerializedName("name") val name: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("unit") val unit: String
)

data class Ingredient(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("original") val original: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("amount") val amount: Double? = null,
    @SerializedName("unit") val unit: String? = null,
    @SerializedName("measures") val measures: Measures? = null
)

data class Measures(
    @SerializedName("us") val us: Measure? = null,
    @SerializedName("metric") val metric: Measure? = null
)

data class Measure(
    @SerializedName("amount") val amount: Double? = null,
    @SerializedName("unitShort") val unitShort: String? = null,
    @SerializedName("unitLong") val unitLong: String? = null
)

data class Instruction(
    @SerializedName("steps") val steps: List<InstructionStep>
)

data class InstructionStep(
    @SerializedName("number") val number: Int,
    @SerializedName("step") val step: String
)
