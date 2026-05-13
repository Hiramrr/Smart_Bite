package com.example.wonderbalance.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.wonderbalance.datos.basededatos.BaseDeDatos
import com.example.wonderbalance.datos.entidad.Categoria
import com.example.wonderbalance.repositorio.CategoriaRepositorio
import com.example.wonderbalance.util.Constantes
import kotlinx.coroutines.launch

class CategoriaViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio: CategoriaRepositorio
    private val _resultado = MutableLiveData<ResultadoOperacion>()
    val resultado: LiveData<ResultadoOperacion> = _resultado

    init {
        val db = BaseDeDatos.obtenerInstancia(application)
        repositorio = CategoriaRepositorio(db.categoriaDao())
    }

    fun obtenerTodas(usuarioId: Int): LiveData<List<Categoria>> =
        repositorio.obtenerTodas(usuarioId)

    fun obtenerPorTipo(usuarioId: Int, tipo: String): LiveData<List<Categoria>> =
        repositorio.obtenerPorTipo(usuarioId, tipo)

    fun guardar(categoria: Categoria) {
        viewModelScope.launch {
            if (categoria.nombre.isBlank()) {
                _resultado.value = ResultadoOperacion.Error("El nombre no puede estar vacío")
                return@launch
            }
            val existe = repositorio.existeNombre(categoria.nombre, categoria.usuarioId)
            if (existe) {
                _resultado.value = ResultadoOperacion.Error("Ya existe una categoría con ese nombre")
                return@launch
            }
            repositorio.insertar(categoria)
            _resultado.value = ResultadoOperacion.Exito("Categoría creada")
        }
    }

    fun eliminar(categoria: Categoria) {
        viewModelScope.launch {
            repositorio.eliminar(categoria)
            _resultado.value = ResultadoOperacion.Exito("Categoría eliminada")
        }
    }

    fun crearCategoriasPredeterminadas(usuarioId: Int) {
        viewModelScope.launch {
            Constantes.CATEGORIAS_PREDETERMINADAS_GASTO.forEach { nombre ->
                val existe = repositorio.existeNombre(nombre, usuarioId)
                if (!existe) {
                    repositorio.insertar(
                        Categoria(
                            nombre = nombre,
                            tipo = Constantes.TIPO_GASTO,
                            usuarioId = usuarioId
                        )
                    )
                }
            }
            Constantes.CATEGORIAS_PREDETERMINADAS_INGRESO.forEach { nombre ->
                val existe = repositorio.existeNombre(nombre, usuarioId)
                if (!existe) {
                    repositorio.insertar(
                        Categoria(
                            nombre = nombre,
                            tipo = Constantes.TIPO_INGRESO,
                            usuarioId = usuarioId
                        )
                    )
                }
            }
        }
    }
}