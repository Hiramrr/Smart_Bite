package com.smart.comida.data.repository

import com.smart.comida.data.model.ArticuloCompra
import com.smart.comida.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonNull

class ListaComprasRepository {

    private fun requireUserId(): String {
        return SupabaseClient.currentUserId
            ?: throw IllegalStateException("Usuario no autenticado")
    }

    suspend fun agregarArticulos(articulos: List<ArticuloCompra>): Result<Unit> {
        return try {
            val userId = requireUserId()
            var errorCount = 0
            var lastError: Exception? = null

            for (articulo in articulos) {
                try {
                    val nuevoArticulo = ArticuloCompra(
                        nombre = articulo.nombre,
                        cantidadEsperada = articulo.cantidadEsperada,
                        unidad = articulo.unidad,
                        estado = articulo.estado ?: "Pendiente",
                        userId = userId
                    )

                    SupabaseClient.client.postgrest["lista_compras"]
                        .insert(nuevoArticulo)
                } catch (e: Exception) {
                    errorCount++
                    lastError = e
                }
            }

            if (errorCount > 0 && errorCount == articulos.size) {
                Result.failure(lastError ?: Exception("Error al insertar artículos"))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun agregarArticulo(nombre: String, cantidadEsperada: Double?, unidad: String?): Result<Unit> {
        return try {
            val userId = requireUserId()
            val nuevoArticulo = ArticuloCompra(
                nombre = nombre,
                cantidadEsperada = cantidadEsperada,
                unidad = unidad,
                estado = "Pendiente",
                userId = userId
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
            val userId = requireUserId()
            val lista = SupabaseClient.client.postgrest["lista_compras"]
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<ArticuloCompra>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarArticulo(id: Int): Result<Unit> {
        return try {
            val userId = requireUserId()
            SupabaseClient.client.postgrest["lista_compras"]
                .delete {
                    filter {
                        eq("id", id)
                        eq("user_id", userId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarEstado(id: Int, estado: String): Result<Unit> {
        return try {
            val userId = requireUserId()
            SupabaseClient.client.postgrest["lista_compras"]
                .update(mapOf("estado" to estado)) {
                    filter {
                        eq("id", id)
                        eq("user_id", userId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarArticulo(id: Int, nombre: String, cantidadEsperada: Double?, unidad: String?): Result<Unit> {
        return try {
            val userId = requireUserId()
            val body = buildJsonObject {
                put("nombre", nombre)
                if (cantidadEsperada != null) put("cantidad_esperada", cantidadEsperada) else put("cantidad_esperada", JsonNull)
                if (unidad != null) put("unidad", unidad) else put("unidad", JsonNull)
            }
            SupabaseClient.client.postgrest["lista_compras"]
                .update(body) {
                    filter {
                        eq("id", id)
                        eq("user_id", userId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
