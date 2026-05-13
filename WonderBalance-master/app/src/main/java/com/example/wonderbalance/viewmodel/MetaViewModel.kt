package com.example.wonderbalance.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.wonderbalance.datos.basededatos.BaseDeDatos
import com.example.wonderbalance.datos.entidad.Meta
import com.example.wonderbalance.repositorio.MetaRepositorio
import kotlinx.coroutines.launch


class MetaViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio: MetaRepositorio
    private val _resultado = MutableLiveData<ResultadoOperacion>()
    val resultado: LiveData<ResultadoOperacion> = _resultado

    init {
        val db = BaseDeDatos.obtenerInstancia(application)
        repositorio = MetaRepositorio(db.metaDao())
    }

    fun obtenerTodas(usuarioId: Int): LiveData<List<Meta>> =
        repositorio.obtenerTodas(usuarioId)

    fun guardar(meta: Meta) {
        viewModelScope.launch {
            if (meta.montoObjetivo <= 0) {
                _resultado.value = ResultadoOperacion.Error("El monto objetivo debe ser mayor a cero")
                return@launch
            }
            repositorio.insertar(meta)
            _resultado.value = ResultadoOperacion.Exito("Meta creada")
        }
    }

    fun actualizar(meta: Meta) {
        viewModelScope.launch {
            repositorio.actualizar(meta)
            _resultado.value = ResultadoOperacion.Exito("Meta actualizada")
        }
    }

    fun eliminar(meta: Meta) {
        viewModelScope.launch {
            repositorio.eliminar(meta)
            _resultado.value = ResultadoOperacion.Exito("Meta eliminada")
        }
    }

    fun abonarMonto(meta: Meta, abono: Double) {
        viewModelScope.launch {
            if (abono <= 0) {
                _resultado.value = ResultadoOperacion.Error("El abono debe ser mayor a cero")
                return@launch
            }
            val nuevoMonto = meta.montoAcumulado + abono
            repositorio.actualizarMonto(meta, nuevoMonto)
            _resultado.value = ResultadoOperacion.Exito("Abono registrado")
        }
    }
    fun agregarAbono(idMeta: Int, cantidad: Double) {
        viewModelScope.launch {
            repositorio.abonarDinero(idMeta, cantidad)
        }
    }
}