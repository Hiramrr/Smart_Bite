package com.example.wonderbalance.ui.transaccion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wonderbalance.R
import com.example.wonderbalance.databinding.FragmentoHistorialBinding
import com.example.wonderbalance.ui.dashboard.AdaptadorTransaccion
import com.example.wonderbalance.util.GestorSesion
import com.example.wonderbalance.viewmodel.CategoriaViewModel
import com.example.wonderbalance.viewmodel.TransaccionViewModel
import com.example.wonderbalance.datos.entidad.Transaccion
class FragmentoHistorial : Fragment() {

    private var _enlace: FragmentoHistorialBinding? = null
    private val enlace get() = _enlace!!
    private val transaccionViewModel: TransaccionViewModel by viewModels()
    private val categoriaViewModel: CategoriaViewModel by viewModels()
    private lateinit var adaptador: AdaptadorTransaccion
    private lateinit var gestorSesion: GestorSesion

    private var listaCompletaTransacciones = listOf<Transaccion>()

    private var mapaCategoriasActual = mapOf<Int, String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _enlace = FragmentoHistorialBinding.inflate(inflater, container, false)
        return enlace.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gestorSesion = GestorSesion(requireContext())
        val usuarioId = gestorSesion.obtenerUsuarioId()

        // 1. Configuración del adaptador y envío del ID
        adaptador = AdaptadorTransaccion { transaccion ->
            val paquete = android.os.Bundle().apply {
                putInt("transaccionId", transaccion.id)
            }
            // Asegúrate de usar la acción correcta de tu archivo de navegación
            findNavController().navigate(R.id.accion_historial_a_detalle, paquete)
        }

        enlace.listaHistorial.layoutManager = LinearLayoutManager(requireContext())
        enlace.listaHistorial.adapter = adaptador

        // 2. Cargar categorías y guardarlas en memoria para el buscador
        categoriaViewModel.obtenerTodas(usuarioId).observe(viewLifecycleOwner) { categorias ->
            mapaCategoriasActual = categorias.associate { it.id to it.nombre }
            adaptador.actualizarCategorias(mapaCategoriasActual)
            filtrarLista(enlace.etBusqueda.text.toString())
        }

        // 3. Cargar todas las transacciones en memoria
        transaccionViewModel.obtenerTodas(usuarioId).observe(viewLifecycleOwner) { lista ->
            listaCompletaTransacciones = lista ?: emptyList()
            filtrarLista(enlace.etBusqueda.text.toString())
        }

        // 4. Búsqueda en tiempo real desde la memoria
        enlace.etBusqueda.addTextChangedListener { texto ->
            filtrarLista(texto.toString())
        }
    }

    // 5. Función que filtra por nota o por nombre de categoría
    private fun filtrarLista(textoConsulta: String) {
        val busqueda = textoConsulta.trim()

        if (busqueda.isBlank()) {
            mostrarLista(listaCompletaTransacciones.isEmpty())
            adaptador.submitList(listaCompletaTransacciones)
            return
        }

        val listaFiltrada = listaCompletaTransacciones.filter { transaccion ->
            val nombreCategoria = mapaCategoriasActual[transaccion.categoriaId] ?: ""

            // Revisa si el texto coincide con la categoría o con la nota
            nombreCategoria.contains(busqueda, ignoreCase = true) ||
                    (transaccion.nota?.contains(busqueda, ignoreCase = true) == true)
        }

        mostrarLista(listaFiltrada.isEmpty())
        adaptador.submitList(listaFiltrada)
    }

    private fun mostrarLista(estaVacia: Boolean) {
        enlace.listaHistorial.visibility = if (estaVacia) View.GONE else View.VISIBLE
        enlace.txtSinResultados.visibility = if (estaVacia) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _enlace = null
    }
}