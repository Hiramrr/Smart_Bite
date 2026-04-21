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

    // NUEVA FUNCIÓN: Descargar categorías
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

    // NUEVA FUNCIÓN: Validar duplicados (FA-02)
    suspend fun existeIngrediente(nombreIngrediente: String): Boolean {
        return try {
            val coincidencias = SupabaseClient.client.postgrest["ingredientes"]
                // Buscamos si existe exactamente ese nombre
                .select { filter { eq("nombre", nombreIngrediente) } }
                .decodeList<Ingrediente>()

            coincidencias.isNotEmpty() // Devuelve true si encontró alguno
        } catch (e: Exception) {
            false
        }
    }

    // NUEVA FUNCIÓN: Eliminar ingrediente
    suspend fun eliminarIngrediente(id: Int): Result<Unit> {
        return try {
            SupabaseClient.client.postgrest["ingredientes"]
                .delete { filter { eq("id", id) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registrarComoDesperdicio(ingrediente: Ingrediente): Result<Unit> {
        val ingredienteId = ingrediente.id
            ?: return Result.failure(IllegalArgumentException("Ingrediente sin ID válido"))
        val fechaDesecho = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        val desperdicio = Desperdicio(
            nombre = ingrediente.nombre,
            cantidad = ingrediente.cantidad,
            unidad = ingrediente.unidad,
            fechaCaducidad = ingrediente.fechaCaducidad,
            categoriaId = ingrediente.categoriaId,
            fechaDesecho = fechaDesecho
        )

        return try {
            SupabaseClient.client.postgrest["historial_desperdicio"]
                .insert(desperdicio)

            try {
                SupabaseClient.client.postgrest["ingredientes"]
                    .delete { filter { eq("id", ingredienteId) } }
            } catch (deleteError: Exception) {
                // Si falla la eliminación en inventario, revertimos el historial insertado.
                runCatching {
                    SupabaseClient.client.postgrest["historial_desperdicio"]
                        .delete {
                            filter {
                                eq("nombre", ingrediente.nombre)
                                eq("cantidad", ingrediente.cantidad)
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

    // NUEVA FUNCIÓN: Actualizar los datos de un ingrediente
    suspend fun actualizarIngrediente(
        id: Int, nombre: String, cantidad: Float,
        unidad: String?, fechaCaducidad: String?, categoriaId: Int?,
        imagenUrl: String? = null // --- NUEVO PARÁMETRO ---
    ): Result<Unit> {
        return try {
            val ingredienteActualizado = Ingrediente(
                id = id, // Es vital pasar el ID para que Supabase sepa cuál editar
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

    // NUEVA FUNCIÓN: Subir imagen a Supabase Storage
    suspend fun subirImagen(byteArray: ByteArray, nombreArchivo: String): Result<String> {
        return try {
            // Nos conectamos a la "caja" que creaste
            val bucket = SupabaseClient.client.storage["ingredientes_imagenes"]

            // Creamos un nombre único para el archivo (ej. tomate_168439.jpg)
            val rutaArchivo = "$nombreArchivo.jpg"

            // Subimos la imagen (upsert = true permite sobreescribir si ya existe uno con ese nombre)
            bucket.upload(rutaArchivo, byteArray, upsert = true)

            // Pedimos la URL pública de la imagen recién subida
            val urlPublica = bucket.publicUrl(rutaArchivo)

            Result.success(urlPublica) // Devolvemos el link
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // NUEVA FUNCIÓN: Eliminar imagen vieja para liberar espacio
    suspend fun eliminarImagen(urlPublica: String) {
        try {
            // Extraemos el nombre del archivo de la URL
            // Ejemplo: si la URL termina en /foto_1234.jpg, esto extrae "foto_1234.jpg"
            val nombreArchivo = urlPublica.substringAfterLast("/")

            if (nombreArchivo.isNotBlank()) {
                val bucket = SupabaseClient.client.storage["ingredientes_imagenes"]
                bucket.delete(nombreArchivo) // Le decimos a la nube que lo borre
            }
        } catch (e: Exception) {
            // Si falla el borrado, lo atrapamos aquí para que la app no se trabe
            // y permita seguir guardando los datos del ingrediente.
            e.printStackTrace()
        }
    }
}
