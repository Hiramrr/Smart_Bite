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

    // se implemento esta funcion para descargar la lista de la despensa
    suspend fun obtenerIngredientes(): Result<List<Ingrediente>> {
        return try {
            // "select()" trae todos los registros de la tabla
            // "decodeList" los convierte automáticamente a nuestra clase Ingrediente
            val lista = SupabaseClient.client.postgrest["ingredientes"]
                .select()
                .decodeList<Ingrediente>()

            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}