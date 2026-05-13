package com.example.wonderbalance.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.wonderbalance.datos.basededatos.BaseDeDatos
import com.example.wonderbalance.repositorio.TransaccionRepositorio
import com.example.wonderbalance.util.GestorSesion

class ReporteViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio: TransaccionRepositorio
    private val gestorSesion: GestorSesion = GestorSesion(application)
    private val usuarioId: Int = gestorSesion.obtenerUsuarioId()

    private val _mesSeleccionado = MutableLiveData<String>()
    val mesSeleccionado: LiveData<String> = _mesSeleccionado

    init {
        val db = BaseDeDatos.obtenerInstancia(application)
        repositorio = TransaccionRepositorio(db.transaccionDao())
    }

    val ingresos: LiveData<Double> = _mesSeleccionado.switchMap { mes ->
        repositorio.obtenerIngresosTotalesPorMes(usuarioId, mes)
    }

    val gastos: LiveData<Double> = _mesSeleccionado.switchMap { mes ->
        repositorio.obtenerGastosTotalesPorMes(usuarioId, mes)
    }

    val balance = MediatorLiveData<Double>().apply {
        addSource(ingresos) { i ->
            value = (i ?: 0.0) - (gastos.value ?: 0.0)
        }
        addSource(gastos) { g ->
            value = (ingresos.value ?: 0.0) - (g ?: 0.0)
        }
    }

    fun establecerMes(año: Int, mes: Int) {
        val mesFormateado = String.format("%04d-%02d", año, mes + 1)
        _mesSeleccionado.value = mesFormateado
    }
}
