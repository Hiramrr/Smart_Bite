package com.smart.comida.data.repository

import com.smart.comida.data.model.Categoria
import com.smart.comida.data.model.Desperdicio
import com.smart.comida.data.model.Ingrediente
import com.smart.comida.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class InventarioRepository {

    suspend fun agregarIngrediente(
        nombre: String,
        cantidad: Float,
        unidad: String?,
        fechaCaducidad: String?,
        categoriaId: Int?,
        imagenUrl: String? = null
    ): Result<Unit> {
        return try {
            val nuevoIngrediente = Ingrediente(
                nombre = nombre,
                cantidad = cantidad,
                unidad = unidad,
                fechaCaducidad = fechaCaducidad,
                categoriaId = categoriaId,
                imagenUrl = imagenUrl
            )

            SupabaseClient.client.postgrest["ingredientes"]
                .insert(nuevoIngrediente)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerIngredientes(): Result<List<Ingrediente>> {
        return try {
            val lista = SupabaseClient.client.postgrest["ingredientes"]
                .select()
                .decodeList<Ingrediente>()

            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerCategorias(): Result<List<Categoria>> {
        return try {
            val lista = SupabaseClient.client.postgrest["categorias"]
                .select()
                .decodeList<Categoria>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun existeIngrediente(nombreIngrediente: String): Boolean {
        return try {
            val coincidencias = SupabaseClient.client.postgrest["ingredientes"]
                .select { filter { eq("nombre", nombreIngrediente) } }
                .decodeList<Ingrediente>()

            coincidencias.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun eliminarIngrediente(id: Int): Result<Unit> {
        return try {
            SupabaseClient.client.postgrest["ingredientes"]
                .delete { filter { eq("id", id) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registrarComoDesperdicio(ingrediente: Ingrediente, cantidadDesperdicio: Float): Result<Unit> {
        val ingredienteId = ingrediente.id
            ?: return Result.failure(IllegalArgumentException("Ingrediente sin ID válido"))
        val fechaDesecho = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        val desperdicio = Desperdicio(
            nombre = ingrediente.nombre,
            cantidad = cantidadDesperdicio,
            unidad = ingrediente.unidad,
            fechaCaducidad = ingrediente.fechaCaducidad,
            categoriaId = ingrediente.categoriaId,
            fechaDesecho = fechaDesecho
        )

        return try {
            SupabaseClient.client.postgrest["historial_desperdicio"]
                .insert(desperdicio)

            try {
                val nuevaCantidad = ingrediente.cantidad - cantidadDesperdicio
                if (nuevaCantidad <= 0) {
                    SupabaseClient.client.postgrest["ingredientes"]
                        .delete { filter { eq("id", ingredienteId) } }
                } else {
                    val ingredienteActualizado = Ingrediente(
                        id = ingredienteId,
                        nombre = ingrediente.nombre,
                        cantidad = nuevaCantidad,
                        unidad = ingrediente.unidad,
                        fechaCaducidad = ingrediente.fechaCaducidad,
                        categoriaId = ingrediente.categoriaId,
                        imagenUrl = ingrediente.imagenUrl
                    )
                    SupabaseClient.client.postgrest["ingredientes"]
                        .update(ingredienteActualizado) { filter { eq("id", ingredienteId) } }
                }
            } catch (deleteError: Exception) {
                runCatching {
                    SupabaseClient.client.postgrest["historial_desperdicio"]
                        .delete {
                            filter {
                                eq("nombre", ingrediente.nombre)
                                eq("cantidad", cantidadDesperdicio)
                                eq("fecha_desecho", fechaDesecho)
                            }
                        }
                }
                return Result.failure(deleteError)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerHistorialDesperdicio(): Result<List<Desperdicio>> {
        return try {
            val lista = SupabaseClient.client.postgrest["historial_desperdicio"]
                .select()
                .decodeList<Desperdicio>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // NUEVA FUNCIÓN: Obtener un ingrediente específico por su ID
    suspend fun obtenerIngredientePorId(id: Int): Result<Ingrediente> {
        return try {
            val ingrediente = SupabaseClient.client.postgrest["ingredientes"]
                .select { filter { eq("id", id) } }
                .decodeSingle<Ingrediente>() // decodeSingle porque solo esperamos uno
            Result.success(ingrediente)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarIngrediente(
        id: Int, nombre: String, cantidad: Float,
        unidad: String?, fechaCaducidad: String?, categoriaId: Int?,
        imagenUrl: String? = null
    ): Result<Unit> {
        return try {
            val ingredienteActualizado = Ingrediente(
                id = id,
                nombre = nombre, cantidad = cantidad, unidad = unidad,
                fechaCaducidad = fechaCaducidad, categoriaId = categoriaId,
                imagenUrl = imagenUrl // --- NUEVO ---
            )

            SupabaseClient.client.postgrest["ingredientes"]
                .update(ingredienteActualizado) { filter { eq("id", id) } }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun subirImagen(byteArray: ByteArray, nombreArchivo: String): Result<String> {
        return try {
            val bucket = SupabaseClient.client.storage["ingredientes_imagenes"]

            val rutaArchivo = "$nombreArchivo.jpg"

            bucket.upload(rutaArchivo, byteArray, upsert = true)

            val urlPublica = bucket.publicUrl(rutaArchivo)

            Result.success(urlPublica)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarImagen(urlPublica: String) {
        try {

            val nombreArchivo = urlPublica.substringAfterLast("/")

            if (nombreArchivo.isNotBlank()) {
                val bucket = SupabaseClient.client.storage["ingredientes_imagenes"]
                bucket.delete(nombreArchivo)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
