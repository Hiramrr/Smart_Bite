package com.example.wonderbalance.repositorio

import com.example.wonderbalance.datos.entidad. UsuarioSupabase
import com.example.wonderbalance.datos.red.RedSupabase
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.withContext

class UsuarioRepositorioSupabase {

    suspend fun registrarUsuario(nombre: String, correo: String, contrasena: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val respuestaAuth = RedSupabase.cliente.auth.signUpWith(Email) {
                    email = correo
                    password = contrasena
                }

                if (respuestaAuth != null) {
                    val perfil = UsuarioSupabase(
                        auth_id = respuestaAuth.id,
                        nombre = nombre,
                        correo = correo
                    )
                    RedSupabase.cliente.postgrest["usuarios"].upsert(perfil)
                    return@withContext true
                }
                false
            } catch (e: Exception) {
                if (e.message?.contains("rate limit") == true) {
                    println("Límite de email alcanzado, pero procediendo...")
                }
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun iniciarSesion(correo: String, contrasena: String): UsuarioSupabase? {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Iniciar sesión en Supabase Auth
                RedSupabase.cliente.auth.signInWith(Email) {
                    email = correo
                    password = contrasena
                }

                // 2. Obtener el ID único de la sesión actual
                val userId = RedSupabase.cliente.auth.currentUserOrNull()?.id

                if (userId != null) {
                    // 3. Descargar el perfil completo desde la tabla 'usuarios'
                    val perfil = RedSupabase.cliente.postgrest["usuarios"]
                        .select(columns = Columns.ALL) {
                            filter {
                                eq("auth_id", userId)
                            }
                        }.decodeSingleOrNull<UsuarioSupabase>()

                    return@withContext perfil
                }
                return@withContext null
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }
    }
}