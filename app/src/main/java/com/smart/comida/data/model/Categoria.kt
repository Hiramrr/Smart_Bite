package com.smart.comida.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Categoria(
    @SerialName("id") val id: Int? = null,
    @SerialName("nombre") val nombre: String,
    @SerialName("user_id") val userId: String? = null
)