package com.example.wonderbalance.ui.presupuesto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wonderbalance.databinding.ItemPresupuestoBinding
import com.example.wonderbalance.datos.entidad.Presupuesto

class AdaptadorPresupuesto : ListAdapter<Presupuesto, AdaptadorPresupuesto.PresupuestoViewHolder>(DiffPresupuesto()) {

    private val nombresCategorias = mutableMapOf<Int, String>()
    private val gastosActuales = mutableMapOf<Int, Double>()

    fun actualizarCategorias(mapa: Map<Int, String>) {
        nombresCategorias.clear()
        nombresCategorias.putAll(mapa)
        notifyDataSetChanged()
    }

    fun actualizarGasto(presupuestoId: Int, gasto: Double) {
        gastosActuales[presupuestoId] = gasto
        notifyDataSetChanged()
    }

    inner class PresupuestoViewHolder(
        private val enlace: ItemPresupuestoBinding
    ) : RecyclerView.ViewHolder(enlace.root) {

        fun vincular(presupuesto: Presupuesto) {
            val nombre = nombresCategorias[presupuesto.categoriaId] ?: "Sin categoría"
            val gastado = gastosActuales[presupuesto.id] ?: 0.0
            val porcentaje = if (presupuesto.montoLimite > 0)
                ((gastado / presupuesto.montoLimite) * 100).toInt() else 0

            enlace.txtCategoria.text = nombre
            enlace.txtPorcentaje.text = "$porcentaje%"
            enlace.txtMontos.text = "$${"%.2f".format(gastado)} / $${"%.2f".format(presupuesto.montoLimite)}"
            enlace.barraProgreso.progress = porcentaje.coerceAtMost(100)

            if (porcentaje >= 100) {
                enlace.barraProgreso.progressTintList =
                    android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#E24B4A")
                    )
                enlace.txtExcedido.visibility = View.VISIBLE
                enlace.txtPorcentaje.setTextColor(android.graphics.Color.parseColor("#E24B4A"))
            } else if (porcentaje >= 80) {
                enlace.barraProgreso.progressTintList =
                    android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#EF9F27")
                    )
                enlace.txtExcedido.visibility = View.GONE
                enlace.txtPorcentaje.setTextColor(android.graphics.Color.parseColor("#EF9F27"))
            } else {
                enlace.barraProgreso.progressTintList =
                    android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#7F77DD")
                    )
                enlace.txtExcedido.visibility = View.GONE
                enlace.txtPorcentaje.setTextColor(android.graphics.Color.parseColor("#7F77DD"))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresupuestoViewHolder {
        val enlace = ItemPresupuestoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PresupuestoViewHolder(enlace)
    }

    override fun onBindViewHolder(holder: PresupuestoViewHolder, position: Int) =
        holder.vincular(getItem(position))

    class DiffPresupuesto : DiffUtil.ItemCallback<Presupuesto>() {
        override fun areItemsTheSame(oldItem: Presupuesto, newItem: Presupuesto) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Presupuesto, newItem: Presupuesto) =
            oldItem == newItem
    }
}