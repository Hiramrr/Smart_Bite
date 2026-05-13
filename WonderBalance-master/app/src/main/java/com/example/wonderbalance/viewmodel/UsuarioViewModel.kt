package com.example.wonderbalance.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.wonderbalance.datos.entidad.Usuario
import com.example.wonderbalance.repositorio.UsuarioRepositorioSupabase
import kotlinx.coroutines.launch

class UsuarioViewModel(application: Application) : AndroidViewModel(application) {

    // 1. Instanciamos tu nuevo repositorio de la nube
    private val repositorio = UsuarioRepositorioSupabase()

    private val _resultadoRegistro = MutableLiveData<ResultadoOperacion>()
    val resultadoRegistro: LiveData<ResultadoOperacion> = _resultadoRegistro

    private val _resultadoSesion = MutableLiveData<ResultadoOperacion>()
    val resultadoSesion: LiveData<ResultadoOperacion> = _resultadoSesion

    private val _usuarioActual = MutableLiveData<Usuario?>()
    val usuarioActual: LiveData<Usuario?> = _usuarioActual

    fun registrar(nombre: String, correo: String, contrasena: String) {
        viewModelScope.launch {
            // Mandamos a llamar a Supabase
            val exito = repositorio.registrarUsuario(nombre, correo, contrasena)

            if (exito) {
                // Si se registró bien, iniciamos sesión para recuperar el ID que generó la base de datos
                val perfilSupabase = repositorio.iniciarSesion(correo, contrasena)

                if (perfilSupabase != null) {
                    // Mapeamos el perfil de la nube a tu clase local para no romper el GestorSesion
                    _usuarioActual.value = Usuario(
                        id = perfilSupabase.id ?: 0,
                        nombre = perfilSupabase.nombre,
                        correo = perfilSupabase.correo,
                        contrasena = "", // Ya no guardamos la contraseña en memoria
                        moneda = perfilSupabase.moneda
                    )
                    _resultadoRegistro.value = ResultadoOperacion.Exito("Registro exitoso en la nube")
                } else {
                    _resultadoRegistro.value = ResultadoOperacion.Error("Registro exitoso, pero falló al recuperar perfil")
                }
            } else {
                _resultadoRegistro.value = ResultadoOperacion.Error("Error al registrar. El correo podría estar en uso.")
            }
        }
    }

    fun iniciarSesion(correo: String, contrasena: String) {
        viewModelScope.launch {
            if (correo.isBlank() || contrasena.isBlank()) {
                _resultadoSesion.value = ResultadoOperacion.Error("Campos vacíos")
                return@launch
            }

            // Consultamos a Supabase
            val perfilSupabase = repositorio.iniciarSesion(correo, contrasena)

            if (perfilSupabase != null) {
                // Traducimos el resultado para la interfaz
                _usuarioActual.value = Usuario(
                    id = perfilSupabase.id ?: 0,
                    nombre = perfilSupabase.nombre,
                    correo = perfilSupabase.correo,
                    contrasena = "",
                    moneda = perfilSupabase.moneda
                )
                _resultadoSesion.value = ResultadoOperacion.Exito("Sesión iniciada")
            } else {
                _resultadoSesion.value = ResultadoOperacion.Error("Correo o contraseña incorrectos")
            }
        }
    }
}