package com.example.wonderbalance.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wonderbalance.R
import com.example.wonderbalance.databinding.FragmentoDashboardBinding
import com.example.wonderbalance.util.GestorSesion
import com.example.wonderbalance.viewmodel.TransaccionViewModel
import com.example.wonderbalance.viewmodel.CategoriaViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.appcompat.app.AlertDialog
import android.widget.Toast
import com.example.wonderbalance.repositorio.MonedaRepositorio
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.wonderbalance.datos.red.RedSupabase
import io.github.jan.supabase.gotrue.auth

class FragmentoDashboard : Fragment() {

    private val monedaRepositorio = MonedaRepositorio()

    private var balanceBaseMXN: Double = 0.0

    private var monedaActual: String = "MXN"

    private var _enlace: FragmentoDashboardBinding? = null
    private val enlace get() = _enlace!!

    private val transaccionViewModel: TransaccionViewModel by viewModels()
    private val categoriaViewModel: CategoriaViewModel by viewModels() // <-- LÍNEA NUEVA

    private lateinit var adaptador: AdaptadorTransaccion
    private lateinit var gestorSesion: GestorSesion

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _enlace = FragmentoDashboardBinding.inflate(inflater, container, false)
        return enlace.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gestorSesion = GestorSesion(requireContext())
        val usuarioId = gestorSesion.obtenerUsuarioId()

        // Saludo
        enlace.txtSaludo.text = "Hola, ${gestorSesion.obtenerUsuarioNombre()}"
        enlace.txtSaludo.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // 1. Desconectar la cuenta en Supabase
                    RedSupabase.cliente.auth.signOut()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // 2. Borrar la memoria del teléfono
                gestorSesion.cerrarSesion()

                // 3. Regresar a la pantalla de Acceso borrando el historial de pantallas
                val opciones = androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.fragmentoDashboard, true)
                    .build()
                findNavController().navigate(R.id.fragmentoAcceso, null, opciones)
            }
        }

        // RecyclerView
        adaptador = AdaptadorTransaccion { transaccion ->
            // navegar al detalle (lo implementaremos después)
        }
        enlace.listaTransacciones.layoutManager = LinearLayoutManager(requireContext())
        enlace.listaTransacciones.adapter = adaptador

        // NUEVO: Observar categorías y pasarlas al adaptador
        categoriaViewModel.obtenerTodas(usuarioId).observe(viewLifecycleOwner) { categorias ->
            val mapaCategorias = categorias.associate { it.id to it.nombre }
            adaptador.actualizarCategorias(mapaCategorias)
        }

        // Observar balance
        transaccionViewModel.obtenerBalanceGeneral(usuarioId)
            .observe(viewLifecycleOwner) { balance ->
                // Guardamos el balance base siempre en pesos (MXN)
                balanceBaseMXN = balance ?: 0.0

                // Si la moneda actual es MXN, lo mostramos normal. Si no, lo recalculamos.
                if (monedaActual == "MXN") {
                    actualizarTextoBalance(balanceBaseMXN, "MXN")
                } else {
                    convertirYMostrarBalance(monedaActual)
                }
            }

        // NUEVO: Al hacer clic en el balance, elegir moneda
        enlace.txtBalance.setOnClickListener {
            val opciones = arrayOf("MXN (Pesos Mexicanos)", "USD (Dólares)", "EUR (Euros)")
            val codigos = arrayOf("MXN", "USD", "EUR")

            AlertDialog.Builder(requireContext())
                .setTitle("Cambiar Moneda")
                .setItems(opciones) { _, which ->
                    val monedaDestino = codigos[which]
                    convertirYMostrarBalance(monedaDestino)
                }
                .show()
        }

        // Observar últimas transacciones
        transaccionViewModel.obtenerUltimas(usuarioId)
            .observe(viewLifecycleOwner) { lista ->
                if (lista.isNullOrEmpty()) {
                    enlace.listaTransacciones.visibility = View.GONE
                    enlace.txtSinTransacciones.visibility = View.VISIBLE
                } else {
                    enlace.listaTransacciones.visibility = View.VISIBLE
                    enlace.txtSinTransacciones.visibility = View.GONE
                    adaptador.submitList(lista)
                }
            }

        // Botones
        enlace.fabNuevaTransaccion.setOnClickListener {
            findNavController().navigate(R.id.accion_dashboard_a_transaccion)
        }

        enlace.fabVerHistorial.setOnClickListener {
            findNavController().navigate(R.id.accion_dashboard_a_historial)
        }

        enlace.fabAnalitica.setOnClickListener {
            findNavController().navigate(R.id.fragmentoAnalitica)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _enlace = null
    }

    private fun convertirYMostrarBalance(monedaDestino: String) {
        if (monedaDestino == "MXN") {
            monedaActual = "MXN"
            actualizarTextoBalance(balanceBaseMXN, "MXN")
            adaptador.actualizarMoneda(1.0, "MXN$") // <- AVISAR A LA LISTA
            return
        }

        enlace.txtBalance.text = "Calculando..."

        viewLifecycleOwner.lifecycleScope.launch {
            val tasa = monedaRepositorio.obtenerTipoDeCambio("MXN", monedaDestino)

            if (tasa != null) {
                monedaActual = monedaDestino
                val balanceConvertido = balanceBaseMXN * tasa
                actualizarTextoBalance(balanceConvertido, monedaDestino)

                // <- AVISAR A LA LISTA DEL NUEVO CÁLCULO
                val simboloLista = when(monedaDestino) {
                    "USD" -> "USD$"
                    "EUR" -> "€"
                    else -> "MXN$"
                }
                adaptador.actualizarMoneda(tasa, simboloLista)

            } else {
                Toast.makeText(requireContext(), "Error de red al obtener tipo de cambio", Toast.LENGTH_SHORT).show()
                actualizarTextoBalance(balanceBaseMXN, "MXN")
                adaptador.actualizarMoneda(1.0, "MXN$") // <- RESTAURAR LA LISTA EN CASO DE ERROR
            }
        }
    }

    private fun actualizarTextoBalance(cantidad: Double, moneda: String) {
        val simbolo = when (moneda) {
            "USD" -> "USD$"
            "EUR" -> "€"
            else -> "MXN$"
        }
        enlace.txtBalance.text = "$simbolo %.2f".format(cantidad)

        // AQUÍ EL CAMBIO A BLANCO
        enlace.txtBalance.setTextColor(
            if (cantidad < 0) requireContext().getColor(android.R.color.holo_red_dark)
            else requireContext().getColor(android.R.color.white) // <-- CAMBIADO A BLANCO
        )
    }
}