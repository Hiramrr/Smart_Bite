package com.example.wonderbalance.repositorio

import androidx.lifecycle.LiveData
import com.example.wonderbalance.datos.dao.PresupuestoDao
import com.example.wonderbalance.datos.entidad.Presupuesto

class PresupuestoRepositorio(private val presupuestoDao: PresupuestoDao) {

    fun obtenerPorMes(usuarioId: Int, mes: Int, anio: Int): LiveData<List<Presupuesto>> =
        presupuestoDao.obtenerPorMes(usuarioId, mes, anio)

    suspend fun insertar(presupuesto: Presupuesto): Long =
        presupuestoDao.insertar(presupuesto)

    suspend fun actualizar(presupuesto: Presupuesto) =
        presupuestoDao.actualizar(presupuesto)

    suspend fun eliminar(presupuesto: Presupuesto) =
        presupuestoDao.eliminar(presupuesto)

    suspend fun buscarPorCategoriaYMes(
        usuarioId: Int,
        categoriaId: Int,
        mes: Int,
        anio: Int
    ): Presupuesto? = presupuestoDao.buscarPorCategoriaYMes(usuarioId, categoriaId, mes, anio)

    suspend fun buscarPorId(id: Int): Presupuesto? =
        presupuestoDao.buscarPorId(id)
}