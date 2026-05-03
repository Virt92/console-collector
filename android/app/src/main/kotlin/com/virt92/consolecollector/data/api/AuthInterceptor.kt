package com.virt92.consolecollector.data.api

import com.virt92.consolecollector.data.auth.AuthTokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenStore: AuthTokenStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val token = runBlocking { tokenStore.current() }
        return if (token.isNullOrBlank() || req.header("Authorization") != null) {
            chain.proceed(req)
        } else {
            chain.proceed(req.newBuilder().header("Authorization", "Bearer $token").build())
        }
    }
}
