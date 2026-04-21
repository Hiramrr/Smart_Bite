package com.smart.comida.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartbite.data.Recipe
import com.example.smartbite.data.RecipeDetail
import com.example.smartbite.data.repository.RecipeRepository
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

// 2. Creamos el ViewModel
class RecipeViewModel : ViewModel() {

    private val repository = RecipeRepository()

    private val _uiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Idle)

    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()


    fun searchRecipes(query: String) {
        // 1. Inmediatamente avisamos que estamos cargando
        _uiState.value = RecipeUiState.Loading

        // 2. viewModelScope lanza la petición en un hilo secundario para no trabar el celular
        viewModelScope.launch {
            val result = repository.searchRecipes(query)

            // 3. Evaluamos el resultado usando el fold (éxito o fracaso)
            result.fold(
                onSuccess = { response ->
                    // FA-01: 'Sin resultados de la API'
                    if (response.results.isEmpty()) {
                        _uiState.value = RecipeUiState.Error("No se encontraron recetas con '$query'.")
                    } else {
                        // Flujo normal: Mostramos resultados
                        _uiState.value = RecipeUiState.SearchSuccess(response.results)
                    }
                },
                onFailure = { error ->
                    // Ex-01: 'Error de conexión'
                    _uiState.value = RecipeUiState.Error("Error de red: Verifica tu conexión a internet.")
                }
            )
        }
    }

    fun getRecipeDetail(id: Int) {
        _uiState.value = RecipeUiState.Loading

        viewModelScope.launch {
            val result = repository.getRecipeDetails(id)

            result.fold(
                onSuccess = { detail ->
                    _uiState.value = RecipeUiState.DetailSuccess(detail)
                },
                onFailure = { error ->
                    _uiState.value = RecipeUiState.Error("No se pudo obtener el detalle de la receta.")
                }
            )
        }
    }
}