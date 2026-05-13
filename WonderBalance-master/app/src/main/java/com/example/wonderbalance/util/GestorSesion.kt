package com.example.wonderbalance.util

import android.content.Context
import android.content.SharedPreferences

class GestorSesion(context: Context) {

    private val preferencias: SharedPreferences =
        context.getSharedPreferences(NOMBRE_PREFERENCIAS, Context.MODE_PRIVATE)

    companion object {
        private const val NOMBRE_PREFERENCIAS = "sesion_wonderbalance"
        private const val CLAVE_USUARIO_ID = "usuario_id"
        private const val CLAVE_USUARIO_NOMBRE = "usuario_nombre"
        private const val CLAVE_USUARIO_CORREO = "usuario_correo"
        private const val CLAVE_MONEDA = "moneda"
        private const val SIN_SESION = -1
    }

    fun guardarSesion(id: Int, nombre: String, correo: String, moneda: String = "MXN") {
        preferencias.edit().apply {
            putInt(CLAVE_USUARIO_ID, id)
            putString(CLAVE_USUARIO_NOMBRE, nombre)
            putString(CLAVE_USUARIO_CORREO, correo)
            putString(CLAVE_MONEDA, moneda)
            apply()
        }
    }

    fun obtenerUsuarioId(): Int =
        preferencias.getInt(CLAVE_USUARIO_ID, SIN_SESION)

    fun obtenerUsuarioNombre(): String =
        preferencias.getString(CLAVE_USUARIO_NOMBRE, "") ?: ""

    fun obtenerUsuarioCorreo(): String =
        preferencias.getString(CLAVE_USUARIO_CORREO, "") ?: ""

    fun obtenerMoneda(): String =
        preferencias.getString(CLAVE_MONEDA, "MXN") ?: "MXN"

    fun actualizarMoneda(moneda: String) {
        preferencias.edit().putString(CLAVE_MONEDA, moneda).apply()
    }

    fun haySesionActiva(): Boolean =
        obtenerUsuarioId() != SIN_SESION

    fun cerrarSesion() {
        preferencias.edit().clear().apply()
    }
}