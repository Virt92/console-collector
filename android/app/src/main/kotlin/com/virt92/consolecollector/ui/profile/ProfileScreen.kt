package com.virt92.consolecollector.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.virt92.consolecollector.di.AppContainer
import com.virt92.consolecollector.ui.viewmodel.viewModelFactory

@Composable
fun ProfileScreen(
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    val vm: ProfileViewModel = viewModel(factory = viewModelFactory { ProfileViewModel(container) })
    val state by vm.state.collectAsState()

    var displayName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.load() }
    LaunchedEffect(state.user?.id) {
        state.user?.let {
            displayName = it.displayName
            city = it.city.orEmpty()
            country = it.country.orEmpty()
            bio = it.bio.orEmpty()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        state.user?.let { user ->
            Text(text = user.email, style = MaterialTheme.typography.titleLarge)
        }

        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Display name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("City") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = country,
            onValueChange = { country = it },
            label = { Text("Country") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Bio") },
            modifier = Modifier.fillMaxWidth(),
        )

        if (state.message != null) {
            Text(state.message.orEmpty(), color = MaterialTheme.colorScheme.primary)
        }
        if (state.error != null) {
            Text(state.error.orEmpty(), color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                vm.save(
                    displayName = displayName,
                    city = city,
                    country = country,
                    bio = bio,
                )
            },
            enabled = !state.saving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.saving) "Saving…" else "Save profile")
        }

        OutlinedButton(
            onClick = { vm.shareCollection() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Share my collection")
        }
        state.shareUrl?.let {
            Text(
                "Public link: $it",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        OutlinedButton(
            onClick = { vm.logout() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Log out")
        }
    }
}
