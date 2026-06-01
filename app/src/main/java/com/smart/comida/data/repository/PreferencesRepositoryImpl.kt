package com.smart.comida.data.repository

import com.smart.comida.data.local.PreferencesDataStore
import com.smart.comida.data.model.UserPreferences
import com.smart.comida.data.network.SupabaseClient
import com.smart.comida.domain.repository.PreferencesRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PreferencesRepositoryImpl(
    private val preferencesDataStore: PreferencesDataStore
) : PreferencesRepository {

    private val postgrest = SupabaseClient.client.postgrest["user_preferences"]

    override fun getPreferencesFlow(userId: String): Flow<UserPreferences> {
        return preferencesDataStore.getUserPreferencesFlow(userId)
    }

    override suspend fun fetchPreferencesFromRemote(userId: String): Result<UserPreferences?> {
        return runCatching {
            // Buscamos las preferencias en Supabase para el usuario actual
            val remotePrefs = postgrest.select(columns = Columns.ALL) {
                filter {
                    eq("user_id", userId)
                }
            }.decodeSingleOrNull<UserPreferences>()

            // Si existen datos en la nube, actualizamos nuestra fuente de verdad local
            if (remotePrefs != null) {
                preferencesDataStore.savePreferences(userId, remotePrefs.diets, remotePrefs.intolerances)
            }
            remotePrefs
        }
    }

    override suspend fun savePreferences(
        userId: String,
        diets: List<String>,
        intolerances: List<String>
    ): Result<Unit> {
        return runCatching {
            // 1. Validar Flujo Alternativo FA-01 (Sin cambios)
            // Obtenemos el último valor guardado localmente de forma síncrona/bloqueante en esta corrutina
            val currentLocal = preferencesDataStore.getUserPreferencesFlow(userId).first()

            // Si el contenido es idéntico, evitamos la petición de red y retornamos éxito de inmediato
            if (currentLocal.diets.sorted() == diets.sorted() &&
                currentLocal.intolerances.sorted() == intolerances.sorted()) {
                return Result.success(Unit) // Se manejará en el ViewModel como "Sin cambios"
            }

            // 2. Si hay cambios, preparamos el DTO para el Upsert
            val updatedPreferences = UserPreferences(
                userId = userId,
                diets = diets,
                intolerances = intolerances
            )

            postgrest.upsert(value = updatedPreferences, onConflict = "user_id")

            // 4. Si el servidor responde de manera exitosa, actualizamos el almacenamiento local
            preferencesDataStore.savePreferences(userId, diets, intolerances)
        }
    }
}
