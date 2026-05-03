package com.virt92.consolecollector.ui.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.virt92.consolecollector.data.model.ConsoleModelDto
import com.virt92.consolecollector.data.model.Rarity
import com.virt92.consolecollector.ui.collection.ConsoleCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanConfirmScreen(
    viewModel: ScanViewModel,
    onConfirmed: () -> Unit,
    onCancel: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        if (state.recognized == null && !state.recognizing) {
            viewModel.recognize()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirm card") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when {
                state.recognizing -> {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.padding(end = 12.dp))
                        Text("Identifying via vision model…")
                    }
                }
                state.recognized != null -> {
                    val r = state.recognized!!
                    val previewModel = ConsoleModelDto(
                        id = r.consoleModelId ?: "preview",
                        slug = r.consoleSlug,
                        name = r.consoleName,
                        manufacturer = "—",
                        year = 0,
                        rarity = r.rarity,
                    )
                    ConsoleCard(consoleModel = previewModel)
                    Text(
                        text = "Identified as: ${r.consoleName}",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = "Rarity: ${r.rarity}  ·  Confidence: ${(r.confidence * 100).toInt()}%",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (r.reasoning.isNotBlank()) {
                        Text(
                            text = r.reasoning,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    if (r.consoleModelId == null) {
                        Text(
                            text = "Note: this object isn't in our catalog yet. Add it manually if needed.",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                state.error != null -> {
                    Text(
                        text = state.error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(
                    onClick = { viewModel.confirmAndAdd { onConfirmed() } },
                    enabled = state.recognized?.consoleModelId != null && !state.saving,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (state.saving) "Saving…" else "Add to collection")
                }
            }

            // Sample preview when no recognition exists yet (debug aid).
            if (state.recognized == null && !state.recognizing) {
                Text(
                    text = "Preview:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 24.dp),
                )
                ConsoleCard(
                    consoleModel = ConsoleModelDto(
                        id = "demo",
                        slug = "playstation-5",
                        name = "PlayStation 5",
                        manufacturer = "Sony",
                        year = 2020,
                        rarity = Rarity.RARE,
                    ),
                )
            }
        }
    }
}
