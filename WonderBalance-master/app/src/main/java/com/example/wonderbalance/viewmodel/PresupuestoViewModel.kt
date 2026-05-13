package com.example.wonderbalance.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.wonderbalance.datos.basededatos.BaseDeDatos
import com.example.wonderbalance.datos.entidad.Presupuesto
import com.example.wonderbalance.repositorio.PresupuestoRepositorio
import com.example.wonderbalance.repositorio.TransaccionRepositorio
import kotlinx.coroutines.launch
import java.util.Calendar

class PresupuestoViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio: PresupuestoRepositorio
    private val transaccionRepositorio: TransaccionRepositorio
    private val _resultado = MutableLiveData<ResultadoOperacion>()
    val resultado: LiveData<ResultadoOperacion> = _resultado

    init {
        val db = BaseDeDatos.obtenerInstancia(application)
        repositorio = PresupuestoRepositorio(db.presupuestoDao())
        transaccionRepositorio = TransaccionRepositorio(db.transaccionDao())
    }

    fun obtenerPorMesActual(usuarioId: Int): LiveData<List<Presupuesto>> {
        val calendario = Calendar.getInstance()
        return repositorio.obtenerPorMes(
            usuarioId,
            calendario.get(Calendar.MONTH) + 1,
            calendario.get(Calendar.YEAR)
        )
    }

    fun guardar(presupuesto: Presupuesto) {
        viewModelScope.launch {
            val existente = repositorio.buscarPorCategoriaYMes(
                presupuesto.usuarioId,
                presupuesto.categoriaId,
                presupuesto.mes,
                presupuesto.anio
            )
            if (existente != null) {
                _resultado.value = ResultadoOperacion.Error("Ya existe un presupuesto para esta categoría en este mes")
                return@launch
            }
            repositorio.insertar(presupuesto)
            _resultado.value = ResultadoOperacion.Exito("Presupuesto guardado")
        }
    }

    fun actualizar(presupuesto: Presupuesto) {
        viewModelScope.launch {
            repositorio.actualizar(presupuesto)
            _resultado.value = ResultadoOperacion.Exito("Presupuesto actualizado")
        }
    }

    fun eliminar(presupuesto: Presupuesto) {
        viewModelScope.launch {
            repositorio.eliminar(presupuesto)
            _resultado.value = ResultadoOperacion.Exito("Presupuesto eliminado")
        }
    }
}