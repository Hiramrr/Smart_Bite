package com.example.wonderbalance.datos.api

import retrofit2.http.GET
import retrofit2.http.Query

interface FrankfurterApi {
    // Esto arma la URL: latest?from=USD&to=MXN
    @GET("latest")
    suspend fun obtenerCambio(
        @Query("from") desde: String,
        @Query("to") hacia: String
    ): RespuestaFrankfurter
}