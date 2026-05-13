package com.example.wonderbalance.repositorio

import com.example.wonderbalance.datos.dao.UsuarioDao
import com.example.wonderbalance.datos.entidad.Usuario

class UsuarioRepositorio(private val usuarioDao: UsuarioDao) {

    suspend fun registrar(usuario: Usuario): Long =
        usuarioDao.insertar(usuario)

    suspend fun iniciarSesion(correo: String, contrasena: String): Usuario? =
        usuarioDao.iniciarSesion(correo, contrasena)

    suspend fun buscarPorCorreo(correo: String): Usuario? =
        usuarioDao.buscarPorCorreo(correo)

    suspend fun buscarPorId(id: Int): Usuario? =
        usuarioDao.buscarPorId(id)

    suspend fun actualizar(usuario: Usuario) =
        usuarioDao.actualizar(usuario)
}