package com.smart.comida.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    @SerialName("user_id")
    val userId: String,

    @SerialName("diets")
    val diets: List<String> = emptyList(),

    @SerialName("intolerances")
    val intolerances: List<String> = emptyList()
)