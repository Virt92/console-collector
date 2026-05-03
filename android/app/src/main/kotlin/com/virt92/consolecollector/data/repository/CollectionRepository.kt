package com.virt92.consolecollector.data.repository

import com.virt92.consolecollector.data.api.ApiService
import com.virt92.consolecollector.data.auth.AuthTokenStore
import com.virt92.consolecollector.data.model.AuthResponse
import com.virt92.consolecollector.data.model.CollectionItemDto
import com.virt92.consolecollector.data.model.CollectionStats
import com.virt92.consolecollector.data.model.ConsoleModelDto
import com.virt92.consolecollector.data.model.CreateCollectionItemRequest
import com.virt92.consolecollector.data.model.CreateGameItemRequest
import com.virt92.consolecollector.data.model.GameDto
import com.virt92.consolecollector.data.model.GameItemDto
import com.virt92.consolecollector.data.model.GameStats
import com.virt92.consolecollector.data.model.LoginRequest
import com.virt92.consolecollector.data.model.RecognizeGameResponse
import com.virt92.consolecollector.data.model.RecognizeRequest
import com.virt92.consolecollector.data.model.RecognizeResponse
import com.virt92.consolecollector.data.model.RegisterRequest
import com.virt92.consolecollector.data.model.ShareLinkResponse
import com.virt92.consolecollector.data.model.UpdateGameItemRequest
import com.virt92.consolecollector.data.model.UpdateProfileRequest
import com.virt92.consolecollector.data.model.UserDto

class CollectionRepository(
    private val api: ApiService,
    private val authStore: AuthTokenStore,
) {
    suspend fun login(email: String, password: String): AuthResponse {
        val res = api.login(LoginRequest(email = email, password = password))
        authStore.setToken(res.accessToken)
        return res
    }

    suspend fun register(
        email: String,
        password: String,
        displayName: String,
        city: String?,
        country: String?,
    ): AuthResponse {
        val res = api.register(
            RegisterRequest(
                email = email,
                password = password,
                displayName = displayName,
                city = city,
                country = country,
            ),
        )
        authStore.setToken(res.accessToken)
        return res
    }

    suspend fun logout() {
        authStore.clear()
    }

    suspend fun me(): UserDto = api.me()

    suspend fun updateProfile(request: UpdateProfileRequest): UserDto =
        api.updateProfile(request)

    suspend fun listConsoles(): List<ConsoleModelDto> = api.listConsoles()

    suspend fun listCollection(): List<CollectionItemDto> = api.listCollection()

    suspend fun stats(): CollectionStats = api.collectionStats()

    suspend fun recognize(images: List<String>): RecognizeResponse =
        api.recognizeConsole(RecognizeRequest(images))

    suspend fun addToCollection(request: CreateCollectionItemRequest): CollectionItemDto =
        api.addToCollection(request)

    suspend fun deleteCollectionItem(id: String) = api.deleteCollectionItem(id)

    suspend fun shareCollection(): ShareLinkResponse = api.shareCollection()

    suspend fun shareItem(itemId: String): ShareLinkResponse = api.shareItem(itemId)

    suspend fun listGames(platform: String? = null, search: String? = null): List<GameDto> =
        api.listGames(platform = platform, search = search)

    suspend fun recognizeGame(images: List<String>): RecognizeGameResponse =
        api.recognizeGame(RecognizeRequest(images))

    suspend fun listGameItems(): List<GameItemDto> = api.listGameItems()

    suspend fun gameStats(): GameStats = api.gameStats()

    suspend fun addGameItem(request: CreateGameItemRequest): GameItemDto =
        api.addGameItem(request)

    suspend fun updateGameItem(id: String, request: UpdateGameItemRequest): GameItemDto =
        api.updateGameItem(id, request)

    suspend fun deleteGameItem(id: String) = api.deleteGameItem(id)

    suspend fun shareGameItem(itemId: String): ShareLinkResponse =
        api.shareGameItem(itemId)
}
