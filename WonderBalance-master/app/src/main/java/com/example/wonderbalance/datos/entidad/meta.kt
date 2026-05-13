package com.example.wonderbalance.datos.entidad

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "metas")
data class Meta(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val montoObjetivo: Double,
    val montoAcumulado: Double = 0.0,
    val fechaLimite: String, // formato "yyyy-MM-dd"
    val usuarioId: Int
)