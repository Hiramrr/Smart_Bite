package com.smart.comida.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://xjhhrwaopisbemoebqzz.supabase.co",
        supabaseKey = "sb_publishable_hQ8TECVf8BYV3ixteCou4Q_zFwjKYx7"
    ) {
        install(Postgrest)

        defaultSerializer = KotlinXSerializer(Json { ignoreUnknownKeys = true })
    }
}