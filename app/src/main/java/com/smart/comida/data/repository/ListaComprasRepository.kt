package com.smart.comida.data.repository

import com.smart.comida.data.model.ArticuloCompra
import com.smart.comida.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonNull

class ListaComprasRepository {

    suspend fun agregarArticulo(nombre: String, cantidadEsperada: Double?, unidad: String?): Result<Unit> {
        return try {
            val nuevoArticulo = ArticuloCompra(
                nombre = nombre,
                cantidadEsperada = cantidadEsperada,
                unidad = unidad,
                estado = "Pendiente"
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

    suspend fun actualizarEstado(id: Int, estado: String): Result<Unit> {
        return try {
            SupabaseClient.client.postgrest["lista_compras"]
                .update(mapOf("estado" to estado)) { filter { eq("id", id) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarArticulo(id: Int, nombre: String, cantidadEsperada: Double?, unidad: String?): Result<Unit> {
        return try {
            val body = buildJsonObject {
                put("nombre", nombre)
                if (cantidadEsperada != null) put("cantidad_esperada", cantidadEsperada) else put("cantidad_esperada", JsonNull)
                if (unidad != null) put("unidad", unidad) else put("unidad", JsonNull)
            }
            SupabaseClient.client.postgrest["lista_compras"]
                .update(body) { filter { eq("id", id) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
