package com.smart.comida.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticuloCompra(
    @SerialName("id") val id: Int? = null,
    @SerialName("nombre") val nombre: String,
    @SerialName("cantidad_esperada") val cantidadEsperada: Double? = null,
    @SerialName("unidad") val unidad: String? = null,
    @SerialName("estado") val estado: String? = null,
    @SerialName("fecha_agregado") val fechaAgregado: String? = null
)
