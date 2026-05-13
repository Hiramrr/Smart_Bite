package com.example.wonderbalance.ui.presupuesto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wonderbalance.R
import com.example.wonderbalance.databinding.FragmentoPresupuestoBinding
import com.example.wonderbalance.util.GestorSesion
import com.example.wonderbalance.viewmodel.CategoriaViewModel
import com.example.wonderbalance.viewmodel.PresupuestoViewModel
import com.example.wonderbalance.viewmodel.TransaccionViewModel

class FragmentoPresupuesto : Fragment() {

    private var _enlace: FragmentoPresupuestoBinding? = null
    private val enlace get() = _enlace!!
    private val presupuestoViewModel: PresupuestoViewModel by viewModels()
    private val categoriaViewModel: CategoriaViewModel by viewModels()
    private val transaccionViewModel: TransaccionViewModel by viewModels()
    private lateinit var adaptador: AdaptadorPresupuesto

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _enlace = FragmentoPresupuestoBinding.inflate(inflater, container, false)
        return enlace.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usuarioId = GestorSesion(requireContext()).obtenerUsuarioId()
        adaptador = AdaptadorPresupuesto()

        enlace.listaPresupuestos.layoutManager = LinearLayoutManager(requireContext())
        enlace.listaPresupuestos.adapter = adaptador

        categoriaViewModel.obtenerTodas(usuarioId).observe(viewLifecycleOwner) { categorias ->
            val mapa = categorias.associate { it.id to it.nombre }
            adaptador.actualizarCategorias(mapa)
        }

        presupuestoViewModel.obtenerPorMesActual(usuarioId)
            .observe(viewLifecycleOwner) { lista ->
                if (lista.isNullOrEmpty()) {
                    enlace.listaPresupuestos.visibility = View.GONE
                    enlace.txtSinPresupuestos.visibility = View.VISIBLE
                } else {
                    enlace.listaPresupuestos.visibility = View.VISIBLE
                    enlace.txtSinPresupuestos.visibility = View.GONE

                    // Calcular gasto actual por cada presupuesto
                    val calendario = java.util.Calendar.getInstance()
                    val mes = "%04d-%02d".format(
                        calendario.get(java.util.Calendar.YEAR),
                        calendario.get(java.util.Calendar.MONTH) + 1
                    )
                    lista.forEach { presupuesto ->
                        transaccionViewModel.obtenerPorMes(usuarioId, mes)
                            .observe(viewLifecycleOwner) { transacciones ->
                                val gastado = transacciones
                                    .filter { it.categoriaId == presupuesto.categoriaId }
                                    .sumOf { it.monto }
                                adaptador.actualizarGasto(presupuesto.id, gastado)
                            }
                    }
                    adaptador.submitList(lista)
                }
            }

        enlace.fabNuevoPresupuesto.setOnClickListener {
            findNavController().navigate(R.id.accion_presupuesto_a_nuevo)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _enlace = null
    }
}