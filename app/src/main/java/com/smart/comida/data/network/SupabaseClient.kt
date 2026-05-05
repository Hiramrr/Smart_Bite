package com.smart.comida.data.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.appleNativeLogin
import io.github.jan.supabase.compose.auth.googleNativeLogin

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://xjhhrwaopisbemoebqzz.supabase.co",
        supabaseKey = "sb_publishable_hQ8TECVf8BYV3ixteCou4Q_zFwjKYx7"
    ) {
        install(Postgrest.Companion)
        install(Storage)
        install(Auth)
        install(ComposeAuth) {
            googleNativeLogin(serverClientId = "263244792721-fe0dih9nd1svpqqaq2v55aomfqpv49mf.apps.googleusercontent.com")
        }

        defaultSerializer = KotlinXSerializer(Json { ignoreUnknownKeys = true })
    }
}