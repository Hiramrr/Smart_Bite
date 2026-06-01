package com.smart.comida.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.smart.comida.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Instancia única (Singleton) de DataStore vinculada al Contexto
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dietary_settings")

class PreferencesDataStore(private val context: Context) {

    private fun dietsKey(userId: String) = stringSetPreferencesKey("diets_$userId")
    private fun intolerancesKey(userId: String) = stringSetPreferencesKey("intolerances_$userId")

    /**
     * Lee las preferencias del usuario como un flujo reactivo (Flow).
     * Cada vez que se actualice el DataStore, este Flow emitirá el nuevo valor automáticamente.
     */
    fun getUserPreferencesFlow(userId: String): Flow<UserPreferences> {
        return context.dataStore.data.map { preferences ->
            val dietsSet = preferences[dietsKey(userId)] ?: emptySet()
            val intolerancesSet = preferences[intolerancesKey(userId)] ?: emptySet()

            UserPreferences(
                userId = userId,
                diets = dietsSet.toList(),
                intolerances = intolerancesSet.toList()
            )
        }
    }

    /**
     * Guarda las preferencias de dietas e intolerancias en el DataStore local.
     */
    suspend fun savePreferences(userId: String, diets: List<String>, intolerances: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[dietsKey(userId)] = diets.toSet()
            preferences[intolerancesKey(userId)] = intolerances.toSet()
        }
    }

    /**
     * Limpia el DataStore. Útil para cuando el usuario cierra sesión (Logout).
     */
    suspend fun clearPreferences(userId: String) {
        context.dataStore.edit { preferences ->
            preferences.remove(dietsKey(userId))
            preferences.remove(intolerancesKey(userId))
        }
    }
}
