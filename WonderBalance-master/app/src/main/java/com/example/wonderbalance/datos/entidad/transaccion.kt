package com.example.wonderbalance.datos.entidad

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transacciones")
data class Transaccion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val monto: Double,
    val tipo: String, // "GASTO" o "INGRESO"
    val categoriaId: Int,
    val fecha: String, // formato "yyyy-MM-dd"
    val nota: String = "",
    val usuarioId: Int
)