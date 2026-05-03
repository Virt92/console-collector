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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virt92.consolecollector.data.model.ConsoleModelDto
import com.virt92.consolecollector.data.model.Rarity

/**
 * Pokemon-style collectible card. The visual treatment escalates with rarity:
 * COMMON     → matte gradient.
 * UNCOMMON   → soft sheen.
 * RARE       → animated holographic sweep.
 * EPIC       → animated rainbow sweep + outer glow.
 * LEGENDARY  → gold-shimmer outer ring + intense rainbow sweep.
 */
@Composable
fun ConsoleCard(
    consoleModel: ConsoleModelDto,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val rarity = consoleModel.rarity
    val visuals = rarityVisuals(rarity)
    val palette = visuals.palette
    val holoStrength = visuals.holoStrength

    val transition = rememberInfiniteTransition(label = "holo")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (rarity == Rarity.LEGENDARY) 2400 else 3600),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "holo-phase",
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
                RarityBadge(rarity)
                Text(
                    text = if (consoleModel.year > 0) consoleModel.year.toString() else "",
                    color = palette.foreground.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelSmall,
                )
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
                    text = consoleInitials(consoleModel.name),
                    color = palette.foreground,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            }

            Column {
                Text(
                    text = consoleModel.name,
                    color = palette.foreground,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Start,
                    maxLines = 2,
                )
                Text(
                    text = consoleModel.manufacturer,
                    color = palette.foreground.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun RarityBadge(rarity: Rarity) {
    val palette = rarityVisuals(rarity).palette
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(palette.badgeBg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = rarity.name,
            color = palette.badgeFg,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

private data class CardPalette(
    val backdrop: List<Color>,
    val sweep: List<Color>,
    val border: Color,
    val foreground: Color,
    val artBackdrop: Color,
    val badgeBg: Color,
    val badgeFg: Color,
)

private data class CardVisuals(
    val palette: CardPalette,
    val holoStrength: Float,
    val glow: Boolean,
)

private fun rarityVisuals(rarity: Rarity): CardVisuals {
    val palette = when (rarity) {
        Rarity.COMMON -> CardPalette(
            backdrop = listOf(Color(0xFF334155), Color(0xFF1F2937)),
            sweep = listOf(Color.Transparent, Color.White.copy(alpha = 0.08f), Color.Transparent),
            border = Color(0xFF475569),
            foreground = Color.White,
            artBackdrop = Color(0x3300000A),
            badgeBg = Color(0xFF94A3B8),
            badgeFg = Color(0xFF0F172A),
        )
        Rarity.UNCOMMON -> CardPalette(
            backdrop = listOf(Color(0xFF064E3B), Color(0xFF065F46)),
            sweep = listOf(Color.Transparent, Color(0x3334D399), Color.Transparent),
            border = Color(0xFF10B981),
            foreground = Color.White,
            artBackdrop = Color(0x3300000A),
            badgeBg = Color(0xFF34D399),
            badgeFg = Color(0xFF064E3B),
        )
        Rarity.RARE -> CardPalette(
            backdrop = listOf(Color(0xFF1E3A8A), Color(0xFF1E40AF)),
            sweep = listOf(
                Color.Transparent,
                Color(0x6660A5FA),
                Color(0x66E0E7FF),
                Color(0x6660A5FA),
                Color.Transparent,
            ),
            border = Color(0xFF60A5FA),
            foreground = Color.White,
            artBackdrop = Color(0x3300000A),
            badgeBg = Color(0xFF60A5FA),
            badgeFg = Color(0xFF1E3A8A),
        )
        Rarity.EPIC -> CardPalette(
            backdrop = listOf(Color(0xFF581C87), Color(0xFF4C1D95)),
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
        Rarity.LEGENDARY -> CardPalette(
            backdrop = listOf(Color(0xFF422006), Color(0xFF7C2D12)),
            sweep = listOf(
                Color(0xCCFCD34D),
                Color(0xCCFB923C),
                Color(0xCCEF4444),
                Color(0xCCFCD34D),
                Color(0xCCFFFFFF),
            ),
            border = Color(0xFFFCD34D),
            foreground = lerp(Color.White, Color(0xFFFFE4B5), 0.2f),
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
    val glow = rarity == Rarity.EPIC || rarity == Rarity.LEGENDARY
    return CardVisuals(palette, holoStrength, glow)
}

private fun consoleInitials(name: String): String {
    val words = name.split(" ", "-").filter { it.isNotBlank() }
    return when (words.size) {
        0 -> "?"
        1 -> words[0].take(2).uppercase()
        else -> (words[0].take(1) + words[1].take(1)).uppercase()
    }
}
