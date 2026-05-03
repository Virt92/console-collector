package com.virt92.consolecollector.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

enum class Rarity { COMMON, UNCOMMON, RARE, EPIC, LEGENDARY }

enum class ItemStatus { OWNED, FOR_SALE, FOR_TRADE, GIVING_AWAY }

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val displayName: String,
    val city: String? = null,
    val country: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null,
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val user: UserDto,
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String,
    val city: String? = null,
    val country: String? = null,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class ConsoleModelDto(
    val id: String,
    val slug: String,
    val name: String,
    val manufacturer: String,
    val year: Int,
    val region: String? = null,
    val rarity: Rarity,
    val imageUrl: String? = null,
    val description: String? = null,
    val aliases: List<String> = emptyList(),
)

@Serializable
data class CollectionItemDto(
    val id: String,
    val userId: String,
    val consoleModelId: String,
    val status: ItemStatus,
    val notes: String? = null,
    val photos: List<String> = emptyList(),
    val recognized: JsonElement? = null,
    val createdAt: String,
    val updatedAt: String,
    val consoleModel: ConsoleModelDto,
)

@Serializable
data class RecognizeRequest(
    val images: List<String>,
)

@Serializable
data class RecognizeResponse(
    val consoleName: String,
    val consoleSlug: String,
    val consoleModelId: String? = null,
    val rarity: Rarity,
    val confidence: Double,
    val reasoning: String,
    val details: JsonElement? = null,
)

@Serializable
data class CreateCollectionItemRequest(
    val consoleModelId: String,
    val status: ItemStatus = ItemStatus.OWNED,
    val notes: String? = null,
    val photos: List<String> = emptyList(),
    val recognized: JsonElement? = null,
)

@Serializable
data class UpdateCollectionItemRequest(
    val status: ItemStatus? = null,
    val notes: String? = null,
)

@Serializable
data class UpdateProfileRequest(
    val displayName: String? = null,
    val city: String? = null,
    val country: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
)

@Serializable
data class CollectionStats(
    val total: Int,
    val uniqueModels: Int,
    val catalogSize: Int,
    val byRarity: Map<String, Int>,
)

@Serializable
data class ShareLinkResponse(
    val token: String,
    val url: String,
)

@Serializable
data class GameDto(
    val id: String,
    val slug: String,
    val title: String,
    val platforms: List<String> = emptyList(),
    val releaseYear: Int? = null,
    val publisher: String? = null,
    val developer: String? = null,
    val genres: List<String> = emptyList(),
    val coverUrl: String? = null,
    val summary: String? = null,
    val rarity: Rarity = Rarity.COMMON,
    val aliases: List<String> = emptyList(),
)

@Serializable
data class GameItemDto(
    val id: String,
    val userId: String,
    val gameId: String,
    val platformSlug: String,
    val edition: String? = null,
    val region: String? = null,
    val status: ItemStatus,
    val notes: String? = null,
    val photos: List<String> = emptyList(),
    val recognized: JsonElement? = null,
    val createdAt: String,
    val updatedAt: String,
    val game: GameDto,
)

@Serializable
data class RecognizeGameResponse(
    val title: String,
    val slug: String,
    val gameId: String? = null,
    val coverUrl: String? = null,
    val platformSlug: String,
    val platformName: String? = null,
    val region: String? = null,
    val edition: String? = null,
    val rarity: Rarity,
    val confidence: Double,
    val reasoning: String,
    val details: JsonElement? = null,
)

@Serializable
data class CreateGameItemRequest(
    val gameId: String,
    val platformSlug: String,
    val edition: String? = null,
    val region: String? = null,
    val status: ItemStatus = ItemStatus.OWNED,
    val notes: String? = null,
    val photos: List<String> = emptyList(),
    val recognized: JsonElement? = null,
)

@Serializable
data class UpdateGameItemRequest(
    val status: ItemStatus? = null,
    val edition: String? = null,
    val region: String? = null,
    val notes: String? = null,
)

@Serializable
data class GameStats(
    val total: Int,
    val uniqueGames: Int,
    val catalogSize: Int,
    val byRarity: Map<String, Int>,
    val byPlatform: Map<String, Int> = emptyMap(),
)
