package com.smart.comida.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Categoria(
    @SerialName("id") val id: Int,
    @SerialName("nombre") val nombre: String
)