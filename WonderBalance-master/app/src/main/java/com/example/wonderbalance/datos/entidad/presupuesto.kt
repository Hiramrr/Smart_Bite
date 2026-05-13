package com.example.wonderbalance.datos.entidad

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "presupuestos")
data class Presupuesto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val categoriaId: Int,
    val montoLimite: Double,
    val mes: Int,   // 1-12
    val anio: Int,
    val usuarioId: Int
)