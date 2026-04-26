package com.smart.comida.data.repository

import com.smart.comida.data.model.ArticuloCompra
import com.smart.comida.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class ListaComprasRepository {

    suspend fun agregarArticulo(nombre: String, cantidadEsperada: String): Result<Unit> {
        return try {
            val nuevoArticulo = ArticuloCompra(
                nombre = nombre,
                cantidadEsperada = cantidadEsperada
            )

            SupabaseClient.client.postgrest["lista_compras"]
                .insert(nuevoArticulo)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerArticulos(): Result<List<ArticuloCompra>> {
        return try {
            val lista = SupabaseClient.client.postgrest["lista_compras"]
                .select()
                .decodeList<ArticuloCompra>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarArticulo(id: Int): Result<Unit> {
        return try {
            SupabaseClient.client.postgrest["lista_compras"]
                .delete { filter { eq("id", id) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
