package com.virt92.consolecollector.ui.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.virt92.consolecollector.di.AppContainer
import com.virt92.consolecollector.ui.viewmodel.viewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    container: AppContainer,
    itemId: String,
    onBack: () -> Unit,
) {
    val vm: ItemDetailViewModel = viewModel(
        factory = viewModelFactory { ItemDetailViewModel(container, itemId) },
    )
    val state by vm.state.collectAsState()
    var shareUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.item?.consoleModel?.name ?: "Loading…") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.share { shareUrl = it } }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { vm.delete(onBack) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            val item = state.item
            when {
                state.loading -> CircularProgressIndicator()
                item != null -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ConsoleCard(consoleModel = item.consoleModel)
                    Text(
                        text = "Manufacturer: ${item.consoleModel.manufacturer}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(text = "Year: ${item.consoleModel.year}")
                    Text(text = "Rarity: ${item.consoleModel.rarity}")
                    item.consoleModel.description?.let { Text(text = it) }
                    Text(
                        text = "Status: ${item.status}",
                        color = MaterialTheme.colorScheme.primary,
                    )
                    item.notes?.takeIf { it.isNotBlank() }?.let {
                        Text(text = "Notes: $it")
                    }
                    shareUrl?.let {
                        Text(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            text = "Share link: $it",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                state.error != null -> Text(
                    text = state.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
