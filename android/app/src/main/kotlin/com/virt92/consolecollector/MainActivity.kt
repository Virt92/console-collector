package com.virt92.consolecollector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.virt92.consolecollector.ui.AppRoot
import com.virt92.consolecollector.ui.theme.ConsoleCollectorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as ConsoleCollectorApp).container
        setContent {
            val isLoggedIn by container.authStore.tokenFlow.collectAsState(initial = null)
            ConsoleCollectorTheme {
                AppRoot(
                    container = container,
                    initialLoggedIn = isLoggedIn != null,
                )
            }
        }
    }
}
