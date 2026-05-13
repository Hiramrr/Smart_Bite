package com.example.wonderbalance.ui.meta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wonderbalance.databinding.ItemMetaBinding
import com.example.wonderbalance.datos.entidad.Meta

class AdaptadorMeta(private val alHacerClic: (Meta) -> Unit
) : ListAdapter<Meta, AdaptadorMeta.MetaViewHolder>(DiffMeta()) {

    inner class MetaViewHolder(
        private val enlace: ItemMetaBinding
    ) : RecyclerView.ViewHolder(enlace.root) {

        fun vincular(meta: Meta) {
            val porcentaje = if (meta.montoObjetivo > 0)
                ((meta.montoAcumulado / meta.montoObjetivo) * 100).toInt() else 0

            enlace.txtNombre.text = meta.nombre
            enlace.txtPorcentaje.text = "$porcentaje%"
            enlace.txtMontos.text = "$${"%.2f".format(meta.montoAcumulado)} / $${"%.2f".format(meta.montoObjetivo)}"
            enlace.txtFechaLimite.text = "Límite: ${meta.fechaLimite}"
            enlace.barraProgreso.progress = porcentaje.coerceAtMost(100)

            if (porcentaje >= 100) {
                enlace.txtCumplida.visibility = View.VISIBLE
                enlace.txtFechaLimite.visibility = View.GONE
                enlace.barraProgreso.progressTintList =
                    android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#5DCAA5")
                    )
            } else {
                enlace.txtCumplida.visibility = View.GONE
                enlace.txtFechaLimite.visibility = View.VISIBLE
            }

            enlace.root.setOnClickListener {
                alHacerClic(meta)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetaViewHolder {
        val enlace = ItemMetaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MetaViewHolder(enlace)
    }

    override fun onBindViewHolder(holder: MetaViewHolder, position: Int) =
        holder.vincular(getItem(position))

    class DiffMeta : DiffUtil.ItemCallback<Meta>() {
        override fun areItemsTheSame(oldItem: Meta, newItem: Meta) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Meta, newItem: Meta) = oldItem == newItem
    }
}