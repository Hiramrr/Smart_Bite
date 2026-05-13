package com.example.wonderbalance.datos.red

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

object RedSupabase {
    val cliente = createSupabaseClient(
        supabaseUrl = "https://howupyxjpzofkvxndktl.supabase.co",
        supabaseKey = "sb_publishable_JxxJbJrWZEiAdDBFkvlrzA_o-weOW2I"
    ) {
        install(Auth)
        install(Postgrest)
    }
}