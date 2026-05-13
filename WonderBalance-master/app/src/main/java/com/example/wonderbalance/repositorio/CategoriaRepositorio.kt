package com.example.wonderbalance.repositorio

import androidx.lifecycle.LiveData
import com.example.wonderbalance.datos.dao.CategoriaDao
import com.example.wonderbalance.datos.entidad.Categoria

class CategoriaRepositorio(private val categoriaDao: CategoriaDao) {

    fun obtenerTodas(usuarioId: Int): LiveData<List<Categoria>> =
        categoriaDao.obtenerTodas(usuarioId)

    fun obtenerPorTipo(usuarioId: Int, tipo: String): LiveData<List<Categoria>> =
        categoriaDao.obtenerPorTipo(usuarioId, tipo)

    suspend fun insertar(categoria: Categoria): Long =
        categoriaDao.insertar(categoria)

    suspend fun actualizar(categoria: Categoria) =
        categoriaDao.actualizar(categoria)

    suspend fun eliminar(categoria: Categoria) =
        categoriaDao.eliminar(categoria)

    suspend fun buscarPorId(id: Int): Categoria? =
        categoriaDao.buscarPorId(id)

    suspend fun existeNombre(nombre: String, usuarioId: Int): Boolean =
        categoriaDao.contarPorNombre(nombre, usuarioId) > 0
}