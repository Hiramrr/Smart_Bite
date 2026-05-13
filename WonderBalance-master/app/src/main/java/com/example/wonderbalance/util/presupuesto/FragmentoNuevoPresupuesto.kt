package com.example.wonderbalance.ui.presupuesto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.wonderbalance.databinding.FragmentoNuevoPresupuestoBinding
import com.example.wonderbalance.datos.entidad.Presupuesto
import com.example.wonderbalance.util.GestorSesion
import com.example.wonderbalance.viewmodel.CategoriaViewModel
import com.example.wonderbalance.viewmodel.PresupuestoViewModel
import com.example.wonderbalance.viewmodel.ResultadoOperacion
import java.util.Calendar

class FragmentoNuevoPresupuesto : Fragment() {

    private var _enlace: FragmentoNuevoPresupuestoBinding? = null
    private val enlace get() = _enlace!!
    private val presupuestoViewModel: PresupuestoViewModel by viewModels()
    private val categoriaViewModel: CategoriaViewModel by viewModels()
    private var categoriaSeleccionadaId: Int = 0
    private var listaCategorias = listOf<com.example.wonderbalance.datos.entidad.Categoria>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _enlace = FragmentoNuevoPresupuestoBinding.inflate(inflater, container, false)
        return enlace.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usuarioId = GestorSesion(requireContext()).obtenerUsuarioId()

        categoriaViewModel.obtenerPorTipo(usuarioId, "GASTO")
            .observe(viewLifecycleOwner) { categorias ->
                listaCategorias = categorias
                val nombres = categorias.map { it.nombre }
                val adaptador = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    nombres
                )
                enlace.dropdownCategoria.setAdapter(adaptador)
                enlace.dropdownCategoria.setOnItemClickListener { _, _, posicion, _ ->
                    categoriaSeleccionadaId = listaCategorias[posicion].id
                }
            }

        enlace.btnGuardar.setOnClickListener {
            val montoTexto = enlace.etMonto.text.toString().trim()
            if (montoTexto.isBlank() || categoriaSeleccionadaId == 0) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val monto = montoTexto.toDoubleOrNull()
            if (monto == null || monto <= 0) {
                enlace.campoMonto.error = "Monto no válido"
                return@setOnClickListener
            }
            val calendario = Calendar.getInstance()
            val presupuesto = Presupuesto(
                categoriaId = categoriaSeleccionadaId,
                montoLimite = monto,
                mes = calendario.get(Calendar.MONTH) + 1,
                anio = calendario.get(Calendar.YEAR),
                usuarioId = usuarioId
            )
            presupuestoViewModel.guardar(presupuesto)
        }

        presupuestoViewModel.resultado.observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                is ResultadoOperacion.Exito -> {
                    Toast.makeText(requireContext(), resultado.mensaje, Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                is ResultadoOperacion.Error -> {
                    Toast.makeText(requireContext(), resultado.mensaje, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _enlace = null
    }
}