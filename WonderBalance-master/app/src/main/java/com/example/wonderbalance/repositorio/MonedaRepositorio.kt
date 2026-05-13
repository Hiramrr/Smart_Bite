package com.example.wonderbalance.repositorio

import com.example.wonderbalance.datos.api.FrankfurterApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MonedaRepositorio {

    // 1. Configuramos el cliente de Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.frankfurter.app/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(FrankfurterApi::class.java)

    // 2. La función que usaremos en la app
    suspend fun obtenerTipoDeCambio(desde: String, hacia: String): Double? {
        return try {
            // Hacemos la petición a internet
            val respuesta = api.obtenerCambio(desde, hacia)
            // Extraemos solo el valor de la moneda destino (ej. MXN)
            respuesta.rates[hacia]
        } catch (e: Exception) {
            // Si no hay internet o falla, regresamos null
            e.printStackTrace()
            null
        }
    }
}