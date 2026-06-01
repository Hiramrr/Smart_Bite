package com.smart.comida.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Consumo(
    @SerialName("id") val id: Int? = null,
    @SerialName("nombre") val nombre: String,
    @SerialName("cantidad") val cantidad: Float,
    @SerialName("unidad") val unidad: String?,
    @SerialName("categoria_id") val categoriaId: Int? = null,
    @SerialName("fecha_consumo") val fechaConsumo: String,
    @SerialName("user_id") val userId: String? = null
)
