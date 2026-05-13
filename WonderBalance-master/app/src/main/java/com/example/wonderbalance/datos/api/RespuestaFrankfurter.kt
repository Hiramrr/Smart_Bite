package com.example.wonderbalance.datos.api

data class RespuestaFrankfurter(
    val amount: Double,
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)