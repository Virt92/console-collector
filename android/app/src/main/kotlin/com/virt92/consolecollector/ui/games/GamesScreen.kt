package com.virt92.consolecollector.ui.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.virt92.consolecollector.di.AppContainer
import com.virt92.consolecollector.ui.collection.GameCard
import com.virt92.consolecollector.ui.viewmodel.viewModelFactory

@Composable
fun GamesScreen(
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    val vm: GamesViewModel =
        viewModel(factory = viewModelFactory { GamesViewModel(container) })
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.refresh() }

    Box(modifier = modifier.fillMaxSize().padding(16.dp)) {
        when {
            state.loading && state.items.isEmpty() -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
            )
            state.items.isEmpty() -> EmptyGames()
            else -> Column {
                state.stats?.let {
                    Text(
                        text = "${it.total} games · ${it.uniqueGames}/${it.catalogSize} titles · " +
                            "${it.byRarity["LEGENDARY"] ?: 0} legendary",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.items, key = { it.id }) { item ->
                        GameCard(
                            game = item.game,
                            platformSlug = item.platformSlug,
                            edition = item.edition,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyGames() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.AddAPhoto,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "No games yet",
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = "Tap “Scan game” to photograph a disc, cartridge or box — we'll identify the title and add a card.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
