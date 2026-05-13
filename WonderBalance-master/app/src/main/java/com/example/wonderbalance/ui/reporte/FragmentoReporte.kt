package com.example.wonderbalance.ui.reporte

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.wonderbalance.R
import com.example.wonderbalance.databinding.FragmentoReporteBinding
import com.example.wonderbalance.viewmodel.ReporteViewModel
import java.util.*

class FragmentoReporte : Fragment(R.layout.fragmento_reporte) {

    private var _binding: FragmentoReporteBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ReporteViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentoReporteBinding.bind(view)

        val calendario = Calendar.getInstance()
        
        binding.btnSeleccionarMes.setOnClickListener {
            val picker = DatePickerDialog(requireContext(), { _, year, month, _ ->
                viewModel.establecerMes(year, month)
                actualizarTextoMes(year, month)
            }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), 1)
            picker.show()
        }

        observarDatos()
        
        // Cargar mes actual por defecto
        val añoActual = calendario.get(Calendar.YEAR)
        val mesActual = calendario.get(Calendar.MONTH)
        viewModel.establecerMes(añoActual, mesActual)
        actualizarTextoMes(añoActual, mesActual)
    }

    private fun observarDatos() {
        viewModel.balance.observe(viewLifecycleOwner) { balance ->
            val totalIngresos = viewModel.ingresos.value ?: 0.0
            val totalGastos = viewModel.gastos.value ?: 0.0
            
            val tieneDatos = totalIngresos != 0.0 || totalGastos != 0.0
            
            if (tieneDatos) {
                binding.cardReporte.visibility = View.VISIBLE
                binding.txtVistaVacia.visibility = View.GONE

                binding.txtTotalIngresos.text = getString(R.string.ingresos_con_monto, String.format(Locale.getDefault(), "%.2f", totalIngresos))
                binding.txtTotalGastos.text = getString(R.string.gastos_con_monto, String.format(Locale.getDefault(), "%.2f", totalGastos))
                binding.txtBalanceMes.text = getString(R.string.formato_moneda, String.format(Locale.getDefault(), "%.2f", balance))
            } else {
                binding.cardReporte.visibility = View.GONE
                binding.txtVistaVacia.visibility = View.VISIBLE
            }
        }
    }

    private fun actualizarTextoMes(año: Int, mes: Int) {
        val meses = arrayOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                           "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        binding.txtMesActual.text = getString(R.string.mes_anio, meses[mes], año)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
