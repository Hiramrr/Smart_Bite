package com.example.wonderbalance.viewmodel

sealed class ResultadoOperacion {
    data class Exito(val mensaje: String) : ResultadoOperacion()
    data class Error(val mensaje: String) : ResultadoOperacion()
}