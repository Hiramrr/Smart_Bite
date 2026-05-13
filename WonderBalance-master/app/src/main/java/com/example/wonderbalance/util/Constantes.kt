package com.example.wonderbalance.util

object Constantes {

    // Tipos de transacción
    const val TIPO_GASTO = "GASTO"
    const val TIPO_INGRESO = "INGRESO"

    // Formato de fechas
    const val FORMATO_FECHA = "yyyy-MM-dd"
    const val FORMATO_FECHA_VISIBLE = "dd/MM/yyyy"
    const val FORMATO_MES_ANIO = "yyyy-MM"

    // Umbral de alerta de presupuesto (80%)
    const val UMBRAL_ALERTA_PRESUPUESTO = 0.80

    // Categorías predeterminadas al registrarse
    val CATEGORIAS_PREDETERMINADAS_GASTO = listOf(
        "Alimentación", "Transporte", "Salud",
        "Ocio", "Ropa", "Servicios", "Educación"
    )
    val CATEGORIAS_PREDETERMINADAS_INGRESO = listOf(
        "Salario", "Negocios", "Rentas", "Inversiones"
    )
}