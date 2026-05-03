package com.virt92.consolecollector.di

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.virt92.consolecollector.BuildConfig
import com.virt92.consolecollector.data.api.ApiService
import com.virt92.consolecollector.data.api.AuthInterceptor
import com.virt92.consolecollector.data.auth.AuthTokenStore
import com.virt92.consolecollector.data.repository.CollectionRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {
    val authStore = AuthTokenStore(context.applicationContext)

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(authStore))
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            },
        )
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val api: ApiService = Retrofit.Builder()
        .baseUrl(ensureTrailingSlash(BuildConfig.API_BASE_URL))
        .client(httpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(ApiService::class.java)

    val repository = CollectionRepository(api, authStore)

    private fun ensureTrailingSlash(url: String): String =
        if (url.endsWith("/")) url else "$url/"
}
