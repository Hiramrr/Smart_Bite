package com.example.wonderbalance.datos.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.example.wonderbalance.datos.entidad.Presupuesto

@Dao
interface PresupuestoDao {

    @Insert
    suspend fun insertar(presupuesto: Presupuesto): Long

    @Update
    suspend fun actualizar(presupuesto: Presupuesto)

    @Delete
    suspend fun eliminar(presupuesto: Presupuesto)

    @Query("SELECT * FROM presupuestos WHERE usuarioId = :usuarioId AND mes = :mes AND anio = :anio")
    fun obtenerPorMes(usuarioId: Int, mes: Int, anio: Int): LiveData<List<Presupuesto>>

    @Query("SELECT * FROM presupuestos WHERE usuarioId = :usuarioId AND categoriaId = :categoriaId AND mes = :mes AND anio = :anio LIMIT 1")
    suspend fun buscarPorCategoriaYMes(usuarioId: Int, categoriaId: Int, mes: Int, anio: Int): Presupuesto?

    @Query("SELECT * FROM presupuestos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Presupuesto?
}