package com.smart.comida.data.network

import android.content.Context
import com.smart.comida.data.local.PreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // Necesitaremos inicializar esta referencia desde la Application clase o MainActivity
    private var dataStore: PreferencesDataStore? = null

    fun initialize(context: Context) {
        dataStore = PreferencesDataStore(context.applicationContext)
    }

    /**
     * Interceptor que añade dinámicamente las preferencias dietéticas a la URL
     * de cualquier petición dirigida a nuestra API.
     */
    private val dietaryInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()

        // Si no está inicializado o no hay usuario activo, enviamos la petición tal cual
        val currentUserId = SupabaseClient.currentUserId
        if (dataStore == null || currentUserId == null) {
            return@Interceptor chain.proceed(originalRequest)
        }

        // Leemos de forma síncrona el estado actual de las preferencias en el DataStore
        val preferences = runBlocking {
            dataStore!!.getUserPreferencesFlow(currentUserId).first()
        }

        // Construimos la nueva URL añadiendo los Query Parameters
        val urlBuilder: HttpUrl.Builder = originalRequest.url.newBuilder()

        // Añadimos cada dieta como un parámetro (ej: ?diet=vegan&diet=vegetarian)
        preferences.diets.forEach { diet ->
            urlBuilder.addQueryParameter("diet", diet)
        }

        // Añadimos cada intolerancia (ej: ?intolerance=gluten)
        preferences.intolerances.forEach { intolerance ->
            urlBuilder.addQueryParameter("intolerance", intolerance)
        }

        val newUrl = urlBuilder.build()
        val newRequest = originalRequest.newBuilder().url(newUrl).build()

        chain.proceed(newRequest)
    }

    private val okHttpClient: OkHttpClient by lazy {
        // Creamos el logger para ver las peticiones en el Logcat
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Mostrará la URL completa y los datos
        }

        OkHttpClient.Builder()
            .addInterceptor(dietaryInterceptor) // Tu interceptor de dietas
            .addInterceptor(logging)            // El interceptor que imprime en consola
            .build()
    }

    val api: SmartBiteApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Adjuntamos el cliente con el interceptor configurado
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SmartBiteApi::class.java)
    }


}