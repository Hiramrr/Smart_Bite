package com.example.wonderbalance.ui.meta

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.wonderbalance.databinding.FragmentoNuevaMetaBinding
import com.example.wonderbalance.datos.entidad.Meta
import com.example.wonderbalance.util.GestorSesion
import com.example.wonderbalance.viewmodel.MetaViewModel
import com.example.wonderbalance.viewmodel.ResultadoOperacion
import java.util.Calendar

class FragmentoNuevaMeta : Fragment() {

    private var _enlace: FragmentoNuevaMetaBinding? = null
    private val enlace get() = _enlace!!
    private val metaViewModel: MetaViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _enlace = FragmentoNuevaMetaBinding.inflate(inflater, container, false)
        return enlace.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usuarioId = GestorSesion(requireContext()).obtenerUsuarioId()

        enlace.campoFecha.setEndIconOnClickListener { mostrarDatePicker() }
        enlace.etFecha.setOnClickListener { mostrarDatePicker() }

        enlace.btnCrear.setOnClickListener {
            val nombre = enlace.etNombre.text.toString().trim()
            val montoTexto = enlace.etMonto.text.toString().trim()
            val fecha = enlace.etFecha.text.toString().trim()

            if (nombre.isBlank() || montoTexto.isBlank() || fecha.isBlank()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val monto = montoTexto.toDoubleOrNull()
            if (monto == null || monto <= 0) {
                enlace.campoMonto.error = "Monto no válido"
                return@setOnClickListener
            }
            val meta = Meta(
                nombre = nombre,
                montoObjetivo = monto,
                fechaLimite = fecha,
                usuarioId = usuarioId
            )
            metaViewModel.guardar(meta)
        }

        metaViewModel.resultado.observe(viewLifecycleOwner) { resultado ->
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

    private fun mostrarDatePicker() {
        val calendario = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, anio, mes, dia ->
                val fechaFutura = Calendar.getInstance()
                fechaFutura.set(anio, mes, dia)
                if (fechaFutura.before(Calendar.getInstance())) {
                    Toast.makeText(requireContext(), "Selecciona una fecha futura", Toast.LENGTH_SHORT).show()
                    return@DatePickerDialog
                }
                enlace.etFecha.setText("%04d-%02d-%02d".format(anio, mes + 1, dia))
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _enlace = null
    }
}