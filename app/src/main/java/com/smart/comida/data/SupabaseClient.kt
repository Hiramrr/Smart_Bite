package com.smart.comida.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://xjhhrwaopisbemoebqzz.supabase.co",
        supabaseKey = "sb_publishable_hQ8TECVf8BYV3ixteCou4Q_zFwjKYx7"
    ) {
        install(Postgrest)
    }
}