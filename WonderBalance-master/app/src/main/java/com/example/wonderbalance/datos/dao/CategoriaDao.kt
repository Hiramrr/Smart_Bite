package com.example.wonderbalance.datos.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.example.wonderbalance.datos.entidad.Categoria

@Dao
interface CategoriaDao {

    @Insert
    suspend fun insertar(categoria: Categoria): Long

    @Update
    suspend fun actualizar(categoria: Categoria)

    @Delete
    suspend fun eliminar(categoria: Categoria)

    @Query("SELECT * FROM categorias WHERE usuarioId = :usuarioId ORDER BY nombre ASC")
    fun obtenerTodas(usuarioId: Int): LiveData<List<Categoria>>

    @Query("SELECT * FROM categorias WHERE usuarioId = :usuarioId AND tipo = :tipo ORDER BY nombre ASC")
    fun obtenerPorTipo(usuarioId: Int, tipo: String): LiveData<List<Categoria>>

    @Query("SELECT * FROM categorias WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Categoria?

    @Query("SELECT COUNT(*) FROM categorias WHERE nombre = :nombre AND usuarioId = :usuarioId")
    suspend fun contarPorNombre(nombre: String, usuarioId: Int): Int
}