package com.smart.comida.util

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

object TranslationHelper {

    // Configuramos los traductores
    private val esToEnOptions = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.SPANISH)
        .setTargetLanguage(TranslateLanguage.ENGLISH)
        .build()
    private val esToEnTranslator = Translation.getClient(esToEnOptions)

    private val enToEsOptions = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH)
        .setTargetLanguage(TranslateLanguage.SPANISH)
        .build()
    private val enToEsTranslator = Translation.getClient(enToEsOptions)

    // Condición: Descargar el modelo por WiFi o Datos si no existe
    private val conditions = DownloadConditions.Builder().build()

    // Inicializa y descarga los diccionarios (solo toma tiempo la primera vez)
    suspend fun initModels() {
        esToEnTranslator.downloadModelIfNeeded(conditions).await()
        enToEsTranslator.downloadModelIfNeeded(conditions).await()
    }

    // Funciones suspendidas para traducir
    suspend fun translateToEnglish(text: String): String {
        return try {
            esToEnTranslator.translate(text).await()
        } catch (e: Exception) {
            text // Si falla, devuelve el original
        }
    }

    suspend fun translateToSpanish(text: String): String {
        return try {
            enToEsTranslator.translate(text).await()
        } catch (e: Exception) {
            text
        }
    }
}