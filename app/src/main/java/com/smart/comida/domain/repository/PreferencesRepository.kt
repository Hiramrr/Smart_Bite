package com.smart.comida.domain.repository

import com.smart.comida.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    /**
     * Obtiene el flujo reactivo local de las preferencias del usuario.
     */
    fun getPreferencesFlow(userId: String): Flow<UserPreferences>

    /**
     * Sincroniza las preferencias desde Supabase hacia el DataStore local.
     * Útil al iniciar sesión o abrir la sección de configuración por primera vez.
     */
    suspend fun fetchPreferencesFromRemote(userId: String): Result<UserPreferences?>

    /**
     * Realiza un Upsert en Supabase y actualiza el DataStore local si tiene éxito.
     */
    suspend fun savePreferences(userId: String, diets: List<String>, intolerances: List<String>): Result<Unit>
}