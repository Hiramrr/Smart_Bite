package com.example.wonderbalance.datos.basededatos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.wonderbalance.datos.dao.*
import com.example.wonderbalance.datos.entidad.*

@Database(
    entities = [
        Usuario::class,
        Categoria::class,
        Transaccion::class,
        Presupuesto::class,
        Meta::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BaseDeDatos : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun transaccionDao(): TransaccionDao
    abstract fun presupuestoDao(): PresupuestoDao
    abstract fun metaDao(): MetaDao

    companion object {
        @Volatile
        private var INSTANCIA: BaseDeDatos? = null

        fun obtenerInstancia(context: Context): BaseDeDatos {
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    BaseDeDatos::class.java,
                    "wonderbalance_db"
                ).build()
                INSTANCIA = instancia
                instancia
            }
        }
    }
}