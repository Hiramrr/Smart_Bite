package com.example.wonderbalance.repositorio

import androidx.lifecycle.LiveData
import com.example.wonderbalance.datos.dao.MetaDao
import com.example.wonderbalance.datos.entidad.Meta

class MetaRepositorio(private val metaDao: MetaDao) {

    fun obtenerTodas(usuarioId: Int): LiveData<List<Meta>> =
        metaDao.obtenerTodas(usuarioId)

    suspend fun insertar(meta: Meta): Long =
        metaDao.insertar(meta)

    suspend fun actualizar(meta: Meta) =
        metaDao.actualizar(meta)

    suspend fun eliminar(meta: Meta) =
        metaDao.eliminar(meta)

    suspend fun buscarPorId(id: Int): Meta? =
        metaDao.buscarPorId(id)

    suspend fun actualizarMonto(meta: Meta, montoNuevo: Double) {
        val metaActualizada = meta.copy(montoAcumulado = montoNuevo)
        metaDao.actualizar(metaActualizada)
    }
    suspend fun abonarDinero(idMeta: Int, cantidad: Double) {
        metaDao.abonarDinero(idMeta, cantidad)
    }
}