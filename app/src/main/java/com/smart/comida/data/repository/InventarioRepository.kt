package com.smart.comida.data.repository

import com.smart.comida.data.model.Categoria
import com.smart.comida.data.model.Consumo
import com.smart.comida.data.model.Desperdicio
import com.smart.comida.data.model.Ingrediente
import com.smart.comida.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

data class DescuentoIngredienteInventario(
    val ingrediente: Ingrediente,
    val cantidadADescontar: Float
)

@Serializable
private data class DescuentoPreparacionRpc(
    @SerialName("ingrediente_id") val ingredienteId: Int,
    val cantidad: Float
)

@Serializable
private data class PrepararRecetaRpcParams(
    @SerialName("p_descuentos") val descuentos: List<DescuentoPreparacionRpc>
)

class InventarioRepository {

    private fun requireUserId(): String {
        return SupabaseClient.currentUserId
            ?: throw IllegalStateException("Usuario no autenticado")
    }

    suspend fun agregarIngrediente(
        nombre: String,
        cantidad: Float,
        unidad: String?,
        fechaCaducidad: String?,
        categoriaId: Int?,
        imagenUrl: String? = null
    ): Result<Int> {
        return try {
            val userId = requireUserId()
            val nuevoIngrediente = Ingrediente(
                nombre = nombre,
                cantidad = cantidad,
                unidad = unidad,
                fechaCaducidad = fechaCaducidad,
                categoriaId = categoriaId,
                imagenUrl = imagenUrl,
                userId = userId
            )

            val resultado = SupabaseClient.client.postgrest["ingredientes"]
                .insert(nuevoIngrediente) {
                    select()
                }
                .decodeSingle<Ingrediente>()

            Result.success(resultado.id ?: -1)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarImagenIngrediente(id: Int, imagenUrl: String): Result<Unit> {
        return try {
            val userId = requireUserId()
            SupabaseClient.client.postgrest["ingredientes"]
                .update("{ \"imagen_url\": \"$imagenUrl\" }") {
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

    suspend fun obtenerIngredientes(): Result<List<Ingrediente>> {
        return try {
            val userId = requireUserId()
            val lista = SupabaseClient.client.postgrest["ingredientes"]
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<Ingrediente>()

            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
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

    suspend fun existeIngrediente(nombreIngrediente: String): Boolean {
        return try {
            val userId = requireUserId()
            val coincidencias = SupabaseClient.client.postgrest["ingredientes"]
                .select {
                    filter {
                        eq("nombre", nombreIngrediente)
                        eq("user_id", userId)
                    }
                }
                .decodeList<Ingrediente>()

            coincidencias.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun eliminarIngrediente(id: Int): Result<Unit> {
        return try {
            val userId = requireUserId()
            SupabaseClient.client.postgrest["ingredientes"]
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

    suspend fun registrarComoDesperdicio(ingrediente: Ingrediente, cantidadDesperdicio: Float): Result<Unit> {
        val ingredienteId = ingrediente.id
            ?: return Result.failure(IllegalArgumentException("Ingrediente sin ID válido"))
        if (!puedeRegistrarseComoDesperdicio(ingrediente.fechaCaducidad)) {
            return Result.failure(
                IllegalArgumentException("Solo puedes desechar ingredientes caducados o que vencen en los próximos 7 días")
            )
        }
        if (abs(cantidadDesperdicio - ingrediente.cantidad) > 0.0001f) {
            return Result.failure(
                IllegalArgumentException("Debes registrar como desperdicio la cantidad completa del ingrediente")
            )
        }
        val userId = requireUserId()
        val fechaDesecho = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        val desperdicio = Desperdicio(
            nombre = ingrediente.nombre,
            cantidad = cantidadDesperdicio,
            unidad = ingrediente.unidad,
            fechaCaducidad = ingrediente.fechaCaducidad,
            categoriaId = ingrediente.categoriaId,
            fechaDesecho = fechaDesecho,
            userId = userId
        )

        return try {
            SupabaseClient.client.postgrest["historial_desperdicio"]
                .insert(desperdicio)

            try {
                val nuevaCantidad = ingrediente.cantidad - cantidadDesperdicio
                if (nuevaCantidad <= 0) {
                    SupabaseClient.client.postgrest["ingredientes"]
                        .delete {
                            filter {
                                eq("id", ingredienteId)
                                eq("user_id", userId)
                            }
                        }
                } else {
                    val ingredienteActualizado = Ingrediente(
                        id = ingredienteId,
                        nombre = ingrediente.nombre,
                        cantidad = nuevaCantidad,
                        unidad = ingrediente.unidad,
                        fechaCaducidad = ingrediente.fechaCaducidad,
                        categoriaId = ingrediente.categoriaId,
                        imagenUrl = ingrediente.imagenUrl,
                        userId = userId
                    )
                    SupabaseClient.client.postgrest["ingredientes"]
                        .update(ingredienteActualizado) {
                            filter {
                                eq("id", ingredienteId)
                                eq("user_id", userId)
                            }
                        }
                }
            } catch (deleteError: Exception) {
                runCatching {
                    SupabaseClient.client.postgrest["historial_desperdicio"]
                        .delete {
                            filter {
                                eq("nombre", ingrediente.nombre)
                                eq("cantidad", cantidadDesperdicio)
                                eq("fecha_desecho", fechaDesecho)
                                eq("user_id", userId)
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
            val userId = requireUserId()
            val lista = SupabaseClient.client.postgrest["historial_desperdicio"]
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<Desperdicio>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerIngredientePorId(id: Int): Result<Ingrediente> {
        return try {
            val userId = requireUserId()
            val ingrediente = SupabaseClient.client.postgrest["ingredientes"]
                .select {
                    filter {
                        eq("id", id)
                        eq("user_id", userId)
                    }
                }
                .decodeSingle<Ingrediente>()
            Result.success(ingrediente)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerDesperdiciosPorMes(mes: Int, anio: Int): Result<List<Desperdicio>> {
        return try {
            val userId = requireUserId()
            val inicioMes = String.format("%04d-%02d-01T00:00:00+00:00", anio, mes)
            val finMes = if (mes == 12) {
                String.format("%04d-01-01T00:00:00+00:00", anio + 1)
            } else {
                String.format("%04d-%02d-01T00:00:00+00:00", anio, mes + 1)
            }

            val lista = SupabaseClient.client.postgrest["historial_desperdicio"]
                .select {
                    filter {
                        eq("user_id", userId)
                        gte("fecha_desecho", inicioMes)
                        lt("fecha_desecho", finMes)
                    }
                }
                .decodeList<Desperdicio>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerConsumosPorMes(mes: Int, anio: Int): Result<List<Consumo>> {
        return try {
            val userId = requireUserId()
            val inicioMes = String.format("%04d-%02d-01T00:00:00+00:00", anio, mes)
            val finMes = if (mes == 12) {
                String.format("%04d-01-01T00:00:00+00:00", anio + 1)
            } else {
                String.format("%04d-%02d-01T00:00:00+00:00", anio, mes + 1)
            }

            val lista = SupabaseClient.client.postgrest["historial_consumo"]
                .select {
                    filter {
                        eq("user_id", userId)
                        gte("fecha_consumo", inicioMes)
                        lt("fecha_consumo", finMes)
                    }
                }
                .decodeList<Consumo>()
            Result.success(lista)
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
            val userId = requireUserId()
            val ingredienteActualizado = Ingrediente(
                id = id,
                nombre = nombre, cantidad = cantidad, unidad = unidad,
                fechaCaducidad = fechaCaducidad, categoriaId = categoriaId,
                imagenUrl = imagenUrl, userId = userId
            )

            SupabaseClient.client.postgrest["ingredientes"]
                .update(ingredienteActualizado) {
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

    suspend fun descontarIngredientes(descuentos: List<DescuentoIngredienteInventario>): Result<Unit> {
        if (descuentos.isEmpty()) return Result.success(Unit)

        try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return try {
            val rpcParams = PrepararRecetaRpcParams(
                descuentos = descuentos.map { descuento ->
                    DescuentoPreparacionRpc(
                        ingredienteId = descuento.ingrediente.id
                            ?: throw IllegalArgumentException("Ingrediente sin ID válido"),
                        cantidad = descuento.cantidadADescontar
                    )
                }
            )
            SupabaseClient.client.postgrest.rpc(
                function = "preparar_receta",
                parameters = rpcParams
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun descontarIngrediente(ingrediente: Ingrediente, cantidadADescontar: Float): Result<Unit> {
        val ingredienteId = ingrediente.id
            ?: return Result.failure(IllegalArgumentException("Ingrediente sin ID válido"))
        if (cantidadADescontar <= 0f || cantidadADescontar > ingrediente.cantidad) {
            return Result.failure(IllegalArgumentException("Cantidad de consumo inválida"))
        }

        val userId = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val ingredienteActualizado = ingrediente.copy(
            cantidad = ingrediente.cantidad - cantidadADescontar,
            userId = userId
        )
        val consumo = crearRegistroConsumo(
            ingrediente = ingrediente,
            cantidad = cantidadADescontar,
            userId = userId,
            fechaConsumo = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        )

        return try {
            SupabaseClient.client.postgrest["ingredientes"]
                .update(ingredienteActualizado) {
                    filter {
                        eq("id", ingredienteId)
                        eq("user_id", userId)
                    }
                }
            try {
                SupabaseClient.client.postgrest["historial_consumo"].insert(consumo)
            } catch (e: Exception) {
                runCatching {
                    SupabaseClient.client.postgrest["ingredientes"]
                        .update(ingrediente.copy(userId = userId)) {
                            filter {
                                eq("id", ingredienteId)
                                eq("user_id", userId)
                            }
                        }
                }
                return Result.failure(e)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun crearRegistroConsumo(
        ingrediente: Ingrediente,
        cantidad: Float,
        userId: String,
        fechaConsumo: String
    ): Consumo {
        return Consumo(
            nombre = ingrediente.nombre,
            cantidad = cantidad,
            unidad = ingrediente.unidad,
            categoriaId = ingrediente.categoriaId,
            fechaConsumo = fechaConsumo,
            userId = userId
        )
    }

    private fun puedeRegistrarseComoDesperdicio(fechaCaducidad: String?): Boolean {
        if (fechaCaducidad.isNullOrBlank()) return false
        return runCatching {
            val fecha = java.time.LocalDate.parse(fechaCaducidad)
            !fecha.isAfter(java.time.LocalDate.now().plusDays(7))
        }.getOrDefault(false)
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
