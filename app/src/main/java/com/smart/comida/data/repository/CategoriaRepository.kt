package com.smart.comida.data.repository

import com.smart.comida.data.model.Categoria
import com.smart.comida.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class CategoriaRepository {

    private fun requireUserId(): String {
        return SupabaseClient.currentUserId
            ?: throw IllegalStateException("Usuario no autenticado")
    }

    suspend fun obtenerCategorias(): Result<List<Categoria>> {
        return try {
            val userId = requireUserId()
            val lista = SupabaseClient.client.postgrest["categorias"]
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<Categoria>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun crearCategoria(nombre: String): Result<Unit> {
        return try {
            val userId = requireUserId()
            val nuevaCategoria = Categoria(
                nombre = nombre,
                userId = userId
            )
            SupabaseClient.client.postgrest["categorias"]
                .insert(nuevaCategoria)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarCategoria(id: Int, nombre: String): Result<Unit> {
        return try {
            val userId = requireUserId()
            val body = buildJsonObject {
                put("nombre", nombre)
            }
            SupabaseClient.client.postgrest["categorias"]
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

    suspend fun eliminarCategoria(id: Int): Result<Unit> {
        return try {
            val userId = requireUserId()
            SupabaseClient.client.postgrest["categorias"]
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
}
