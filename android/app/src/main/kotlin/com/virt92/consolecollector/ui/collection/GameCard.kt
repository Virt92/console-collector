package com.virt92.consolecollector.ui.collection

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virt92.consolecollector.data.model.GameDto
import com.virt92.consolecollector.data.model.Rarity

/**
 * Pokemon-style collectible card for a game (disc / cartridge).
 * Mirrors [ConsoleCard] but shows the platform slug as a sub-line and a small
 * "GAME" tag in the top corner so it reads visually distinct from a console card.
 */
@Composable
fun GameCard(
    game: GameDto,
    platformSlug: String? = null,
    edition: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val rarity = game.rarity
    val visuals = gameRarityVisuals(rarity)
    val palette = visuals.palette
    val holoStrength = visuals.holoStrength

    val transition = rememberInfiniteTransition(label = "game-holo")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (rarity == Rarity.LEGENDARY) 2400 else 3600),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "game-holo-phase",
    )

    val backdropBrush = Brush.linearGradient(
        colors = palette.backdrop,
        start = Offset(0f, 0f),
        end = Offset(700f, 1000f),
    )
    val sweepBrush = Brush.linearGradient(
        colors = palette.sweep,
        start = Offset(phase * 800f - 400f, 0f),
        end = Offset(phase * 800f, 1000f),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.72f)
            .clip(RoundedCornerShape(20.dp))
            .border(BorderStroke(2.dp, palette.border), RoundedCornerShape(20.dp))
            .let { if (onClick != null) it.clickable { onClick() } else it }
            .background(backdropBrush),
    ) {
        if (holoStrength > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(holoStrength)
                    .background(sweepBrush),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(palette.badgeBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "GAME · ${rarity.name}",
                        color = palette.badgeFg,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                Spacer(Modifier.height(4.dp))
                game.releaseYear?.let {
                    Text(
                        text = it.toString(),
                        color = palette.foreground.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.artBackdrop),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = gameInitials(game.title),
                    color = palette.foreground,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 44.sp),
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            }

            Column {
                Text(
                    text = game.title,
                    color = palette.foreground,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Start,
                    maxLines = 2,
                )
                Text(
                    text = listOfNotNull(
                        platformSlug ?: game.platforms.firstOrNull(),
                        game.developer ?: game.publisher,
                        edition,
                    ).joinToString(" · "),
                    color = palette.foreground.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

private data class GameCardPalette(
    val backdrop: List<Color>,
    val sweep: List<Color>,
    val border: Color,
    val foreground: Color,
    val artBackdrop: Color,
    val badgeBg: Color,
    val badgeFg: Color,
)

private data class GameCardVisuals(
    val palette: GameCardPalette,
    val holoStrength: Float,
)

private fun gameRarityVisuals(rarity: Rarity): GameCardVisuals {
    val palette = when (rarity) {
        Rarity.COMMON -> GameCardPalette(
            backdrop = listOf(Color(0xFF1F2937), Color(0xFF111827)),
            sweep = listOf(Color.Transparent, Color.White.copy(alpha = 0.08f), Color.Transparent),
            border = Color(0xFF374151),
            foreground = Color.White,
            artBackdrop = Color(0x3300000A),
            badgeBg = Color(0xFF9CA3AF),
            badgeFg = Color(0xFF111827),
        )
        Rarity.UNCOMMON -> GameCardPalette(
            backdrop = listOf(Color(0xFF064E3B), Color(0xFF0F766E)),
            sweep = listOf(Color.Transparent, Color(0x3334D399), Color.Transparent),
            border = Color(0xFF14B8A6),
            foreground = Color.White,
            artBackdrop = Color(0x3300000A),
            badgeBg = Color(0xFF34D399),
            badgeFg = Color(0xFF064E3B),
        )
        Rarity.RARE -> GameCardPalette(
            backdrop = listOf(Color(0xFF0C4A6E), Color(0xFF0E7490)),
            sweep = listOf(
                Color.Transparent,
                Color(0x6638BDF8),
                Color(0x66E0F2FE),
                Color(0x6638BDF8),
                Color.Transparent,
            ),
            border = Color(0xFF38BDF8),
            foreground = Color.White,
            artBackdrop = Color(0x3300000A),
            badgeBg = Color(0xFF38BDF8),
            badgeFg = Color(0xFF0C4A6E),
        )
        Rarity.EPIC -> GameCardPalette(
            backdrop = listOf(Color(0xFF4C1D95), Color(0xFF6D28D9)),
            sweep = listOf(
                Color(0x66FF6B9D),
                Color(0x66A78BFA),
                Color(0x6622D3EE),
                Color(0x66FBBF24),
                Color(0x66FF6B9D),
            ),
            border = Color(0xFFC084FC),
            foreground = Color.White,
            artBackdrop = Color(0x3300000A),
            badgeBg = Color(0xFFC084FC),
            badgeFg = Color(0xFF3B0764),
        )
        Rarity.LEGENDARY -> GameCardPalette(
            backdrop = listOf(Color(0xFF7C2D12), Color(0xFFB45309)),
            sweep = listOf(
                Color(0xCCFCD34D),
                Color(0xCCFB923C),
                Color(0xCCEF4444),
                Color(0xCCFCD34D),
                Color(0xCCFFFFFF),
            ),
            border = Color(0xFFFCD34D),
            foreground = Color.White,
            artBackdrop = Color(0x3300000A),
            badgeBg = Color(0xFFFCD34D),
            badgeFg = Color(0xFF7C2D12),
        )
    }
    val holoStrength = when (rarity) {
        Rarity.COMMON -> 0f
        Rarity.UNCOMMON -> 0.3f
        Rarity.RARE -> 0.6f
        Rarity.EPIC -> 0.85f
        Rarity.LEGENDARY -> 1f
    }
    return GameCardVisuals(palette, holoStrength)
}

private fun gameInitials(title: String): String {
    val words = title.split(" ", "-", ":").filter { it.isNotBlank() }
    return when (words.size) {
        0 -> "?"
        1 -> words[0].take(2).uppercase()
        else -> (words[0].take(1) + words[1].take(1)).uppercase()
    }
}
