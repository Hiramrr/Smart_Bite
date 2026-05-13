package com.example.wonderbalance.repositorio

import androidx.lifecycle.LiveData
import com.example.wonderbalance.datos.dao.TransaccionDao
import com.example.wonderbalance.datos.entidad.Transaccion

class TransaccionRepositorio(private val transaccionDao: TransaccionDao) {

    fun obtenerTodas(usuarioId: Int): LiveData<List<Transaccion>> =
        transaccionDao.obtenerTodas(usuarioId)

    fun obtenerUltimas(usuarioId: Int): LiveData<List<Transaccion>> =
        transaccionDao.obtenerUltimas(usuarioId)

    fun obtenerPorMes(usuarioId: Int, mes: String): LiveData<List<Transaccion>> =
        transaccionDao.obtenerPorMes(usuarioId, mes)

    fun obtenerPorCategoria(usuarioId: Int, categoriaId: Int): LiveData<List<Transaccion>> =
        transaccionDao.obtenerPorCategoria(usuarioId, categoriaId)

    fun obtenerBalanceGeneral(usuarioId: Int): LiveData<Double> =
        transaccionDao.obtenerBalanceGeneral(usuarioId)

    suspend fun obtenerGastoPorCategoriaYMes(
        usuarioId: Int,
        categoriaId: Int,
        mes: String
    ): Double = transaccionDao.obtenerGastoPorCategoriaYMes(usuarioId, categoriaId, mes)

    fun buscar(usuarioId: Int, busqueda: String): LiveData<List<Transaccion>> =
        transaccionDao.buscar(usuarioId, busqueda)

    suspend fun insertar(transaccion: Transaccion): Long =
        transaccionDao.insertar(transaccion)

    suspend fun actualizar(transaccion: Transaccion) =
        transaccionDao.actualizar(transaccion)

    suspend fun eliminar(transaccion: Transaccion) =
        transaccionDao.eliminar(transaccion)

    suspend fun buscarPorId(id: Int): Transaccion? =
        transaccionDao.buscarPorId(id)

    fun obtenerIngresosTotalesPorMes(usuarioId: Int, mes: String): LiveData<Double> =
        transaccionDao.obtenerIngresosTotalesPorMes(usuarioId, mes)

    fun obtenerGastosTotalesPorMes(usuarioId: Int, mes: String): LiveData<Double> =
        transaccionDao.obtenerGastosTotalesPorMes(usuarioId, mes)
}
