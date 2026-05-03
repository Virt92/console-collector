package com.virt92.consolecollector.ui.collection

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
import com.virt92.consolecollector.ui.viewmodel.viewModelFactory

@Composable
fun CollectionScreen(
    container: AppContainer,
    modifier: Modifier = Modifier,
    onOpenItem: (String) -> Unit,
) {
    val vm: CollectionViewModel =
        viewModel(factory = viewModelFactory { CollectionViewModel(container) })
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.refresh() }

    Box(modifier = modifier.fillMaxSize().padding(16.dp)) {
        when {
            state.loading && state.items.isEmpty() -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
            )
            state.items.isEmpty() -> EmptyCollection()
            else -> Column {
                state.stats?.let {
                    Text(
                        text = "${it.total} owned · ${it.uniqueModels}/${it.catalogSize} models · " +
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
                        ConsoleCard(
                            consoleModel = item.consoleModel,
                            onClick = { onOpenItem(item.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCollection() {
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
            text = "Your collection is empty",
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = "Tap “Scan” to photograph your first console — we'll identify it and add a card to your collection.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
