package com.smart.comida.data.repository

import com.smart.comida.data.Ingrediente
import com.smart.comida.data.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class InventarioRepository {

    suspend fun agregarIngrediente(
        nombre: String,
        cantidad: Float,
        unidad: String?,
        fechaCaducidad: String?,
        categoriaId: Int?
    ): Result<Unit> {
        return try {
            val nuevoIngrediente = Ingrediente(
                nombre = nombre,
                cantidad = cantidad,
                unidad = unidad,
                fechaCaducidad = fechaCaducidad,
                categoriaId = categoriaId
            )

            // Inserta el registro en la tabla "ingredientes"
            SupabaseClient.client.postgrest["ingredientes"]
                .insert(nuevoIngrediente)

            Result.success(Unit) // Éxito
        } catch (e: Exception) {
            // Manejo de la Excepción Ex-01: 'Error al guardar los datos'
            Result.failure(e)
        }
    }
}