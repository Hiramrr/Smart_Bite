package com.smart.comida.data.network

import com.smart.comida.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

/**
 * Cliente de red centralizado para la infraestructura de Supabase.
 * Implementa el patrón Singleton a través de un 'object' nativo de Kotlin.
 */
object SupabaseClient {

    private val SUPABASE_URL = BuildConfig.SUPABASE_URL
    private val SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY
    private val WEB_CLIENT_ID = BuildConfig.WEB_CLIENT_ID

    var currentUserId: String? = null

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        // 1. Configuración de Serialización Tolerante a Fallos
        defaultSerializer = KotlinXSerializer(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })

        // 2. Módulo de Autenticación Base
        install(Auth) {
            scheme = "smartbite"
            host = "login-callback"
        }

        // 3. Módulo de Autenticación Nativa (Credential Manager)
        install(ComposeAuth) {
            googleNativeLogin(serverClientId = WEB_CLIENT_ID)
        }

        // 4. Módulo de Base de Datos Relacional
        install(Postgrest)

        // 5. Módulo de Almacenamiento de Archivos
        install(Storage)
    }
}