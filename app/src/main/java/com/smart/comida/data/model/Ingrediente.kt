package com.smart.comida.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Ingrediente(
    @SerialName("id") val id: Int? = null, // Es null al crear porque la BD lo genera
    @SerialName("nombre") val nombre: String,
    @SerialName("cantidad") val cantidad: Float,
    @SerialName("unidad") val unidad: String?,
    @SerialName("fecha_caducidad") val fechaCaducidad: String?, // Formato YYYY-MM-DD
    @SerialName("categoria_id") val categoriaId: Int? = null,
    @SerialName("imagen_url") val imagenUrl: String? = null
)