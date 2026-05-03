package com.virt92.consolecollector.data.api

import com.virt92.consolecollector.data.model.AuthResponse
import com.virt92.consolecollector.data.model.CollectionItemDto
import com.virt92.consolecollector.data.model.CollectionStats
import com.virt92.consolecollector.data.model.ConsoleModelDto
import com.virt92.consolecollector.data.model.CreateCollectionItemRequest
import com.virt92.consolecollector.data.model.LoginRequest
import com.virt92.consolecollector.data.model.RecognizeRequest
import com.virt92.consolecollector.data.model.RecognizeResponse
import com.virt92.consolecollector.data.model.RegisterRequest
import com.virt92.consolecollector.data.model.ShareLinkResponse
import com.virt92.consolecollector.data.model.UpdateCollectionItemRequest
import com.virt92.consolecollector.data.model.UpdateProfileRequest
import com.virt92.consolecollector.data.model.UserDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("api/auth/me")
    suspend fun me(): UserDto

    @PATCH("api/users/me")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): UserDto

    @GET("api/consoles")
    suspend fun listConsoles(
        @Query("manufacturer") manufacturer: String? = null,
        @Query("search") search: String? = null,
    ): List<ConsoleModelDto>

    @POST("api/recognize/console")
    suspend fun recognizeConsole(@Body body: RecognizeRequest): RecognizeResponse

    @GET("api/collection")
    suspend fun listCollection(): List<CollectionItemDto>

    @GET("api/collection/stats")
    suspend fun collectionStats(): CollectionStats

    @POST("api/collection")
    suspend fun addToCollection(@Body body: CreateCollectionItemRequest): CollectionItemDto

    @PATCH("api/collection/{id}")
    suspend fun updateCollectionItem(
        @Path("id") id: String,
        @Body body: UpdateCollectionItemRequest,
    ): CollectionItemDto

    @DELETE("api/collection/{id}")
    suspend fun deleteCollectionItem(@Path("id") id: String)

    @POST("api/share/collection")
    suspend fun shareCollection(): ShareLinkResponse

    @POST("api/share/item/{itemId}")
    suspend fun shareItem(@Path("itemId") itemId: String): ShareLinkResponse
}
