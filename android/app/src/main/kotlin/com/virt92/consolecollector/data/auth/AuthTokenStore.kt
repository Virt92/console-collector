package com.virt92.consolecollector.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore("auth_prefs")

class AuthTokenStore(private val context: Context) {
    private val tokenKey = stringPreferencesKey("access_token")

    val tokenFlow: Flow<String?> = context.authDataStore.data.map { it[tokenKey] }

    suspend fun current(): String? = tokenFlow.first()

    suspend fun setToken(token: String) {
        context.authDataStore.edit { it[tokenKey] = token }
    }

    suspend fun clear() {
        context.authDataStore.edit { it.remove(tokenKey) }
    }
}
