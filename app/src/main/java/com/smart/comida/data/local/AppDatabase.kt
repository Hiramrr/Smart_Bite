package com.smart.comida.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.smart.comida.data.local.dao.FavoriteRecipeDao
import com.smart.comida.data.local.entity.FavoriteRecipeEntity

@Database(entities = [FavoriteRecipeEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoriteRecipeDao(): FavoriteRecipeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smartbite_local_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}