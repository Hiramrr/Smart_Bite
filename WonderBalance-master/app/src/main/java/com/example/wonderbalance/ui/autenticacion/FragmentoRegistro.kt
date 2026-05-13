package com.example.wonderbalance.ui.autenticacion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.wonderbalance.R
import com.example.wonderbalance.databinding.FragmentoRegistroBinding
import com.example.wonderbalance.util.GestorSesion
import com.example.wonderbalance.viewmodel.CategoriaViewModel
import com.example.wonderbalance.viewmodel.ResultadoOperacion
import com.example.wonderbalance.viewmodel.UsuarioViewModel

class FragmentoRegistro : Fragment() {

    private var _enlace: FragmentoRegistroBinding? = null
    private val enlace get() = _enlace!!
    private val viewModel: UsuarioViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _enlace = FragmentoRegistroBinding.inflate(inflater, container, false)
        return enlace.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enlace.btnRegistrarse.setOnClickListener {
            val nombre = enlace.etNombre.text.toString().trim()
            val correo = enlace.etCorreo.text.toString().trim()
            val contrasena = enlace.etContrasena.text.toString().trim()
            val confirmar = enlace.etConfirmar.text.toString().trim()

            if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (contrasena != confirmar) {
                enlace.campoConfirmar.error = "Las contraseñas no coinciden"
                return@setOnClickListener
            }
            enlace.campoConfirmar.error = null
            viewModel.registrar(nombre, correo, contrasena)
        }

        enlace.txtIrAcceso.setOnClickListener {
            findNavController().navigate(R.id.accion_registro_a_acceso)
        }

        viewModel.resultadoRegistro.observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                is ResultadoOperacion.Exito -> {
                    viewModel.usuarioActual.value?.let { usuario ->
                        GestorSesion(requireContext()).guardarSesion(
                            usuario.id, usuario.nombre, usuario.correo
                        )
                        // Crear categorías predeterminadas
                        val categoriaViewModel: CategoriaViewModel by viewModels()
                        categoriaViewModel.crearCategoriasPredeterminadas(usuario.id)
                    }
                    findNavController().navigate(R.id.accion_registro_a_dashboard)
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