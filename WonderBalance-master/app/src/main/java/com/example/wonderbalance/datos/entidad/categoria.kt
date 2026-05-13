package com.example.wonderbalance.datos.entidad

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categorias")
data class Categoria(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val tipo: String, // "GASTO" o "INGRESO"
    val icono: String = "ic_categoria",
    val color: String = "#7F77DD",
    val usuarioId: Int
)