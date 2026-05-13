package com.example.wonderbalance.ui.analitica

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.example.wonderbalance.databinding.FragmentoAnaliticaBinding
import com.example.wonderbalance.datos.entidad.Transaccion
import com.example.wonderbalance.util.Constantes
import com.example.wonderbalance.util.GestorSesion
import com.example.wonderbalance.viewmodel.CategoriaViewModel
import com.example.wonderbalance.viewmodel.TransaccionViewModel
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class FragmentoAnalitica : Fragment() {

    private var _enlace: FragmentoAnaliticaBinding? = null
    private val enlace get() = _enlace!!
    private val transaccionViewModel: TransaccionViewModel by viewModels()
    private val categoriaViewModel: CategoriaViewModel by viewModels()

    private val calendarioActual = Calendar.getInstance()
    private var mapaCategorias: Map<Int, String> = emptyMap()
    private var usuarioId: Int = 0
    private var transaccionesLiveData: LiveData<List<Transaccion>>? = null

    private val coloresGastos = listOf(
        Color.parseColor("#FF9F43"),
        Color.parseColor("#EE5A24"),
        Color.parseColor("#FDA7DF"),
        Color.parseColor("#E53935"),
        Color.parseColor("#B71C1C"),
        Color.parseColor("#FB8C00"),
        Color.parseColor("#FDD835")
    )

    private val coloresIngresos = listOf(
        Color.parseColor("#48DBFB"),
        Color.parseColor("#0ABDE3"),
        Color.parseColor("#00D2D3"),
        Color.parseColor("#1E88E5"),
        Color.parseColor("#43A047"),
        Color.parseColor("#00C853")
    )

    private val formateadorMoneda = object : ValueFormatter() {
        private val formato = DecimalFormat("$#,###.00")
        override fun getFormattedValue(value: Float): String {
            return formato.format(value)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _enlace = FragmentoAnaliticaBinding.inflate(inflater, container, false)
        return enlace.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usuarioId = GestorSesion(requireContext()).obtenerUsuarioId()

        enlace.graficaGastos.setNoDataText("¡Excelente! No hay gastos registrados este mes.")
        enlace.graficaIngresos.setNoDataText("Aún no tienes ingresos en este periodo.")
        enlace.graficaGastos.setNoDataTextColor(Color.parseColor("#7F8C8D"))
        enlace.graficaIngresos.setNoDataTextColor(Color.parseColor("#7F8C8D"))

        categoriaViewModel.obtenerTodas(usuarioId).observe(viewLifecycleOwner) { categorias ->
            mapaCategorias = categorias.associate { it.id to it.nombre }
            cargarDatosDelMes()
        }

        enlace.btnMesAnterior.setOnClickListener {
            calendarioActual.add(Calendar.MONTH, -1)
            cargarDatosDelMes()
        }

        enlace.btnMesSiguiente.setOnClickListener {
            calendarioActual.add(Calendar.MONTH, 1)
            cargarDatosDelMes()
        }

        enlace.btnExportar.setOnClickListener {
            Toast.makeText(requireContext(), "Reporte generado en PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarDatosDelMes() {
        val formatoMesBD = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val mesFiltro = formatoMesBD.format(calendarioActual.time)

        val formatoVisible = SimpleDateFormat("MMMM yyyy", Locale("es", "MX"))
        enlace.txtMes.text = formatoVisible.format(calendarioActual.time).replaceFirstChar { it.uppercase() }

        transaccionesLiveData?.removeObservers(viewLifecycleOwner)

        transaccionesLiveData = transaccionViewModel.obtenerPorMes(usuarioId, mesFiltro)

        transaccionesLiveData?.observe(viewLifecycleOwner) { transacciones ->
            dibujarGraficas(transacciones)
        }
    }

    private fun dibujarGraficas(transacciones: List<Transaccion>) {
        // GASTOS
        val gastos = transacciones.filter { it.tipo == Constantes.TIPO_GASTO }
        val gastosAgrupados = gastos.groupBy { it.categoriaId }
            .mapValues { entrada -> entrada.value.sumOf { it.monto } }

        if (gastosAgrupados.isNotEmpty()) {
            val totalGastos = gastosAgrupados.values.sum()
            val entradasPastel = gastosAgrupados.map { (catId, total) ->
                PieEntry(total.toFloat(), mapaCategorias[catId] ?: "Otro")
            }

            val conjuntoPastel = PieDataSet(entradasPastel, "").apply {
                colors = coloresGastos
                valueTextSize = 12f
                valueTextColor = Color.WHITE
                valueFormatter = formateadorMoneda
                sliceSpace = 4f
                selectionShift = 8f
            }

            enlace.graficaGastos.apply {
                data = PieData(conjuntoPastel)
                description.isEnabled = false
                isDrawHoleEnabled = true
                holeRadius = 55f
                transparentCircleRadius = 60f
                setHoleColor(Color.TRANSPARENT)

                val formatoTotal = DecimalFormat("$#,###.00").format(totalGastos)
                setCenterText("Total\n$formatoTotal")
                setCenterTextSize(18f)
                setCenterTextColor(Color.parseColor("#2C3E50"))

                legend.apply {
                    form = Legend.LegendForm.CIRCLE
                    textSize = 12f
                    textColor = Color.parseColor("#7F8C8D")
                    isWordWrapEnabled = true
                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                }

                animateY(800)
                invalidate()
            }
        } else {
            enlace.graficaGastos.clear()
        }

        //INGRESOS
        val ingresos = transacciones.filter { it.tipo == Constantes.TIPO_INGRESO }
        val ingresosAgrupados = ingresos.groupBy { it.categoriaId }
            .mapValues { entrada -> entrada.value.sumOf { it.monto } }

        if (ingresosAgrupados.isNotEmpty()) {
            val etiquetasEjeX = ingresosAgrupados.keys.map { mapaCategorias[it] ?: "Otro" }
            val entradasBarra = ingresosAgrupados.entries
                .mapIndexed { indice, (_, total) ->
                    BarEntry(indice.toFloat(), total.toFloat())
                }

            val conjuntoBarra = BarDataSet(entradasBarra, "Ingresos").apply {
                colors = coloresIngresos
                valueTextSize = 11f
                valueTextColor = Color.parseColor("#2C3E50")
                valueFormatter = formateadorMoneda
            }

            enlace.graficaIngresos.apply {
                data = BarData(conjuntoBarra)
                description.isEnabled = false

                xAxis.apply {
                    valueFormatter = IndexAxisValueFormatter(etiquetasEjeX)
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    setDrawAxisLine(true)
                    axisLineColor = Color.parseColor("#BDC3C7")
                    textColor = Color.parseColor("#7F8C8D")
                    granularity = 1f
                }

                axisLeft.apply {
                    axisMinimum = 0f
                    setDrawAxisLine(false)
                    gridColor = Color.parseColor("#ECF0F1")
                    textColor = Color.parseColor("#7F8C8D")
                    valueFormatter = formateadorMoneda
                }

                axisRight.isEnabled = false

                legend.apply {
                    form = Legend.LegendForm.CIRCLE
                    textColor = Color.parseColor("#7F8C8D")
                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                }

                animateY(800)
                invalidate()
            }
        } else {
            enlace.graficaIngresos.clear()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _enlace = null
    }
}