package com.smart.comida.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class OpenFoodFactsRepository {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    suspend fun buscarProducto(barcode: String): Result<ProductDetails?> {
        return try {
            val url = "https://es.openfoodfacts.org/api/v2/product/$barcode.json"
            val response: OpenFoodFactsResponse = client.get(url).body()
            
            if (response.status == 1) {
                val product = response.product
                val details = ProductDetails(
                    nombre = product?.productName ?: product?.productNameEs ?: "Producto desconocido",
                    imagenUrl = product?.imageFrontUrl ?: product?.imageFrontSmallUrl,
                    marca = product?.brands
                )
                Result.success(details)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun buscarProductoPorNombre(nombre: String): Result<ProductDetails?> {
        return try {
            val searchTerms = nombre.replace(" ", "+")
            val url = "https://world.openfoodfacts.org/cgi/search.pl?search_terms=$searchTerms&search_simple=1&action=process&json=1&fields=product_name,product_name_es,image_front_url,image_front_small_url,brands"
            val response: OpenFoodFactsSearchResponse = client.get(url).body()
            
            if (response.count > 0 && response.products.isNotEmpty()) {
                val product = response.products.first()
                val details = ProductDetails(
                    nombre = product.productName ?: product.productNameEs ?: nombre,
                    imagenUrl = product.imageFrontUrl ?: product.imageFrontSmallUrl,
                    marca = product.brands
                )
                Result.success(details)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Serializable
data class OpenFoodFactsResponse(
    val status: Int,
    val product: Product? = null
)

@Serializable
data class OpenFoodFactsSearchResponse(
    val count: Int = 0,
    val products: List<SearchProduct> = emptyList()
)

@Serializable
data class SearchProduct(
    @kotlinx.serialization.SerialName("product_name") val productName: String? = null,
    @kotlinx.serialization.SerialName("product_name_es") val productNameEs: String? = null,
    @kotlinx.serialization.SerialName("image_front_url") val imageFrontUrl: String? = null,
    @kotlinx.serialization.SerialName("image_front_small_url") val imageFrontSmallUrl: String? = null,
    val brands: String? = null
)

@Serializable
data class Product(
    @kotlinx.serialization.SerialName("product_name") val productName: String? = null,
    @kotlinx.serialization.SerialName("product_name_es") val productNameEs: String? = null,
    @kotlinx.serialization.SerialName("image_front_url") val imageFrontUrl: String? = null,
    @kotlinx.serialization.SerialName("image_front_small_url") val imageFrontSmallUrl: String? = null,
    val brands: String? = null
)

data class ProductDetails(
    val nombre: String,
    val imagenUrl: String?,
    val marca: String?
)
