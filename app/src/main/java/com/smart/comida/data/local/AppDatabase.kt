package com.smart.comida.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.smart.comida.data.local.dao.FavoriteRecipeDao
import com.smart.comida.data.local.entity.FavoriteRecipeEntity

@Database(entities = [FavoriteRecipeEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoriteRecipeDao(): FavoriteRecipeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE favorite_recipes_new (
                        externalRecipeId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        imageUrl TEXT NOT NULL,
                        recipeDataJson TEXT NOT NULL,
                        userId TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL,
                        PRIMARY KEY(externalRecipeId, userId)
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO favorite_recipes_new (externalRecipeId, title, imageUrl, recipeDataJson, userId, createdAt)
                    SELECT externalRecipeId, title, imageUrl, recipeDataJson, '', createdAt FROM favorite_recipes
                """.trimIndent())
                db.execSQL("DROP TABLE favorite_recipes")
                db.execSQL("ALTER TABLE favorite_recipes_new RENAME TO favorite_recipes")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smartbite_local_db"
                ).addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
