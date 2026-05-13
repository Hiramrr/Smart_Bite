package com.example.wonderbalance.datos.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.example.wonderbalance.datos.entidad.Transaccion

@Dao
interface TransaccionDao {

    @Insert
    suspend fun insertar(transaccion: Transaccion): Long

    @Update
    suspend fun actualizar(transaccion: Transaccion)

    @Delete
    suspend fun eliminar(transaccion: Transaccion)

    @Query("SELECT * FROM transacciones WHERE usuarioId = :usuarioId ORDER BY fecha DESC")
    fun obtenerTodas(usuarioId: Int): LiveData<List<Transaccion>>

    @Query("SELECT * FROM transacciones WHERE usuarioId = :usuarioId ORDER BY fecha DESC LIMIT 5")
    fun obtenerUltimas(usuarioId: Int): LiveData<List<Transaccion>>

    @Query("SELECT * FROM transacciones WHERE usuarioId = :usuarioId AND fecha LIKE :mes || '%' ORDER BY fecha DESC")
    fun obtenerPorMes(usuarioId: Int, mes: String): LiveData<List<Transaccion>>

    @Query("SELECT * FROM transacciones WHERE usuarioId = :usuarioId AND categoriaId = :categoriaId ORDER BY fecha DESC")
    fun obtenerPorCategoria(usuarioId: Int, categoriaId: Int): LiveData<List<Transaccion>>

    @Query("SELECT COALESCE(SUM(CASE WHEN tipo = 'INGRESO' THEN monto ELSE -monto END), 0.0) FROM transacciones WHERE usuarioId = :usuarioId")
    fun obtenerBalanceGeneral(usuarioId: Int): LiveData<Double>

    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM transacciones WHERE usuarioId = :usuarioId AND categoriaId = :categoriaId AND fecha LIKE :mes || '%' AND tipo = 'GASTO'")
    suspend fun obtenerGastoPorCategoriaYMes(usuarioId: Int, categoriaId: Int, mes: String): Double

    @Query("SELECT * FROM transacciones WHERE usuarioId = :usuarioId AND (nota LIKE '%' || :busqueda || '%') ORDER BY fecha DESC")
    fun buscar(usuarioId: Int, busqueda: String): LiveData<List<Transaccion>>

    @Query("SELECT * FROM transacciones WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Transaccion?

    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM transacciones WHERE usuarioId = :usuarioId AND fecha LIKE :mes || '%' AND tipo = 'INGRESO'")
    fun obtenerIngresosTotalesPorMes(usuarioId: Int, mes: String): LiveData<Double>

    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM transacciones WHERE usuarioId = :usuarioId AND fecha LIKE :mes || '%' AND tipo = 'GASTO'")
    fun obtenerGastosTotalesPorMes(usuarioId: Int, mes: String): LiveData<Double>
}
