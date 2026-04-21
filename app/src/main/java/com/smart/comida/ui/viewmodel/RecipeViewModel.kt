package com.smart.comida.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartbite.data.Ingredient
import com.example.smartbite.data.Instruction
import com.example.smartbite.data.InstructionStep
import com.example.smartbite.data.Recipe
import com.example.smartbite.data.RecipeDetail
import com.example.smartbite.data.repository.RecipeRepository
import com.smart.comida.util.TranslationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RecipeUiState {
    object Idle : RecipeUiState()
    object Loading : RecipeUiState()
    data class SearchSuccess(val recipes: List<Recipe>) : RecipeUiState()
    data class DetailSuccess(val recipe: RecipeDetail) : RecipeUiState()
    data class Error(val message: String) : RecipeUiState()
}

class RecipeViewModel : ViewModel() {

    private val repository = RecipeRepository()
    private val _uiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Idle)
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    init {
        // Al iniciar el ViewModel, nos aseguramos de que los diccionarios estén listos
        viewModelScope.launch {
            try {
                TranslationHelper.initModels()
            } catch (e: Exception) {
                Log.e("TRANSLATION", "Error descargando modelos: ${e.message}")
            }
        }
    }

    // -----------------------------------------------------
    // CU-08: Buscar Recetas (Con traducción de ida)
    // -----------------------------------------------------
    fun searchRecipes(queryEs: String) {
        _uiState.value = RecipeUiState.Loading

        viewModelScope.launch {
            try {
                // 1. Traducimos el ingrediente a Inglés (ej. "pollo" -> "chicken")
                val queryEn = TranslationHelper.translateToEnglish(queryEs)
                Log.d("API_SEARCH", "Buscando ingrediente en inglés: $queryEn")

                // 2. Buscamos en la API
                val result = repository.searchRecipes(queryEn)

                result.fold(
                    onSuccess = { response ->
                        if (response.results.isEmpty()) {
                            _uiState.value = RecipeUiState.Error("No se encontraron recetas con '$queryEs'.")
                        } else {
                            // Opcional: También podríamos traducir los títulos de la lista aquí
                            _uiState.value = RecipeUiState.SearchSuccess(response.results)
                        }
                    },
                    onFailure = { error ->
                        Log.e("API_ERROR", "Error de red: ${error.message}", error)
                        _uiState.value = RecipeUiState.Error("Error de red: Verifica tu conexión.")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = RecipeUiState.Error("Error inesperado en la traducción.")
            }
        }
    }

    // -----------------------------------------------------
    // CU-09: Obtener Detalles de Receta (Con traducción de vuelta)
    // -----------------------------------------------------
    fun getRecipeDetail(id: Int) {
        _uiState.value = RecipeUiState.Loading

        viewModelScope.launch {
            val result = repository.getRecipeDetails(id)

            result.fold(
                onSuccess = { detailEn ->
                    // 1. Empezamos la traducción al Español
                    try {
                        val titleEs = TranslationHelper.translateToSpanish(detailEn.title)

                        // Traducimos la lista de ingredientes
                        val ingredientsEs = detailEn.extendedIngredients.map { ing ->
                            Ingredient(original = TranslationHelper.translateToSpanish(ing.original))
                        }

                        // Traducimos la lista de pasos
                        val instructionsEs = if (detailEn.analyzedInstructions.isNotEmpty()) {
                            val stepsEs = detailEn.analyzedInstructions[0].steps.map { step ->
                                InstructionStep(
                                    number = step.number,
                                    step = TranslationHelper.translateToSpanish(step.step)
                                )
                            }
                            listOf(Instruction(steps = stepsEs))
                        } else {
                            emptyList()
                        }

                        // 2. Construimos la receta traducida y la enviamos a la UI
                        val translatedDetail = detailEn.copy(
                            title = titleEs,
                            extendedIngredients = ingredientsEs,
                            analyzedInstructions = instructionsEs
                        )
                        _uiState.value = RecipeUiState.DetailSuccess(translatedDetail)

                    } catch (e: Exception) {
                        Log.e("TRANSLATION", "Error traduciendo detalles", e)
                        // Si falla la traducción, lo mostramos en inglés
                        _uiState.value = RecipeUiState.DetailSuccess(detailEn)
                    }
                },
                onFailure = { error ->
                    Log.e("API_ERROR", "Error de red: ${error.message}", error)
                    _uiState.value = RecipeUiState.Error("No se pudo obtener el detalle de la receta.")
                }
            )
        }
    }
}