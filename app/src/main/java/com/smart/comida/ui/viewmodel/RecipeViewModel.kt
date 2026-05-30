package com.smart.comida.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartbite.data.Instruction
import com.example.smartbite.data.InstructionStep
import com.example.smartbite.data.Recipe
import com.example.smartbite.data.RecipeDetail
import com.example.smartbite.data.repository.RecipeRepository
import com.smart.comida.data.model.Ingrediente
import com.smart.comida.util.TranslationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class RecipeUiState {
    object Idle : RecipeUiState()
    object Loading : RecipeUiState()
    data class SearchSuccess(val recipes: List<Recipe>) : RecipeUiState()
    data class DetailSuccess(val recipe: RecipeDetail) : RecipeUiState()
    data class Error(val message: String, val throwable: Throwable? = null) : RecipeUiState()
}

sealed class RecommendationsUiState {
    object Idle : RecommendationsUiState()
    object Loading : RecommendationsUiState()
    data class Success(val recipes: List<Recipe>) : RecommendationsUiState()
    data class Error(val message: String) : RecommendationsUiState()
}

class RecipeViewModel : ViewModel() {

    private val repository = RecipeRepository()
    private val _uiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Idle)
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    private val _recommendationsUiState = MutableStateFlow<RecommendationsUiState>(RecommendationsUiState.Idle)
    val recommendationsUiState: StateFlow<RecommendationsUiState> = _recommendationsUiState.asStateFlow()

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
                val queryEn = TranslationHelper.translateToEnglish(queryEs)
                Log.d("API_SEARCH", "Buscando ingrediente en inglés: $queryEn")

                val result = repository.searchRecipes(queryEn)

                result.fold(
                    onSuccess = { response ->
                        if (response.results.isEmpty()) {
                            _uiState.value = RecipeUiState.SearchSuccess(emptyList())
                        } else {
                            _uiState.value = RecipeUiState.SearchSuccess(response.results)
                        }
                    },
                    onFailure = { error ->
                        Log.e("API_ERROR", "Error de red: ${error.message}", error)
                        _uiState.value = RecipeUiState.Error("Error de red", error)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = RecipeUiState.Error("Error inesperado", e)
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
                            ing.copy(
                                original = TranslationHelper.translateToSpanish(ing.original),
                                name = ing.name?.let { nombre ->
                                    TranslationHelper.translateToSpanish(nombre)
                                }
                            )
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
                    _uiState.value = RecipeUiState.Error("No se pudo obtener el detalle de la receta", error)
                }
            )
        }
    }

    // -----------------------------------------------------
    // Recomendaciones basadas en Despensa
    // -----------------------------------------------------
    fun getRecommendationsFromPantry(ingredientes: List<Ingrediente>) {
        if (ingredientes.isEmpty()) {
            _recommendationsUiState.value = RecommendationsUiState.Idle
            return
        }

        _recommendationsUiState.value = RecommendationsUiState.Loading

        viewModelScope.launch {
            try {
                val hoy = LocalDate.now()
                val ingredientesClave = ingredientes
                    .filter { !it.fechaCaducidad.isNullOrEmpty() }
                    .mapNotNull { ing ->
                        val fecha = runCatching { LocalDate.parse(ing.fechaCaducidad) }.getOrNull()
                        if (fecha != null && fecha.isAfter(hoy.minusDays(1))) {
                            ing to fecha
                        } else {
                            null
                        }
                    }
                    .sortedBy { it.second }
                    .take(3)
                    .map { it.first.nombre }

                if (ingredientesClave.isEmpty()) {
                    val randomIngs = ingredientes.shuffled().take(3).map { it.nombre }
                    buscarRecomendaciones(randomIngs.joinToString(", "))
                } else {
                    buscarRecomendaciones(ingredientesClave.joinToString(", "))
                }
            } catch (e: Exception) {
                Log.e("RECOMMENDATIONS", "Error al procesar ingredientes", e)
                _recommendationsUiState.value = RecommendationsUiState.Error("Error al procesar ingredientes")
            }
        }
    }

    private suspend fun buscarRecomendaciones(queryEs: String) {
        try {
            val queryEn = TranslationHelper.translateToEnglish(queryEs)
            val result = repository.searchRecipes(queryEn)

            result.fold(
                onSuccess = { response ->
                    _recommendationsUiState.value = RecommendationsUiState.Success(response.results)
                },
                onFailure = {
                    _recommendationsUiState.value = RecommendationsUiState.Error("Error de red")
                }
            )
        } catch (e: Exception) {
            _recommendationsUiState.value = RecommendationsUiState.Error("Error inesperado")
        }
    }
}
