package com.virt92.consolecollector.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.virt92.consolecollector.di.AppContainer
import com.virt92.consolecollector.ui.collection.CollectionScreen
import com.virt92.consolecollector.ui.profile.ProfileScreen
import com.virt92.consolecollector.ui.scan.ScanViewModel

private enum class Tab(val label: String) {
    Collection("Collection"),
    Profile("Profile"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    container: AppContainer,
    scanViewModel: ScanViewModel,
    onOpenScan: () -> Unit,
    onOpenItem: (String) -> Unit,
) {
    var tab by remember { mutableStateOf(Tab.Collection) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tab.label) },
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == Tab.Collection,
                    onClick = { tab = Tab.Collection },
                    icon = { Icon(Icons.Filled.GridView, contentDescription = null) },
                    label = { Text("Collection") },
                )
                NavigationBarItem(
                    selected = tab == Tab.Profile,
                    onClick = { tab = Tab.Profile },
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    label = { Text("Profile") },
                )
            }
        },
        floatingActionButton = {
            if (tab == Tab.Collection) {
                ExtendedFloatingActionButton(
                    onClick = {
                        scanViewModel.reset()
                        onOpenScan()
                    },
                    icon = { Icon(Icons.Filled.AddAPhoto, contentDescription = null) },
                    text = { Text("Scan") },
                )
            }
        },
    ) { padding ->
        when (tab) {
            Tab.Collection -> CollectionScreen(
                container = container,
                modifier = Modifier.padding(padding),
                onOpenItem = onOpenItem,
            )
            Tab.Profile -> ProfileScreen(
                container = container,
                modifier = Modifier.padding(padding),
            )
        }
    }
}
