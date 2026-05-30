package com.smart.comida.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartbite.data.Ingredient
import com.smart.comida.data.model.Ingrediente
import com.smart.comida.data.repository.DescuentoIngredienteInventario
import com.smart.comida.data.repository.InventarioRepository
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.time.LocalDate
import java.util.Locale
import kotlin.math.min

data class ResumenIngredienteDescuento(
    val nombre: String,
    val cantidad: Double,
    val unidad: String?
)

sealed class PrepararRecetaUiState {
    object Idle : PrepararRecetaUiState()
    object Checking : PrepararRecetaUiState()
    data class ReadyToConfirm(val descuentos: List<ResumenIngredienteDescuento>) : PrepararRecetaUiState()
    data class Insufficient(val faltantes: List<IngredienteFaltante>) : PrepararRecetaUiState()
    object Updating : PrepararRecetaUiState()
    data class Success(val message: String) : PrepararRecetaUiState()
    data class Error(val message: String, val throwable: Throwable? = null) : PrepararRecetaUiState()
}

class PrepararRecetaViewModel : ViewModel() {
    private val repository = InventarioRepository()

    var uiState by mutableStateOf<PrepararRecetaUiState>(PrepararRecetaUiState.Idle)
        private set

    private var ultimosIngredientesReceta: List<Ingredient> = emptyList()

    fun verificarIngredientes(ingredientesReceta: List<Ingredient>) {
        if (ingredientesReceta.isEmpty()) {
            uiState = PrepararRecetaUiState.Error("La receta no tiene ingredientes para descontar.")
            return
        }

        ultimosIngredientesReceta = ingredientesReceta
        uiState = PrepararRecetaUiState.Checking

        viewModelScope.launch {
            repository.obtenerIngredientes()
                .onSuccess { inventario ->
                    val plan = crearPlanPreparacion(ingredientesReceta, inventario)
                    uiState = if (plan.faltantes.isEmpty()) {
                        PrepararRecetaUiState.ReadyToConfirm(plan.resumen)
                    } else {
                        PrepararRecetaUiState.Insufficient(plan.faltantes)
                    }
                }
                .onFailure {
                    uiState = PrepararRecetaUiState.Error(
                        "No se pudo acceder al inventario. Verifica tu conexión.",
                        it
                    )
                }
        }
    }

    fun confirmarPreparacion() {
        val ingredientesReceta = ultimosIngredientesReceta
        if (ingredientesReceta.isEmpty()) {
            uiState = PrepararRecetaUiState.Error("No hay una receta pendiente por preparar.")
            return
        }

        uiState = PrepararRecetaUiState.Updating

        viewModelScope.launch {
            repository.obtenerIngredientes()
                .onSuccess { inventario ->
                    val plan = crearPlanPreparacion(ingredientesReceta, inventario)
                    if (plan.faltantes.isNotEmpty()) {
                        uiState = PrepararRecetaUiState.Insufficient(plan.faltantes)
                        return@onSuccess
                    }

                    repository.descontarIngredientes(plan.descuentos)
                        .onSuccess {
                            uiState = PrepararRecetaUiState.Success(
                                "Receta preparada. Ingredientes descontados de la despensa."
                            )
                        }
                        .onFailure {
                            uiState = PrepararRecetaUiState.Error(
                                "Error al actualizar el inventario. No se aplicó la preparación.",
                                it
                            )
                        }
                }
                .onFailure {
                    uiState = PrepararRecetaUiState.Error(
                        "No se pudo validar el inventario antes de preparar la receta.",
                        it
                    )
                }
        }
    }

    fun cancelarPreparacion() {
        uiState = PrepararRecetaUiState.Idle
    }

    fun resetState() {
        uiState = PrepararRecetaUiState.Idle
    }

    private fun crearPlanPreparacion(
        ingredientesReceta: List<Ingredient>,
        inventario: List<Ingrediente>
    ): PlanPreparacion {
        val faltantes = mutableListOf<IngredienteFaltante>()
        val cantidadesDisponibles = inventario
            .mapNotNull { ingrediente ->
                ingrediente.id?.let { id -> id to ingrediente.cantidad.toDouble() }
            }
            .toMap()
            .toMutableMap()
        val descuentosPorIngrediente = linkedMapOf<Int, DescuentoIngredienteInventario>()

        ingredientesReceta.forEach { ingredienteReceta ->
            val requerimiento = ingredienteReceta.toRequerimiento()
            val coincidencias = inventario.filter { ingrediente ->
                ingrediente.id != null && nombresCoinciden(ingrediente.nombre, requerimiento.nombresCandidatos)
            }
            val compatibles = coincidencias.filter { ingrediente ->
                unidadesCompatibles(ingrediente.unidad, requerimiento.unidadOriginal)
            }

            val totalDisponibleBase = compatibles.sumOf { ingrediente ->
                val unidadInventario = normalizarUnidad(ingrediente.unidad)
                val disponible = cantidadesDisponibles[ingrediente.id] ?: 0.0
                disponible * unidadInventario.factorBase
            }

            val cantidadRequeridaBase = requerimiento.cantidad * requerimiento.unidad.factorBase
            if (compatibles.isEmpty() || totalDisponibleBase + TOLERANCIA < cantidadRequeridaBase) {
                val cantidadDisponible = totalDisponibleBase / requerimiento.unidad.factorBase
                faltantes.add(
                    IngredienteFaltante(
                        nombre = requerimiento.nombre,
                        cantidad = (requerimiento.cantidad - cantidadDisponible).coerceAtLeast(0.0),
                        unidad = requerimiento.unidad.nombreVisible
                    )
                )
                return@forEach
            }

            var restanteBase = cantidadRequeridaBase
            compatibles
                .sortedWith(compareBy<Ingrediente> { fechaCaducidadOrden(it.fechaCaducidad) }.thenBy { it.id })
                .forEach { ingrediente ->
                    if (restanteBase <= TOLERANCIA) return@forEach

                    val ingredienteId = ingrediente.id ?: return@forEach
                    val unidadInventario = normalizarUnidad(ingrediente.unidad)
                    val disponible = cantidadesDisponibles[ingredienteId] ?: 0.0
                    if (disponible <= TOLERANCIA) return@forEach

                    val disponibleBase = disponible * unidadInventario.factorBase
                    val descuentoBase = min(restanteBase, disponibleBase)
                    val descuentoEnUnidadInventario = descuentoBase / unidadInventario.factorBase

                    cantidadesDisponibles[ingredienteId] = disponible - descuentoEnUnidadInventario
                    val descuentoActual = descuentosPorIngrediente[ingredienteId]?.cantidadADescontar ?: 0f
                    descuentosPorIngrediente[ingredienteId] = DescuentoIngredienteInventario(
                        ingrediente = ingrediente,
                        cantidadADescontar = descuentoActual + descuentoEnUnidadInventario.toFloat()
                    )

                    restanteBase -= descuentoBase
                }
        }

        val resumen = descuentosPorIngrediente.values.map { descuento ->
            ResumenIngredienteDescuento(
                nombre = descuento.ingrediente.nombre,
                cantidad = descuento.cantidadADescontar.toDouble(),
                unidad = descuento.ingrediente.unidad
            )
        }

        return PlanPreparacion(
            descuentos = descuentosPorIngrediente.values.toList(),
            resumen = resumen,
            faltantes = faltantes
        )
    }

    private fun Ingredient.toRequerimiento(): RequerimientoIngrediente {
        val unidadMetric = measures?.metric?.unitShort?.takeIf { it.isNotBlank() }
            ?: measures?.metric?.unitLong?.takeIf { it.isNotBlank() }
        val cantidadExtraida = extraerCantidadDeOriginal(original).second
        val cantidad = measures?.metric?.amount
            ?: amount
            ?: cantidadExtraida
            ?: 1.0
        val unidadOriginal = unidadMetric ?: unit?.takeIf { it.isNotBlank() }
        val unidadNormalizada = normalizarUnidad(unidadOriginal)
        val nombreOriginalLimpio = limpiarNombreOriginal(original)
        val candidatos = listOfNotNull(name, nombreOriginalLimpio)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        return RequerimientoIngrediente(
            nombre = candidatos.firstOrNull() ?: original,
            nombresCandidatos = candidatos.ifEmpty { listOf(original) },
            cantidad = cantidad,
            unidadOriginal = unidadOriginal,
            unidad = unidadNormalizada
        )
    }

    private fun unidadesCompatibles(unidadInventario: String?, unidadReceta: String?): Boolean {
        val inventario = normalizarUnidad(unidadInventario)
        val receta = normalizarUnidad(unidadReceta)

        if (inventario.tipo != UnidadTipo.DESCONOCIDA && receta.tipo != UnidadTipo.DESCONOCIDA) {
            return inventario.tipo == receta.tipo
        }

        return normalizarTexto(unidadInventario.orEmpty()) == normalizarTexto(unidadReceta.orEmpty())
    }

    private fun nombresCoinciden(nombreInventario: String, candidatosReceta: List<String>): Boolean {
        val inventario = normalizarNombre(nombreInventario)
        if (inventario.isBlank()) return false

        return candidatosReceta.any { candidato ->
            val receta = normalizarNombre(candidato)
            receta == inventario ||
                (receta.length >= 4 && inventario.contains(receta)) ||
                (inventario.length >= 4 && receta.contains(inventario))
        }
    }

    private fun normalizarUnidad(unidad: String?): UnidadNormalizada {
        val unidadNormalizada = normalizarTexto(unidad.orEmpty())

        return when (unidadNormalizada) {
            "", "pieza", "piezas", "unidad", "unidades", "unit", "units", "piece", "pieces",
            "serving", "servings", "porcion", "porciones", "large", "medium", "small",
            "grande", "mediano", "pequeno", "pequena", "diente", "dientes", "clove", "cloves",
            "rebanada", "rebanadas", "slice", "slices" -> UnidadNormalizada(UnidadTipo.PIEZAS, 1.0, "Piezas")

            "kg", "kilogramo", "kilogramos", "kilogram", "kilograms" ->
                UnidadNormalizada(UnidadTipo.MASA, 1000.0, "Kg")

            "g", "gr", "gramo", "gramos", "gram", "grams" ->
                UnidadNormalizada(UnidadTipo.MASA, 1.0, "Gramos")

            "l", "lt", "litro", "litros", "liter", "liters", "litre", "litres" ->
                UnidadNormalizada(UnidadTipo.VOLUMEN, 1000.0, "Litros")

            "ml", "mililitro", "mililitros", "milliliter", "milliliters", "millilitre", "millilitres" ->
                UnidadNormalizada(UnidadTipo.VOLUMEN, 1.0, "ml")

            "cup", "cups", "taza", "tazas" ->
                UnidadNormalizada(UnidadTipo.VOLUMEN, 240.0, "tazas")

            "tbsp", "tablespoon", "tablespoons", "cucharada", "cucharadas" ->
                UnidadNormalizada(UnidadTipo.VOLUMEN, 15.0, "cucharadas")

            "tsp", "teaspoon", "teaspoons", "cucharadita", "cucharaditas" ->
                UnidadNormalizada(UnidadTipo.VOLUMEN, 5.0, "cucharaditas")

            else -> UnidadNormalizada(UnidadTipo.DESCONOCIDA, 1.0, unidad?.takeIf { it.isNotBlank() })
        }
    }

    private fun limpiarNombreOriginal(original: String): String {
        val sinCantidad = original.trim()
            .replace(Regex("^([\\d]+([.,][\\d]+)?|[\\d]+/[\\d]+)\\s*"), "")
            .substringBefore(",")
            .substringBefore("(")
            .trim()

        return sinCantidad.replace(
            Regex(
                "^(kg|kilogramos?|g|gr|gramos?|ml|mililitros?|l|litros?|tazas?|cucharadas?|cucharaditas?|piezas?|unidades?|dientes?|rebanadas?)\\s+(de\\s+)?",
                RegexOption.IGNORE_CASE
            ),
            ""
        ).trim()
    }

    private fun extraerCantidadDeOriginal(original: String): Pair<String, Double?> {
        val regex = Regex("^([\\d/]+(?:[.,][\\d]+)?)\\s*(.*)$")
        val matchResult = regex.find(original.trim()) ?: return Pair(original, null)
        val cantidadStr = matchResult.groupValues[1]
        val resto = matchResult.groupValues[2].trim()
        return Pair(resto, parsearCantidad(cantidadStr))
    }

    private fun parsearCantidad(cantidadStr: String): Double? {
        if (cantidadStr.contains("/")) {
            val partes = cantidadStr.split("/")
            if (partes.size == 2) {
                val numerador = partes[0].toDoubleOrNull()
                val denominador = partes[1].toDoubleOrNull()
                if (denominador != null && denominador != 0.0) {
                    return (numerador ?: 1.0) / denominador
                }
            }
        }
        return cantidadStr.replace(',', '.').toDoubleOrNull()
    }

    private fun normalizarNombre(texto: String): String {
        val stopWords = setOf(
            "de", "del", "la", "el", "las", "los", "un", "una", "y", "en", "con",
            "fresco", "fresca", "frescos", "frescas", "picado", "picada", "picados",
            "picadas", "cortado", "cortada", "cortados", "cortadas", "rallado",
            "rallada", "molido", "molida", "grande", "grandes", "pequeno", "pequena"
        )

        return normalizarTexto(texto)
            .split(" ")
            .filter { it.isNotBlank() && it !in stopWords }
            .joinToString(" ")
    }

    private fun normalizarTexto(texto: String): String {
        val sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")

        return sinAcentos
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9ñ ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun fechaCaducidadOrden(fechaCaducidad: String?): LocalDate {
        return fechaCaducidad?.let { fecha ->
            runCatching { LocalDate.parse(fecha) }.getOrNull()
        } ?: LocalDate.MAX
    }

    private data class PlanPreparacion(
        val descuentos: List<DescuentoIngredienteInventario>,
        val resumen: List<ResumenIngredienteDescuento>,
        val faltantes: List<IngredienteFaltante>
    )

    private data class RequerimientoIngrediente(
        val nombre: String,
        val nombresCandidatos: List<String>,
        val cantidad: Double,
        val unidadOriginal: String?,
        val unidad: UnidadNormalizada
    )

    private data class UnidadNormalizada(
        val tipo: UnidadTipo,
        val factorBase: Double,
        val nombreVisible: String?
    )

    private enum class UnidadTipo {
        MASA,
        VOLUMEN,
        PIEZAS,
        DESCONOCIDA
    }

    private companion object {
        const val TOLERANCIA = 0.0001
    }
}
