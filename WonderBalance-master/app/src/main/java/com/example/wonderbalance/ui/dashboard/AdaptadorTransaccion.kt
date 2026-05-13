package com.example.wonderbalance.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wonderbalance.databinding.ItemTransaccionBinding
import com.example.wonderbalance.datos.entidad.Transaccion
import com.example.wonderbalance.util.Constantes

class AdaptadorTransaccion(
    private val alHacerClic: (Transaccion) -> Unit
) : ListAdapter<Transaccion, AdaptadorTransaccion.TransaccionViewHolder>(DiffTransaccion()) {

    private var mapaCategorias: Map<Int, String> = emptyMap()

    // 1. NUEVAS VARIABLES PARA LA MONEDA
    private var tasaActual: Double = 1.0
    private var simboloActual: String = "MXN$"

    fun actualizarCategorias(nuevoMapa: Map<Int, String>) {
        mapaCategorias = nuevoMapa
        notifyItemRangeChanged(0, currentList.size)
    }

    // 2. NUEVA FUNCIÓN PARA AVISARLE AL ADAPTADOR DEL CAMBIO
    fun actualizarMoneda(nuevaTasa: Double, nuevoSimbolo: String) {
        tasaActual = nuevaTasa
        simboloActual = nuevoSimbolo
        notifyDataSetChanged() // Hace que la lista se vuelva a dibujar sola
    }

    inner class TransaccionViewHolder(private val enlace: ItemTransaccionBinding) :
        RecyclerView.ViewHolder(enlace.root) {

        fun vincular(transaccion: Transaccion, nombreCategoria: String) {
            enlace.txtCategoria.text = nombreCategoria
            enlace.txtFecha.text = transaccion.fecha

            if (transaccion.nota.isNullOrBlank()) {
                enlace.txtNota.visibility = android.view.View.GONE
            } else {
                enlace.txtNota.visibility = android.view.View.VISIBLE
                enlace.txtNota.text = transaccion.nota
            }

            // 3. NUEVO: MULTIPLICAR EL MONTO POR LA TASA DE CAMBIO
            val montoConvertido = transaccion.monto * tasaActual

            if (transaccion.tipo == Constantes.TIPO_GASTO) {
                enlace.txtMonto.text = "-$simboloActual%.2f".format(montoConvertido)
                enlace.txtMonto.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
            } else {
                enlace.txtMonto.text = "+$simboloActual%.2f".format(montoConvertido)
                enlace.txtMonto.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
            }

            enlace.root.setOnClickListener { alHacerClic(transaccion) }
        }
    }

    private val nombresCategorias = mutableMapOf<Int, String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransaccionViewHolder {
        val enlace = ItemTransaccionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TransaccionViewHolder(enlace)
    }

    override fun onBindViewHolder(holder: TransaccionViewHolder, position: Int) {
        val transaccion = getItem(position)

        val nombreCategoria = mapaCategorias[transaccion.categoriaId] ?: "Sin categoría"
        holder.vincular(transaccion, nombreCategoria)
    }

    class DiffTransaccion : DiffUtil.ItemCallback<Transaccion>() {
        override fun areItemsTheSame(oldItem: Transaccion, newItem: Transaccion) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Transaccion, newItem: Transaccion) =
            oldItem == newItem
    }
}