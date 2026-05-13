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
import com.example.wonderbalance.databinding.FragmentoAccesoBinding
import com.example.wonderbalance.util.GestorSesion
import com.example.wonderbalance.viewmodel.ResultadoOperacion
import com.example.wonderbalance.viewmodel.UsuarioViewModel

class FragmentoAcceso : Fragment() {

    private var _enlace: FragmentoAccesoBinding? = null
    private val enlace get() = _enlace!!
    private val viewModel: UsuarioViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _enlace = FragmentoAccesoBinding.inflate(inflater, container, false)
        return enlace.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enlace.btnIniciarSesion.setOnClickListener {
            val correo = enlace.etCorreo.text.toString().trim()
            val contrasena = enlace.etContrasena.text.toString().trim()
            viewModel.iniciarSesion(correo, contrasena)
        }

        enlace.txtIrRegistro.setOnClickListener {
            findNavController().navigate(R.id.accion_acceso_a_registro)
        }

        viewModel.resultadoSesion.observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                is ResultadoOperacion.Exito -> {
                    viewModel.usuarioActual.value?.let { usuario ->
                        GestorSesion(requireContext()).guardarSesion(
                            usuario.id, usuario.nombre, usuario.correo
                        )
                    }
                    findNavController().navigate(R.id.accion_acceso_a_dashboard)
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